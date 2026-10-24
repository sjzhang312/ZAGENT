package com.zagent.ai.mapper;

import java.util.List;
import com.zagent.ai.domain.AiSkill;

/**
 * AiSkillMapper
 *
 * @author zagent
 */
public interface AiSkillMapper
{
    public AiSkill selectAiSkillById(Long skillId);

    public List<AiSkill> selectAiSkillList(AiSkill aiSkill);

    public int insertAiSkill(AiSkill aiSkill);

    public int updateAiSkill(AiSkill aiSkill);

    public int deleteAiSkillByIds(Long[] skillIdss);
}
