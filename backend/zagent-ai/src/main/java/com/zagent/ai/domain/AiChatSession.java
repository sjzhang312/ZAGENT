package com.zagent.ai.domain;

import com.zagent.common.core.domain.BaseEntity;

/**
 * 问答会话对象 ai_chatsession
 *
 * @author zagent
 */
public class AiChatSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long sessionId;

    /** 会话名称 */
    private String sessionName;

    /** 智能体ID */
    private Long agentId;

    /** 用户ID */
    private Long userId;

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getSessionName()
    {
        return sessionName;
    }

    public void setSessionName(String sessionName)
    {
        this.sessionName = sessionName;
    }

    public Long getAgentId()
    {
        return agentId;
    }

    public void setAgentId(Long agentId)
    {
        this.agentId = agentId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

}
