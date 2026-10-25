package com.zagent.ai.service.impl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.zagent.ai.domain.AiSkill;
import com.zagent.ai.domain.AiSkillResource;
import com.zagent.ai.mapper.AiSkillMapper;
import com.zagent.ai.mapper.AiSkillResourceMapper;
import com.zagent.ai.service.IAiSkillService;
import com.zagent.ai.tools.SkillStorageService;
import com.zagent.common.exception.ServiceException;
import com.zagent.common.utils.StringUtils;

/**
 * 技能Service业务层处理。
 * 现代技能包（SKILL.md + references/scripts/data 附属资源）导入：
 * 全量解压落盘到沙箱 skills/{技能名}/（正文不存库），资源清单入 ai_skill_resource；
 * 散装单 md 兼容导入（content 存库）。
 *
 * @author zagent
 */
@Service
public class AiSkillServiceImpl implements IAiSkillService
{
    /** 路径分隔符兼容字符 */
    private static final String BS = String.valueOf((char) 92);

    @Autowired
    private AiSkillMapper aiSkillMapper;

    @Autowired
    private AiSkillResourceMapper resourceMapper;

    @Autowired
    private SkillStorageService storage;

    @Override
    public AiSkill selectAiSkillById(Long skillId)
    {
        AiSkill skill = aiSkillMapper.selectAiSkillById(skillId);
        if (skill != null)
        {
            fillFromFile(skill);
        }
        return skill;
    }

    /** 技能包技能：正文从沙箱 SKILL.md 回填，并附带资源清单 */
    private void fillFromFile(AiSkill skill)
    {
        if (storage.exists(skill.getSkillName()))
        {
            try
            {
                String main = storage.readMain(skill.getSkillName());
                if (main != null)
                {
                    skill.setContent(main);
                }
            }
            catch (Exception ignored)
            {
            }
            skill.setResources(resourceMapper.selectBySkillId(skill.getSkillId()));
        }
    }

    @Override
    public List<AiSkill> selectAiSkillList(AiSkill aiSkill)
    {
        return aiSkillMapper.selectAiSkillList(aiSkill);
    }

    @Override
    public int insertAiSkill(AiSkill aiSkill)
    {
        return aiSkillMapper.insertAiSkill(aiSkill);
    }

    @Override
    public int updateAiSkill(AiSkill aiSkill)
    {
        int rows = aiSkillMapper.updateAiSkill(aiSkill);
        // 技能包技能在管理页编辑正文时，同步写回沙箱 SKILL.md，保持库与文件一致
        if (rows > 0 && StringUtils.isNotEmpty(aiSkill.getContent()) && storage.exists(aiSkill.getSkillName()))
        {
            try
            {
                storage.writeMain(aiSkill.getSkillName(), aiSkill.getContent());
            }
            catch (Exception e)
            {
                throw new ServiceException("技能正文写回文件失败: " + e.getMessage());
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int deleteAiSkillByIds(Long[] skillIds)
    {
        for (Long id : skillIds)
        {
            AiSkill skill = aiSkillMapper.selectAiSkillById(id);
            if (skill == null)
            {
                continue;
            }
            resourceMapper.deleteBySkillId(id);
            try
            {
                storage.delete(skill.getSkillName());
            }
            catch (Exception ignored)
            {
            }
        }
        return aiSkillMapper.deleteAiSkillByIds(skillIds);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> importSkills(MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请选择 zip 文件");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".zip"))
        {
            throw new ServiceException("仅支持 .zip 文件");
        }
        byte[] zipBytes = file.getBytes();
        List<Map<String, Object>> detail = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8))
        {
            // 阶段一：收集 zip 内全部安全的 .md 条目（分隔符统一为 /，兼容 Windows 反斜杠 zip）
            Map<String, String> mdEntries = new LinkedHashMap<>();
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null)
            {
                String name = entry.getName().replace(BS, "/");
                if (entry.isDirectory() || name.contains("..") || name.startsWith("/")
                        || name.startsWith("__MACOSX") || name.startsWith("."))
                {
                    continue;
                }
                if (!name.toLowerCase().endsWith(".md"))
                {
                    continue;
                }
                mdEntries.put(name, new String(zis.readAllBytes(), StandardCharsets.UTF_8));
            }

            // 阶段二：筛选技能文件 ——
            // 现代技能包是「SKILL.md + references/data 等参考文档」结构，
            // 只有 SKILL.md 是技能定义，其余文档作为附属资源落盘，不单独成技能；
            // 整个 zip 只有一个散装 md 时仍按单文件技能兼容导入
            List<String> selected = new ArrayList<>();
            boolean hasSkillMd = false;
            for (String n : mdEntries.keySet())
            {
                String base = n.substring(0, n.length() - 3);
                int slash = lastSep(base);
                String title = slash >= 0 ? base.substring(slash + 1) : base;
                if ("SKILL".equalsIgnoreCase(title) && slash > 0)
                {
                    hasSkillMd = true;
                    selected.add(n);
                }
            }
            if (!hasSkillMd)
            {
                if (mdEntries.size() == 1)
                {
                    selected.addAll(mdEntries.keySet());
                }
                else
                {
                    throw new ServiceException("zip 中未找到 {技能名}/SKILL.md，且包含 "
                            + mdEntries.size() + " 个散装 md 无法确定技能；请压缩技能目录（含 SKILL.md）或单个 md 文件");
                }
            }

            for (String name : selected)
            {
                String content = mdEntries.get(name);

                // 技能名：SKILL.md 取父目录名，否则取文件名
                String base = name.substring(0, name.length() - 3);
                int slash = lastSep(base);
                String fileTitle = slash >= 0 ? base.substring(slash + 1) : base;
                String skillName;
                String packagePrefix = null;
                if ("SKILL".equalsIgnoreCase(fileTitle) && slash > 0)
                {
                    String parent = base.substring(0, slash);
                    int pslash = lastSep(parent);
                    skillName = pslash >= 0 ? parent.substring(pslash + 1) : parent;
                    packagePrefix = parent + "/";
                }
                else
                {
                    skillName = fileTitle;
                }
                skillName = sanitize(skillName);
                if (skillName.isEmpty())
                {
                    continue;
                }

                // 解析 frontmatter（首行 --- 到下一行 --- 之间的 name/description）
                String description = "";
                String body = content;
                String[] lines = content.split("\\n");
                int end = -1;
                if (lines.length > 0 && lines[0].trim().equals("---"))
                {
                    for (int i = 1; i < lines.length; i++)
                    {
                        if (lines[i].trim().equals("---"))
                        {
                            end = i;
                            break;
                        }
                    }
                    if (end > 0)
                    {
                        for (int i = 1; i < end; i++)
                        {
                            String line = lines[i];
                            int colon = line.indexOf(':');
                            if (colon <= 0)
                            {
                                continue;
                            }
                            String key = line.substring(0, colon).trim().toLowerCase();
                            String val = stripQuotes(line.substring(colon + 1).trim());
                            if ("name".equals(key) && !val.isEmpty())
                            {
                                skillName = sanitize(val);
                            }
                            else if ("description".equals(key))
                            {
                                description = val;
                            }
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = end + 1; i < lines.length; i++)
                        {
                            sb.append(lines[i]).append('\n');
                        }
                        body = sb.toString();
                    }
                }
                // 描述超长截断到列宽 2000
                if (description.length() > 2000)
                {
                    description = description.substring(0, 2000);
                }

                // 技能包（SKILL.md）：全量解压落盘沙箱，正文不存库，资源清单入库
                int resourceCount = 0;
                if (packagePrefix != null)
                {
                    List<Map<String, Object>> files = storage.extract(zipBytes, skillName, packagePrefix);
                    List<AiSkillResource> resources = new ArrayList<>();
                    for (Map<String, Object> f : files)
                    {
                        String rel = String.valueOf(f.get("relPath"));
                        if ("SKILL.md".equalsIgnoreCase(rel))
                        {
                            continue;
                        }
                        AiSkillResource r = new AiSkillResource();
                        r.setRelPath(rel);
                        r.setFileSize((Long) f.get("size"));
                        resources.add(r);
                    }
                    resourceCount = resources.size();

                    AiSkill q = new AiSkill();
                    q.setSkillName(skillName);
                    List<AiSkill> exists = aiSkillMapper.selectAiSkillList(q);
                    Long skillId;
                    String action;
                    if (exists.isEmpty())
                    {
                        AiSkill s = new AiSkill();
                        s.setSkillName(skillName);
                        s.setDescription(description);
                        s.setContent("");
                        s.setEnabled("0");
                        s.setCreateBy("import");
                        aiSkillMapper.insertAiSkill(s);
                        skillId = s.getSkillId();
                        action = "新增";
                    }
                    else
                    {
                        AiSkill s = exists.get(0);
                        s.setDescription(description);
                        s.setContent("");
                        s.setUpdateBy("import");
                        aiSkillMapper.updateAiSkill(s);
                        skillId = s.getSkillId();
                        action = "更新";
                    }
                    if (!resources.isEmpty())
                    {
                        for (AiSkillResource r : resources)
                        {
                            r.setSkillId(skillId);
                        }
                        resourceMapper.deleteBySkillId(skillId);
                        resourceMapper.insertBatch(resources);
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", skillName);
                    row.put("action", action);
                    row.put("description", description);
                    row.put("resourceCount", resourceCount);
                    detail.add(row);
                }
                else
                {
                    // 散装单 md：content 存库（无附属资源）
                    AiSkill q = new AiSkill();
                    q.setSkillName(skillName);
                    List<AiSkill> exists = aiSkillMapper.selectAiSkillList(q);
                    String action;
                    if (exists.isEmpty())
                    {
                        AiSkill s = new AiSkill();
                        s.setSkillName(skillName);
                        s.setDescription(description);
                        s.setContent(body.trim());
                        s.setEnabled("0");
                        s.setCreateBy("import");
                        aiSkillMapper.insertAiSkill(s);
                        action = "新增";
                    }
                    else
                    {
                        AiSkill s = exists.get(0);
                        s.setDescription(description);
                        s.setContent(body.trim());
                        s.setUpdateBy("import");
                        aiSkillMapper.updateAiSkill(s);
                        action = "更新";
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", skillName);
                    row.put("action", action);
                    row.put("description", description);
                    row.put("resourceCount", 0);
                    detail.add(row);
                }
            }
        }
        if (detail.isEmpty())
        {
            throw new ServiceException("zip 中未找到技能文件（每技能一个目录 {name}/SKILL.md 或根目录 *.md）");
        }
        return detail;
    }

    private int lastSep(String path)
    {
        return Math.max(path.lastIndexOf('/'), path.lastIndexOf(BS));
    }

    /** 技能名只保留字母/数字/中文/连字符，小写 */
    private String sanitize(String name)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : name.trim().toLowerCase().toCharArray())
        {
            if ((Character.isLetterOrDigit(c) && c < 128) || c == '-' || (c >= 0x4e00 && c <= 0x9fa5))
            {
                sb.append(c);
            }
            else if (c == ' ' || c == '_')
            {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    private String stripQuotes(String s)
    {
        if (s.length() >= 2 && ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\""))))
        {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
