package com.zagent.ai.mapper;

import java.util.List;
import com.zagent.ai.domain.AiChatSession;

/**
 * 问答会话Mapper
 *
 * @author zagent
 */
public interface AiChatSessionMapper
{
    public AiChatSession selectAiChatSessionById(Long sessionId);

    public List<AiChatSession> selectAiChatSessionList(AiChatSession aiChatSession);

    public int insertAiChatSession(AiChatSession aiChatSession);

    public int updateAiChatSession(AiChatSession aiChatSession);

    public int deleteAiChatSessionByIds(Long[] sessionIds);
}
