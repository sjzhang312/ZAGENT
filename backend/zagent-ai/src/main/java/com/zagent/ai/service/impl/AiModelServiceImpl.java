package com.zagent.ai.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zagent.ai.domain.AiModel;
import com.zagent.ai.mapper.AiModelMapper;
import com.zagent.ai.service.IAiModelService;

/**
 * 模型Service业务层处理
 *
 * @author zagent
 */
@Service
public class AiModelServiceImpl implements IAiModelService
{
    @Autowired
    private AiModelMapper aiModelMapper;

    @Override
    public AiModel selectAiModelById(Long modelId)
    {
        return aiModelMapper.selectAiModelById(modelId);
    }

    @Override
    public List<AiModel> selectAiModelList(AiModel aiModel)
    {
        return aiModelMapper.selectAiModelList(aiModel);
    }

    @Override
    public int insertAiModel(AiModel aiModel)
    {
        return aiModelMapper.insertAiModel(aiModel);
    }

    @Override
    public int updateAiModel(AiModel aiModel)
    {
        return aiModelMapper.updateAiModel(aiModel);
    }

    @Override
    public int deleteAiModelByIds(Long[] modelIds)
    {
        return aiModelMapper.deleteAiModelByIds(modelIds);
    }
}
