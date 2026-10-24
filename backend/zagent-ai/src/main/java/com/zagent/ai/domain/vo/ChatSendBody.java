package com.zagent.ai.domain.vo;

/**
 * 问答发送请求体
 *
 * @author zagent
 */
public class ChatSendBody
{
    /** 会话 ID（为空则新建会话） */
    private Long sessionId;

    /** 智能体 ID */
    private Long agentId;

    /** 消息内容（开头可带 /技能A /技能B 多个斜杠调用） */
    private String content;

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public Long getAgentId()
    {
        return agentId;
    }

    public void setAgentId(Long agentId)
    {
        this.agentId = agentId;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
