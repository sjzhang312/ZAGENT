package com.zagent.ai.domain;

import com.zagent.common.core.domain.BaseEntity;

/**
 * AI 模型配置对象 ai_model
 *
 * @author zagent
 */
public class AiModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模型ID */
    private Long modelId;

    /** 所属服务商ID */
    private Long providerId;

    /** 模型标识 如 claude-sonnet-4-5 / gpt-4o */
    private String modelCode;

    /** 显示名称 */
    private String displayName;

    /** 是否支持思考 0否 1是 */
    private String supportReasoning;

    /** 状态 0正常 1停用 */
    private String status;

    public Long getModelId()
    {
        return modelId;
    }

    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    public Long getProviderId()
    {
        return providerId;
    }

    public void setProviderId(Long providerId)
    {
        this.providerId = providerId;
    }

    public String getModelCode()
    {
        return modelCode;
    }

    public void setModelCode(String modelCode)
    {
        this.modelCode = modelCode;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getSupportReasoning()
    {
        return supportReasoning;
    }

    public void setSupportReasoning(String supportReasoning)
    {
        this.supportReasoning = supportReasoning;
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
