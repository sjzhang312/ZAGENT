package com.zagent.ai.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.zagent.ai.domain.AiSkill;

/**
 * 技能Service接口
 *
 * @author zagent
 */
public interface IAiSkillService
{
    public AiSkill selectAiSkillById(Long skillId);

    public List<AiSkill> selectAiSkillList(AiSkill aiSkill);

    public int insertAiSkill(AiSkill aiSkill);

    public int updateAiSkill(AiSkill aiSkill);

    public int deleteAiSkillByIds(Long[] skillIds);

    /**
     * zip 导入技能：每个技能一个目录 {name}/SKILL.md（YAML frontmatter: name/description + 正文），
     * 兼容 zip 根目录散装 *.md；按技能名 upsert，导入后默认停用；返回导入明细
     */
    public List<Map<String, Object>> importSkills(MultipartFile file) throws Exception;
}
