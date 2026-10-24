package com.zagent.ai.domain;

import com.zagent.common.core.domain.BaseEntity;

/**
 * AI 技能对象 ai_skill
 *
 * @author zagent
 */
public class AiSkill extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 技能ID */
    private Long skillId;

    /** 技能名(斜杠调用名) */
    private String skillName;

    /** 技能描述 */
    private String description;

    /** 技能内容 markdown */
    private String content;

    /** 是否启用 0停用 1启用 */
    private String enabled;

    public Long getSkillId()
    {
        return skillId;
    }

    public void setSkillId(Long skillId)
    {
        this.skillId = skillId;
    }

    public String getSkillName()
    {
        return skillName;
    }

    public void setSkillName(String skillName)
    {
        this.skillName = skillName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getEnabled()
    {
        return enabled;
    }

    public void setEnabled(String enabled)
    {
        this.enabled = enabled;
    }

}
