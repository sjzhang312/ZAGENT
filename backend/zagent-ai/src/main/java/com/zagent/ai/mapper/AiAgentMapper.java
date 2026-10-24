package com.zagent.ai.mapper;

import java.util.List;
import com.zagent.ai.domain.AiAgent;

/**
 * AiAgentMapper
 *
 * @author zagent
 */
public interface AiAgentMapper
{
    public AiAgent selectAiAgentById(Long agentId);

    public List<AiAgent> selectAiAgentList(AiAgent aiAgent);

    public int insertAiAgent(AiAgent aiAgent);

    public int updateAiAgent(AiAgent aiAgent);

    public int deleteAiAgentByIds(Long[] agentIdss);
}
