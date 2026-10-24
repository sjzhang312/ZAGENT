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
import org.springframework.web.multipart.MultipartFile;
import com.zagent.ai.domain.AiSkill;
import com.zagent.ai.mapper.AiSkillMapper;
import com.zagent.ai.service.IAiSkillService;
import com.zagent.common.exception.ServiceException;

/**
 * 技能Service业务层处理：含 zip 导入（SKILL.md frontmatter 解析、防 zip-slip、按名 upsert）
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

    @Override
    public AiSkill selectAiSkillById(Long skillId)
    {
        return aiSkillMapper.selectAiSkillById(skillId);
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
        return aiSkillMapper.updateAiSkill(aiSkill);
    }

    @Override
    public int deleteAiSkillByIds(Long[] skillIds)
    {
        return aiSkillMapper.deleteAiSkillByIds(skillIds);
    }

    @Override
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
        List<Map<String, Object>> detail = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(file.getBytes()), StandardCharsets.UTF_8))
        {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null)
            {
                String name = entry.getName();
                if (entry.isDirectory() || name.contains("..") || name.startsWith("/")
                        || name.startsWith(BS) || name.startsWith("__MACOSX") || name.startsWith("."))
                {
                    continue;
                }
                if (!name.toLowerCase().endsWith(".md"))
                {
                    continue;
                }
                String content = new String(zis.readAllBytes(), StandardCharsets.UTF_8);

                // 技能名：SKILL.md 取父目录名，否则取文件名
                String base = name.substring(0, name.length() - 3);
                int slash = lastSep(base);
                String fileTitle = slash >= 0 ? base.substring(slash + 1) : base;
                String skillName;
                if ("SKILL".equalsIgnoreCase(fileTitle) && slash > 0)
                {
                    String parent = base.substring(0, slash);
                    int pslash = lastSep(parent);
                    skillName = pslash >= 0 ? parent.substring(pslash + 1) : parent;
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

                // upsert（按技能名），导入后默认停用
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
                detail.add(row);
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
