package com.zagent.ai.mapper;

import java.util.List;
import com.zagent.ai.domain.AiChatTrace;

/**
 * 工具调用轨迹Mapper
 *
 * @author zagent
 */
public interface AiChatTraceMapper
{
    public List<AiChatTrace> selectAiChatTraceList(AiChatTrace aiChatTrace);

    public int insertAiChatTrace(AiChatTrace aiChatTrace);
}
