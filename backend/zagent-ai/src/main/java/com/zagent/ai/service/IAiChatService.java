package com.zagent.ai.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.zagent.ai.domain.vo.ChatSendBody;

/**
 * AI 问答服务
 *
 * @author zagent
 */
public interface IAiChatService
{
    /** 发送消息并流式返回（SSE），返回给 Controller 的 emitter */
    SseEmitter send(ChatSendBody body, Long userId, String username);

    /** 可用智能体列表（含模型/服务商信息） */
    List<Map<String, Object>> listAgents();

    /** 用户的会话列表 */
    List<Map<String, Object>> listSessions(Long userId);

    /** 会话消息（含工具轨迹，用于历史回放） */
    Map<String, Object> sessionMessages(Long sessionId, Long userId);

    /** 删除会话（连同消息） */
    int deleteSessions(Long[] sessionIds, Long userId);
}
