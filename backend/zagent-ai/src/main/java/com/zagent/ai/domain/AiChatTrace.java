package com.zagent.ai.domain;

import com.zagent.common.core.domain.BaseEntity;

/**
 * 工具调用轨迹对象 ai_chattrace
 *
 * @author zagent
 */
public class AiChatTrace extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 轨迹ID */
    private Long traceId;

    /** 消息ID */
    private Long messageId;

    /** 序号 */
    private Integer seq;

    /** 工具名 */
    private String toolName;

    /** 调用参数 */
    private String args;

    /** 执行结果 */
    private String result;

    /** 状态 0成功 1失败 */
    private String status;

    /** 耗时毫秒 */
    private Long durationMs;

    public Long getTraceId()
    {
        return traceId;
    }

    public void setTraceId(Long traceId)
    {
        this.traceId = traceId;
    }

    public Long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    public Integer getSeq()
    {
        return seq;
    }

    public void setSeq(Integer seq)
    {
        this.seq = seq;
    }

    public String getToolName()
    {
        return toolName;
    }

    public void setToolName(String toolName)
    {
        this.toolName = toolName;
    }

    public String getArgs()
    {
        return args;
    }

    public void setArgs(String args)
    {
        this.args = args;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getDurationMs()
    {
        return durationMs;
    }

    public void setDurationMs(Long durationMs)
    {
        this.durationMs = durationMs;
    }

}
