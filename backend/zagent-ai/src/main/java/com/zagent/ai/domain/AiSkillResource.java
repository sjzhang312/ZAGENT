package com.zagent.ai.domain;

import java.io.Serializable;

/**
 * AI 技能附属资源对象 ai_skill_resource
 * 现代技能包（SKILL.md + references/scripts/data 等）的文件清单，
 * 文件本体落盘于沙箱 skills/{技能名}/ 目录。
 *
 * @author zagent
 */
public class AiSkillResource implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 资源ID */
    private Long resourceId;

    /** 技能ID */
    private Long skillId;

    /** 相对技能根目录的路径（如 references/cookbook.md） */
    private String relPath;

    /** 文件大小（字节） */
    private Long fileSize;

    public Long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(Long resourceId)
    {
        this.resourceId = resourceId;
    }

    public Long getSkillId()
    {
        return skillId;
    }

    public void setSkillId(Long skillId)
    {
        this.skillId = skillId;
    }

    public String getRelPath()
    {
        return relPath;
    }

    public void setRelPath(String relPath)
    {
        this.relPath = relPath;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }
}
