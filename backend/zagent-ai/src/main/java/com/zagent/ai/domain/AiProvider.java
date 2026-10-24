package com.zagent.ai.domain;

import com.zagent.common.core.domain.BaseEntity;

/**
 * AI 服务商配置对象 ai_provider
 *
 * @author zagent
 */
public class AiProvider extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 服务商ID */
    private Long providerId;

    /** 服务商名称 */
    private String providerName;

    /** 服务商编码 anthropic|openai */
    private String providerCode;

    /** API Key */
    private String apiKey;

    /** API 地址(支持中转) */
    private String baseUrl;

    /** 状态 0正常 1停用 */
    private String status;

    public Long getProviderId()
    {
        return providerId;
    }

    public void setProviderId(Long providerId)
    {
        this.providerId = providerId;
    }

    public String getProviderName()
    {
        return providerName;
    }

    public void setProviderName(String providerName)
    {
        this.providerName = providerName;
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
