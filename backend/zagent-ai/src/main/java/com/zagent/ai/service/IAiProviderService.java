package com.zagent.ai.service;

import java.util.List;
import com.zagent.ai.domain.AiProvider;

/**
 * 服务商Service接口
 *
 * @author zagent
 */
public interface IAiProviderService
{
    public AiProvider selectAiProviderById(Long providerId);

    public List<AiProvider> selectAiProviderList(AiProvider aiProvider);

    public int insertAiProvider(AiProvider aiProvider);

    public int updateAiProvider(AiProvider aiProvider);

    public int deleteAiProviderByIds(Long[] providerIds);

    /**
     * 测试连通性并拉取可选模型列表
     */
    public java.util.List<String> testConnection(Long providerId);
}
