package com.zagent.ai.domain;

import com.zagent.common.core.domain.BaseEntity;

/**
 * 问答消息对象 ai_chatmessage
 *
 * @author zagent
 */
public class AiChatMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long messageId;

    /** 会话ID */
    private Long sessionId;

    /** 角色 user|assistant */
    private String role;

    /** 消息内容 */
    private String content;

    /** 思考内容 */
    private String reasoning;

    /** 状态 done|error */
    private String status;

    /** 耗时毫秒 */
    private Long elapsedMs;

    /** 使用的模型描述 */
    private String modelDesc;

    public Long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getReasoning()
    {
        return reasoning;
    }

    public void setReasoning(String reasoning)
    {
        this.reasoning = reasoning;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getElapsedMs()
    {
        return elapsedMs;
    }

    public void setElapsedMs(Long elapsedMs)
    {
        this.elapsedMs = elapsedMs;
    }

    public String getModelDesc()
    {
        return modelDesc;
    }

    public void setModelDesc(String modelDesc)
    {
        this.modelDesc = modelDesc;
    }

}
