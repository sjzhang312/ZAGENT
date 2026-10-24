package com.zagent.ai.service;

import java.util.List;
import com.zagent.ai.domain.AiModel;

/**
 * 模型Service接口
 *
 * @author zagent
 */
public interface IAiModelService
{
    public AiModel selectAiModelById(Long modelId);

    public List<AiModel> selectAiModelList(AiModel aiModel);

    public int insertAiModel(AiModel aiModel);

    public int updateAiModel(AiModel aiModel);

    public int deleteAiModelByIds(Long[] modelIds);
}
