package com.zagent.ai.mapper;

import java.util.List;
import com.zagent.ai.domain.AiModel;

/**
 * AiModelMapper
 *
 * @author zagent
 */
public interface AiModelMapper
{
    public AiModel selectAiModelById(Long modelId);

    public List<AiModel> selectAiModelList(AiModel aiModel);

    public int insertAiModel(AiModel aiModel);

    public int updateAiModel(AiModel aiModel);

    public int deleteAiModelByIds(Long[] modelIdss);
}
