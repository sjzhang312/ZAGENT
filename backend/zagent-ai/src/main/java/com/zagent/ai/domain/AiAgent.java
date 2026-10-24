package com.zagent.ai.domain;

import com.zagent.common.core.domain.BaseEntity;

/**
 * AI 智能体配置对象 ai_agent
 *
 * @author zagent
 */
public class AiAgent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 智能体ID */
    private Long agentId;

    /** 智能体名称 */
    private String agentName;

    /** 描述 */
    private String description;

    /** 绑定模型ID */
    private Long modelId;

    /** 思考等级 0关 1低 2中 3高 */
    private Integer thinkingLevel;

    /** 温度 */
    private Double temperature;

    /** 最大输出 token */
    private Integer maxTokens;

    /** 启用工具 逗号分隔 read,write,edit,bash */
    private String enabledTools;

    /** 系统提示词 */
    private String systemPrompt;

    /** 状态 0正常 1停用 */
    private String status;

    public Long getAgentId()
    {
        return agentId;
    }

    public void setAgentId(Long agentId)
    {
        this.agentId = agentId;
    }

    public String getAgentName()
    {
        return agentName;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getModelId()
    {
        return modelId;
    }

    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    public Integer getThinkingLevel()
    {
        return thinkingLevel;
    }

    public void setThinkingLevel(Integer thinkingLevel)
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

    public String getEnabledTools()
    {
        return enabledTools;
    }

    public void setEnabledTools(String enabledTools)
    {
        this.enabledTools = enabledTools;
    }

    public String getSystemPrompt()
    {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt)
    {
        this.systemPrompt = systemPrompt;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
