package com.zagent.ai.mapper;

import java.util.List;
import com.zagent.ai.domain.AiSkillResource;

/**
 * AiSkillResourceMapper
 *
 * @author zagent
 */
public interface AiSkillResourceMapper
{
    public List<AiSkillResource> selectBySkillId(Long skillId);

    public int insertBatch(List<AiSkillResource> resources);

    public int deleteBySkillId(Long skillId);
}
