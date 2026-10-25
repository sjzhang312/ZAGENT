package com.zagent.ai.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.zagent.common.exception.ServiceException;
import com.zagent.common.utils.StringUtils;
import jakarta.annotation.PostConstruct;

/**
 * 技能包存储：现代技能包（SKILL.md + references/scripts/data 等附属资源）
 * 完整解压到沙箱 {workspace}/skills/{技能名}/ 目录，使 read/write/edit/bash
 * 四个沙箱工具天然可访问技能文件（脚本可执行、文档可读、产物可写）。
 *
 * @author zagent
 */
@Component
public class SkillStorageService
{
    private static final Logger log = LoggerFactory.getLogger(SkillStorageService.class);

    /** 路径分隔符兼容字符 */
    private static final String BS = String.valueOf((char) 92);

    /** 单文件大小上限（32MB），防异常大文件撑爆磁盘 */
    private static final long FILE_SIZE_LIMIT = 32L * 1024 * 1024;

    @Value("${zagent.ai.workspace:./ai-workspace}")
    private String workspace;

    private Path skillsRoot;

    @PostConstruct
    public void init() throws IOException
    {
        skillsRoot = Paths.get(workspace).toAbsolutePath().normalize().resolve("skills");
        Files.createDirectories(skillsRoot);
        log.info("AI 技能包存储目录：{}", skillsRoot);
    }

    /** 技能根目录 */
    public Path skillDir(String skillName)
    {
        String safe = safeName(skillName);
        if (safe == null)
        {
            throw new ServiceException("非法技能名: " + skillName);
        }
        return skillsRoot.resolve(safe);
    }

    public boolean exists(String skillName)
    {
        Path dir;
        try
        {
            dir = skillDir(skillName);
        }
        catch (Exception e)
        {
            return false;
        }
        return Files.isDirectory(dir) && Files.isRegularFile(dir.resolve("SKILL.md"));
    }

    /**
     * 解压技能包到 skills/{skillName}/（清旧重建）。
     * stripPrefix：zip 内技能包的根前缀（如 "skills/drawio-skill/"），解压时剥掉，
     * 使技能根目录下直接是 SKILL.md、references/ 等；null 表示不剥。
     *
     * @return 落盘文件清单（相对技能根目录路径 + 大小）
     */
    public List<Map<String, Object>> extract(byte[] zipBytes, String skillName, String stripPrefix) throws IOException
    {
        Path dir = skillDir(skillName);
        // 清旧重建
        if (Files.exists(dir))
        {
            try (var walk = Files.walk(dir))
            {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try
                    {
                        Files.delete(p);
                    }
                    catch (IOException ignored)
                    {
                    }
                });
            }
        }
        Files.createDirectories(dir);

        String prefix = stripPrefix == null ? "" : stripPrefix;
        List<Map<String, Object>> files = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8))
        {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null)
            {
                String name = entry.getName().replace(BS, "/");
                if (entry.isDirectory() || name.contains("..") || name.startsWith("/")
                        || name.startsWith("__MACOSX") || name.startsWith("."))
                {
                    continue;
                }
                if (StringUtils.isNotEmpty(prefix))
                {
                    if (!name.startsWith(prefix))
                    {
                        continue;
                    }
                    name = name.substring(prefix.length());
                }
                if (name.isEmpty())
                {
                    continue;
                }
                Path target = dir.resolve(name).normalize();
                if (!target.startsWith(dir))
                {
                    continue; // zip-slip 防御
                }
                if (entry.getSize() > FILE_SIZE_LIMIT)
                {
                    throw new ServiceException("技能文件过大（>32MB）: " + name);
                }
                if (target.getParent() != null)
                {
                    Files.createDirectories(target.getParent());
                }
                Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                Map<String, Object> f = new java.util.LinkedHashMap<>();
                f.put("relPath", name);
                f.put("size", Files.size(target));
                files.add(f);
            }
        }
        if (!Files.isRegularFile(dir.resolve("SKILL.md")))
        {
            throw new ServiceException("技能包解压后缺少 SKILL.md: " + skillName);
        }
        log.info("技能包 {} 落盘完成，共 {} 个文件：{}", skillName, files.size(), dir);
        return files;
    }

    /** 读取 SKILL.md 全文 */
    public String readMain(String skillName) throws IOException
    {
        Path main = skillDir(skillName).resolve("SKILL.md");
        if (!Files.isRegularFile(main))
        {
            return null;
        }
        return Files.readString(main, StandardCharsets.UTF_8);
    }

    /** 覆盖写回 SKILL.md（管理页编辑包技能正文时同步文件） */
    public void writeMain(String skillName, String content) throws IOException
    {
        Path main = skillDir(skillName).resolve("SKILL.md");
        Files.writeString(main, content == null ? "" : content, StandardCharsets.UTF_8);
    }

    /** 删除技能目录 */
    public void delete(String skillName) throws IOException
    {
        Path dir = skillDir(skillName);
        if (!Files.exists(dir))
        {
            return;
        }
        try (var walk = Files.walk(dir))
        {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try
                {
                    Files.delete(p);
                }
                catch (IOException ignored)
                {
                }
            });
        }
    }

    private String safeName(String name)
    {
        if (StringUtils.isEmpty(name))
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : name.trim().toLowerCase().toCharArray())
        {
            if ((Character.isLetterOrDigit(c) && c < 128) || c == '-' || (c >= 0x4e00 && c <= 0x9fa5))
            {
                sb.append(c);
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }
}
