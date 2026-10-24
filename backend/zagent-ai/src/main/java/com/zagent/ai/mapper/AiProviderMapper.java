package com.zagent.ai.mapper;

import java.util.List;
import com.zagent.ai.domain.AiProvider;

/**
 * AiProviderMapper
 *
 * @author zagent
 */
public interface AiProviderMapper
{
    public AiProvider selectAiProviderById(Long providerId);

    public List<AiProvider> selectAiProviderList(AiProvider aiProvider);

    public int insertAiProvider(AiProvider aiProvider);

    public int updateAiProvider(AiProvider aiProvider);

    public int deleteAiProviderByIds(Long[] providerIdss);
}
