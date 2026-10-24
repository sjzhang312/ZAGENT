package com.zagent.ai.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.zagent.ai.domain.vo.ChatSendBody;
import com.zagent.ai.service.IAiChatService;
import com.zagent.common.core.controller.BaseController;
import com.zagent.common.core.domain.AjaxResult;
import com.zagent.common.utils.SecurityUtils;

/**
 * AI 问答中心
 *
 * @author zagent
 */
@RestController
@RequestMapping("/ai/chat")
public class AiChatController extends BaseController
{
    @Autowired
    private IAiChatService chatService;

    /** 可用智能体（含模型/服务商信息） */
    @GetMapping("/agents")
    public AjaxResult agents()
    {
        return success(chatService.listAgents());
    }

    /** 当前用户会话列表 */
    @GetMapping("/session/list")
    public AjaxResult sessions()
    {
        return success(chatService.listSessions(SecurityUtils.getUserId()));
    }

    /** 会话消息（含工具轨迹，历史回放） */
    @GetMapping("/session/{sessionId}/messages")
    public AjaxResult messages(@PathVariable("sessionId") Long sessionId)
    {
        return success(chatService.sessionMessages(sessionId, SecurityUtils.getUserId()));
    }

    /** 删除会话（连同消息与轨迹） */
    @PreAuthorize("@ss.hasPermi('ai:chat:list')")
    @DeleteMapping("/session/{sessionIds}")
    public AjaxResult remove(@PathVariable Long[] sessionIds)
    {
        return toAjax(chatService.deleteSessions(sessionIds, SecurityUtils.getUserId()));
    }

    /** 发送消息（SSE 流式返回：thinking/delta/tool_start/tool_end/warning/error/done） */
    @PreAuthorize("@ss.hasPermi('ai:chat:chat')")
    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@RequestBody ChatSendBody body)
    {
        return chatService.send(body, SecurityUtils.getUserId(), SecurityUtils.getUsername());
    }
}
