package com.zagent.ai.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zagent.ai.domain.AiAgent;
import com.zagent.ai.mapper.AiAgentMapper;
import com.zagent.ai.service.IAiAgentService;

/**
 * 智能体Service业务层处理
 *
 * @author zagent
 */
@Service
public class AiAgentServiceImpl implements IAiAgentService
{
    @Autowired
    private AiAgentMapper aiAgentMapper;

    @Override
    public AiAgent selectAiAgentById(Long agentId)
    {
        return aiAgentMapper.selectAiAgentById(agentId);
    }

    @Override
    public List<AiAgent> selectAiAgentList(AiAgent aiAgent)
    {
        return aiAgentMapper.selectAiAgentList(aiAgent);
    }

    @Override
    public int insertAiAgent(AiAgent aiAgent)
    {
        return aiAgentMapper.insertAiAgent(aiAgent);
    }

    @Override
    public int updateAiAgent(AiAgent aiAgent)
    {
        return aiAgentMapper.updateAiAgent(aiAgent);
    }

    @Override
    public int deleteAiAgentByIds(Long[] agentIds)
    {
        return aiAgentMapper.deleteAiAgentByIds(agentIds);
    }
}
