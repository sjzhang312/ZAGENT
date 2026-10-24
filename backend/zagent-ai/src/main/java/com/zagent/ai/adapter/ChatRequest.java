package com.zagent.ai.adapter;

import java.util.List;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.ToolCallback;

/**
 * 适配层请求上下文：由 Chat 服务根据 服务商/模型/智能体 配置组装。
 *
 * @author zagent
 */
public class ChatRequest
{
    /** 服务商配置 ID（模型缓存 key 用） */
    private Long providerId;
    /** 服务商编码 anthropic | openai */
    private String providerCode;
    private String apiKey;
    private String baseUrl;
    /** 模型标识 */
    private String modelCode;
    /** 模型是否支持思考 */
    private boolean supportReasoning;
    /** 思考等级 0关 1低 2中 3高 */
    private int thinkingLevel;
    private Double temperature;
    private Integer maxTokens;
    /** 系统提示词（含技能内容） */
    private String systemPrompt;
    /** 历史消息（不含本轮 user 消息） */
    private List<Message> history;
    /** 本轮用户消息 */
    private String userMessage;
    /** 工具回调（已按智能体配置过滤并包装事件发射） */
    private List<ToolCallback> toolCallbacks;
    /** 工具循环最大轮数 */
    private int maxToolRounds = 10;

    public Long getProviderId()
    {
        return providerId;
    }

    public void setProviderId(Long providerId)
    {
        this.providerId = providerId;
    }

    public String getProviderCode()
    {
        return providerCode;
    }

    public void setProviderCode(String providerCode)
    {
        this.providerCode = providerCode;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getModelCode()
    {
        return modelCode;
    }

    public void setModelCode(String modelCode)
    {
        this.modelCode = modelCode;
    }

    public boolean isSupportReasoning()
    {
        return supportReasoning;
    }

    public void setSupportReasoning(boolean supportReasoning)
    {
        this.supportReasoning = supportReasoning;
    }

    public int getThinkingLevel()
    {
        return thinkingLevel;
    }

    public void setThinkingLevel(int thinkingLevel)
    {
        this.thinkingLevel = thinkingLevel;
    }

    public Double getTemperature()
    {
        return temperature;
    }

    public void setTemperature(Double temperature)
    {
        this.temperature = temperature;
    }

    public Integer getMaxTokens()
    {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens)
    {
        this.maxTokens = maxTokens;
    }

    public String getSystemPrompt()
    {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt)
    {
        this.systemPrompt = systemPrompt;
    }

    public List<Message> getHistory()
    {
        return history;
    }

    public void setHistory(List<Message> history)
    {
        this.history = history;
    }

    public String getUserMessage()
    {
        return userMessage;
    }

    public void setUserMessage(String userMessage)
    {
        this.userMessage = userMessage;
    }

    public List<ToolCallback> getToolCallbacks()
    {
        return toolCallbacks;
    }

    public void setToolCallbacks(List<ToolCallback> toolCallbacks)
    {
        this.toolCallbacks = toolCallbacks;
    }

    public int getMaxToolRounds()
    {
        return maxToolRounds;
    }

    public void setMaxToolRounds(int maxToolRounds)
    {
        this.maxToolRounds = maxToolRounds;
    }
}
