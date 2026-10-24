package com.zagent.ai.mapper;

import java.util.List;
import com.zagent.ai.domain.AiChatMessage;

/**
 * 问答消息Mapper
 *
 * @author zagent
 */
public interface AiChatMessageMapper
{
    public AiChatMessage selectAiChatMessageById(Long messageId);

    public List<AiChatMessage> selectAiChatMessageList(AiChatMessage aiChatMessage);

    public int insertAiChatMessage(AiChatMessage aiChatMessage);

    public int updateAiChatMessage(AiChatMessage aiChatMessage);

    public int deleteAiChatMessageBySessionIds(Long[] sessionIds);
}
