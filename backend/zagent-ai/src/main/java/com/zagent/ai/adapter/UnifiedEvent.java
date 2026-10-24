package com.zagent.ai.adapter;

/**
 * 统一模型事件：与前端 SSE 帧一一对应。
 * 适配层把 Anthropic / OpenAI 两家的流式响应归一为该事件流，Chat 服务只消费这一种格式。
 *
 * type 取值：
 *  - thinking   思考增量（delta）
 *  - delta      正文增量（text）
 *  - tool_start 工具调用开始（tool、args）
 *  - tool_end   工具调用结束（tool、status ok|error、durationMs、result）
 *  - warning    提示（未命中的技能名等）
 *  - error      错误（message）
 *  - done       结束（elapsedMs、messageId、content 为空）
 *
 * @author zagent
 */
public class UnifiedEvent
{
    private String type;
    private String text;
    private String tool;
    private String args;
    private String result;
    private String status;
    private Long durationMs;
    private String message;
    private Long elapsedMs;
    private Long messageId;

    public static UnifiedEvent thinking(String delta)
    {
        UnifiedEvent e = new UnifiedEvent();
        e.type = "thinking";
        e.text = delta;
        return e;
    }

    public static UnifiedEvent delta(String text)
    {
        UnifiedEvent e = new UnifiedEvent();
        e.type = "delta";
        e.text = text;
        return e;
    }

    public static UnifiedEvent toolStart(String tool, String args)
    {
        UnifiedEvent e = new UnifiedEvent();
        e.type = "tool_start";
        e.tool = tool;
        e.args = args;
        return e;
    }

    public static UnifiedEvent toolEnd(String tool, String status, long durationMs, String result)
    {
        UnifiedEvent e = new UnifiedEvent();
        e.type = "tool_end";
        e.tool = tool;
        e.status = status;
        e.durationMs = durationMs;
        e.result = result;
        return e;
    }

    public static UnifiedEvent warning(String message)
    {
        UnifiedEvent e = new UnifiedEvent();
        e.type = "warning";
        e.message = message;
        return e;
    }

    public static UnifiedEvent error(String message)
    {
        UnifiedEvent e = new UnifiedEvent();
        e.type = "error";
        e.message = message;
        return e;
    }

    public static UnifiedEvent done(long elapsedMs, Long messageId)
    {
        UnifiedEvent e = new UnifiedEvent();
        e.type = "done";
        e.elapsedMs = elapsedMs;
        e.messageId = messageId;
        return e;
    }

    public String getType()
    {
        return type;
    }

    public String getText()
    {
        return text;
    }

    public String getTool()
    {
        return tool;
    }

    public String getArgs()
    {
        return args;
    }

    public String getResult()
    {
        return result;
    }

    public String getStatus()
    {
        return status;
    }

    public Long getDurationMs()
    {
        return durationMs;
    }

    public String getMessage()
    {
        return message;
    }

    public Long getElapsedMs()
    {
        return elapsedMs;
    }

    public Long getMessageId()
    {
        return messageId;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }
}
