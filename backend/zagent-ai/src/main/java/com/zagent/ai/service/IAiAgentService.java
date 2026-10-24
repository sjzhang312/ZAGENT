package com.zagent.ai.service;

import java.util.List;
import com.zagent.ai.domain.AiAgent;

/**
 * 智能体Service接口
 *
 * @author zagent
 */
public interface IAiAgentService
{
    public AiAgent selectAiAgentById(Long agentId);

    public List<AiAgent> selectAiAgentList(AiAgent aiAgent);

    public int insertAiAgent(AiAgent aiAgent);

    public int updateAiAgent(AiAgent aiAgent);

    public int deleteAiAgentByIds(Long[] agentIds);
}
