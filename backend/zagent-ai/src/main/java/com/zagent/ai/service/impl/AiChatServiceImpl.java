package com.zagent.ai.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.alibaba.fastjson2.JSON;
import com.zagent.ai.adapter.ChatRequest;
import com.zagent.ai.adapter.ProviderAdapter;
import com.zagent.ai.adapter.UnifiedEvent;
import com.zagent.ai.domain.AiAgent;
import com.zagent.ai.domain.AiChatMessage;
import com.zagent.ai.domain.AiChatSession;
import com.zagent.ai.domain.AiChatTrace;
import com.zagent.ai.domain.AiModel;
import com.zagent.ai.domain.AiProvider;
import com.zagent.ai.domain.AiSkill;
import com.zagent.ai.domain.AiSkillResource;
import com.zagent.ai.domain.vo.ChatSendBody;
import com.zagent.ai.mapper.AiAgentMapper;
import com.zagent.ai.mapper.AiChatMessageMapper;
import com.zagent.ai.mapper.AiChatSessionMapper;
import com.zagent.ai.mapper.AiChatTraceMapper;
import com.zagent.ai.mapper.AiModelMapper;
import com.zagent.ai.mapper.AiProviderMapper;
import com.zagent.ai.mapper.AiSkillMapper;
import com.zagent.ai.mapper.AiSkillResourceMapper;
import com.zagent.ai.service.IAiChatService;
import com.zagent.ai.tools.AgentToolCallbacks;
import com.zagent.ai.tools.SkillStorageService;
import com.zagent.common.exception.ServiceException;
import com.zagent.common.utils.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.Disposable;

/**
 * AI 问答服务实现：技能解析、历史组装、适配层流式事件分发、SSE 转发与落库。
 *
 * @author zagent
 */
@Service
public class AiChatServiceImpl implements IAiChatService
{
    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private static final Pattern SKILL_PREFIX = Pattern.compile("^(?:/\\S+\\s+)+");

    private static final int HISTORY_LIMIT = 20;

    @Autowired
    private AiAgentMapper agentMapper;

    @Autowired
    private AiModelMapper modelMapper;

    @Autowired
    private AiProviderMapper providerMapper;

    @Autowired
    private AiSkillMapper skillMapper;

    @Autowired
    private AiSkillResourceMapper skillResourceMapper;

    @Autowired
    private SkillStorageService skillStorage;

    @Autowired
    private AiChatSessionMapper sessionMapper;

    @Autowired
    private AiChatMessageMapper messageMapper;

    @Autowired
    private AiChatTraceMapper traceMapper;

    @Autowired
    private AgentToolCallbacks toolCallbacks;

    @Autowired
    private List<ProviderAdapter> adapters;

    @Value("${zagent.ai.maxToolRounds:10}")
    private int maxToolRounds;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "zagent-chat-sse");
        t.setDaemon(true);
        return t;
    });

    @Override
    public SseEmitter send(ChatSendBody body, Long userId, String username)
    {
        if (body == null || StringUtils.isEmpty(body.getContent()))
        {
            throw new ServiceException("消息内容不能为空");
        }
        AiAgent agent = agentMapper.selectAiAgentById(body.getAgentId());
        if (agent == null || !"0".equals(agent.getStatus()))
        {
            throw new ServiceException("智能体不存在或已停用");
        }
        AiModel model = modelMapper.selectAiModelById(agent.getModelId());
        if (model == null || !"0".equals(model.getStatus()))
        {
            throw new ServiceException("智能体绑定的模型不存在或已停用");
        }
        AiProvider provider = providerMapper.selectAiProviderById(model.getProviderId());
        if (provider == null || !"0".equals(provider.getStatus()))
        {
            throw new ServiceException("模型所属服务商不存在或已停用");
        }
        ProviderAdapter adapter = adapters.stream()
                .filter(a -> a.providerCode().equalsIgnoreCase(provider.getProviderCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("不支持的服务商: " + provider.getProviderCode()));

        // ---- 技能解析（消息开头 /技能A /技能B）----
        String content = body.getContent().trim();
        List<String> warnings = new ArrayList<>();
        StringBuilder skillPrompt = new StringBuilder();
        Matcher m = SKILL_PREFIX.matcher(content);
        if (m.find())
        {
            String prefix = m.group();
            content = content.substring(prefix.length()).trim();
            for (String token : prefix.trim().split("\\s+"))
            {
                String name = token.substring(1);
                AiSkill q = new AiSkill();
                q.setSkillName(name);
                List<AiSkill> hits = skillMapper.selectAiSkillList(q);
                if (hits.isEmpty() || !"1".equals(hits.get(0).getEnabled()))
                {
                    warnings.add("技能不存在或未启用: /" + name);
                    continue;
                }
                AiSkill skill = hits.get(0);
                if (skillStorage.exists(skill.getSkillName()))
                {
                    appendPackageSkill(skillPrompt, skill, agent);
                }
                else
                {
                    skillPrompt.append("<skill name=\"").append(skill.getSkillName()).append("\">\n")
                            .append("描述: ").append(StringUtils.nvl(skill.getDescription(), "")).append("\n\n")
                            .append(StringUtils.nvl(skill.getContent(), "")).append("\n</skill>\n\n");
                }
            }
        }
        if (content.isEmpty())
        {
            throw new ServiceException("消息内容不能为空（仅包含技能调用）");
        }

        // ---- 会话 ----
        AiChatSession session;
        if (body.getSessionId() != null)
        {
            session = sessionMapper.selectAiChatSessionById(body.getSessionId());
            if (session == null || !session.getUserId().equals(userId))
            {
                throw new ServiceException("会话不存在");
            }
        }
        else
        {
            session = new AiChatSession();
            session.setSessionName(content.length() > 24 ? content.substring(0, 24) + "..." : content);
            session.setAgentId(agent.getAgentId());
            session.setUserId(userId);
            session.setCreateBy(username);
            sessionMapper.insertAiChatSession(session);
        }

        // ---- 用户消息落库 ----
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(session.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(body.getContent().trim());
        userMsg.setStatus("done");
        messageMapper.insertAiChatMessage(userMsg);

        // ---- 助手消息占位（结束后回填） ----
        AiChatMessage assistantMsg = new AiChatMessage();
        assistantMsg.setSessionId(session.getSessionId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent("");
        assistantMsg.setReasoning("");
        assistantMsg.setStatus("running");
        assistantMsg.setModelDesc(provider.getProviderCode() + "/" + model.getModelCode());
        messageMapper.insertAiChatMessage(assistantMsg);

        // ---- 组装 ChatRequest ----
        String systemPrompt = buildSystemPrompt(agent, skillPrompt.toString());
        ChatRequest request = new ChatRequest();
        request.setProviderId(provider.getProviderId());
        request.setProviderCode(provider.getProviderCode());
        request.setApiKey(provider.getApiKey());
        request.setBaseUrl(provider.getBaseUrl());
        request.setModelCode(model.getModelCode());
        request.setSupportReasoning("1".equals(model.getSupportReasoning()));
        request.setThinkingLevel(agent.getThinkingLevel() == null ? 0 : agent.getThinkingLevel());
        request.setTemperature(agent.getTemperature());
        request.setMaxTokens(agent.getMaxTokens());
        request.setSystemPrompt(systemPrompt);
        request.setHistory(loadHistory(session.getSessionId(), assistantMsg.getMessageId(), userMsg.getMessageId()));
        request.setUserMessage(content);
        request.setToolCallbacks(toolCallbacks.build(agent.getEnabledTools()));
        request.setMaxToolRounds(maxToolRounds);

        // ---- SSE ----
        SseEmitter emitter = new SseEmitter(0L);
        StringBuilder answerBuf = new StringBuilder();
        StringBuilder reasoningBuf = new StringBuilder();
        Map<String, String> pendingArgs = new ConcurrentHashMap<>();
        long start = System.currentTimeMillis();
        final Long assistantMsgId = assistantMsg.getMessageId();
        final Long sessionId = session.getSessionId();

        final Disposable[] disposableRef = new Disposable[1];
        var heartbeat = scheduler.scheduleAtFixedRate(() -> {
            try
            {
                emitter.send(SseEmitter.event().comment("ping"));
            }
            catch (Exception ignored)
            {
                if (disposableRef[0] != null)
                {
                    disposableRef[0].dispose();
                }
            }
        }, 15, 15, TimeUnit.SECONDS);

        Runnable cleanup = () -> heartbeat.cancel(false);

        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> {
            cleanup.run();
            if (disposableRef[0] != null)
            {
                disposableRef[0].dispose();
            }
            emitter.complete();
        });
        emitter.onError(e -> {
            cleanup.run();
            if (disposableRef[0] != null)
            {
                disposableRef[0].dispose();
            }
        });

        for (String w : warnings)
        {
            sendEvent(emitter, UnifiedEvent.warning(w));
        }
        sendEvent(emitter, statusFrame(sessionId, assistantMsgId));

        disposableRef[0] = adapter.stream(request).subscribe(
            event -> {
                switch (event.getType())
                {
                    case "thinking":
                        reasoningBuf.append(event.getText());
                        break;
                    case "delta":
                        answerBuf.append(event.getText());
                        break;
                    case "tool_start":
                        pendingArgs.put(event.getTool(), event.getArgs());
                        break;
                    case "tool_end":
                        persistTrace(assistantMsgId, event, pendingArgs.remove(event.getTool()));
                        break;
                    default:
                        break;
                }
                sendEvent(emitter, event);
            },
            err -> {
                log.error("问答流异常: {}", err.getMessage());
                String msg = err.getMessage() == null ? err.getClass().getSimpleName() : err.getMessage();
                sendEvent(emitter, UnifiedEvent.error(msg));
                finish(emitter, assistantMsgId, answerBuf, reasoningBuf, "error", start, heartbeat, sessionId);
            },
            () -> finish(emitter, assistantMsgId, answerBuf, reasoningBuf, "done", start, heartbeat, sessionId));

        return emitter;
    }

    private UnifiedEvent statusFrame(Long sessionId, Long messageId)
    {
        UnifiedEvent e = new UnifiedEvent();
        e.setType("start");
        e.setMessage(String.valueOf(sessionId));
        e.setMessageId(messageId);
        return e;
    }

    private void finish(SseEmitter emitter, Long messageId, StringBuilder answer, StringBuilder reasoning,
                        String status, long start, java.util.concurrent.Future<?> heartbeat, Long sessionId)
    {
        heartbeat.cancel(false);
        AiChatMessage update = new AiChatMessage();
        update.setMessageId(messageId);
        update.setContent(answer.toString());
        update.setReasoning(reasoning.toString());
        update.setStatus(status);
        update.setElapsedMs(System.currentTimeMillis() - start);
        messageMapper.updateAiChatMessage(update);
        try
        {
            emitter.send(SseEmitter.event().name("done")
                    .data(JSON.toJSONString(Map.of(
                            "type", "done",
                            "sessionId", sessionId,
                            "messageId", messageId,
                            "elapsedMs", System.currentTimeMillis() - start)), MediaType.APPLICATION_JSON));
        }
        catch (Exception ignored)
        {
        }
        emitter.complete();
    }

    private void persistTrace(Long messageId, UnifiedEvent event, String args)
    {
        try
        {
            AiChatTrace trace = new AiChatTrace();
            trace.setMessageId(messageId);
            trace.setSeq(0);
            trace.setToolName(event.getTool());
            trace.setArgs(args);
            trace.setResult(event.getResult());
            trace.setStatus("ok".equals(event.getStatus()) ? "0" : "1");
            trace.setDurationMs(event.getDurationMs());
            traceMapper.insertAiChatTrace(trace);
        }
        catch (Exception e)
        {
            log.warn("trace 落库失败: {}", e.getMessage());
        }
    }

    private void sendEvent(SseEmitter emitter, UnifiedEvent event)
    {
        if (event == null)
        {
            return;
        }
        try
        {
            emitter.send(SseEmitter.event().name(event.getType())
                    .data(JSON.toJSONString(event), MediaType.APPLICATION_JSON));
        }
        catch (Exception ignored)
        {
        }
    }

    /**
     * 技能包注入：正文 + 资源清单 + 四工具使用指引（read/write/edit/bash，按智能体启用的工具过滤）。
     * 技能文件已落盘在沙箱 skills/{技能名}/，模型可直接用工具访问与执行，
     * 工具调用过程会以工具状态帧（read/bash/write/edit）展示给前端。
     */
    private void appendPackageSkill(StringBuilder skillPrompt, AiSkill skill, AiAgent agent)
    {
        String name = skill.getSkillName();
        String main = null;
        try
        {
            main = skillStorage.readMain(name);
        }
        catch (Exception ignored)
        {
        }
        skillPrompt.append("<skill name=\"").append(name).append("\">\n")
                .append("描述: ").append(StringUtils.nvl(skill.getDescription(), "")).append("\n\n")
                .append(main == null ? StringUtils.nvl(skill.getContent(), "") : main).append("\n\n");

        List<AiSkillResource> resources = skillResourceMapper.selectBySkillId(skill.getSkillId());
        if (!resources.isEmpty())
        {
            skillPrompt.append("# 技能附属资源（已放在沙箱 skills/").append(name).append("/ 目录）\n");
            int limit = Math.min(resources.size(), 30);
            for (int i = 0; i < limit; i++)
            {
                skillPrompt.append("- skills/").append(name).append("/").append(resources.get(i).getRelPath()).append('\n');
            }
            if (resources.size() > limit)
            {
                skillPrompt.append("- ...共 ").append(resources.size()).append(" 个文件\n");
            }
            skillPrompt.append('\n');
        }

        Set<String> tools = new HashSet<>();
        if (StringUtils.isNotEmpty(agent.getEnabledTools()))
        {
            tools.addAll(Arrays.asList(agent.getEnabledTools().split(",")));
        }
        StringBuilder guide = new StringBuilder();
        if (tools.contains(AgentToolCallbacks.READ))
        {
            guide.append("- read：查看技能参考文档/脚本内容，如 read skills/").append(name).append("/references/xxx.md\n");
        }
        if (tools.contains(AgentToolCallbacks.BASH))
        {
            guide.append("- bash：执行技能脚本，如 bash python skills/").append(name).append("/scripts/xxx.py 参数\n");
        }
        if (tools.contains(AgentToolCallbacks.WRITE))
        {
            guide.append("- write：把生成产物写入工作目录文件（如 output.drawio）\n");
        }
        if (tools.contains(AgentToolCallbacks.EDIT))
        {
            guide.append("- edit：基于技能模板或产物文件做精确文本修改\n");
        }
        if (guide.length() > 0)
        {
            skillPrompt.append("# 工具使用（技能文件就在你的沙箱工作目录中）\n").append(guide)
                    .append("需要技能细节或脚本能力时，主动调用上述工具完成任务，不要凭空猜测。\n");
        }
        skillPrompt.append("</skill>\n\n");
    }

    private String buildSystemPrompt(AiAgent agent, String skillPrompt)
    {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(agent.getSystemPrompt()))
        {
            sb.append(agent.getSystemPrompt().trim()).append("\n\n");
        }
        if (StringUtils.isNotEmpty(skillPrompt))
        {
            sb.append("# 已启用技能\n").append("以下技能由用户通过 / 调用启用，请严格按技能内容的指引完成任务：\n\n")
              .append(skillPrompt);
        }
        if (StringUtils.isNotEmpty(agent.getEnabledTools()))
        {
            sb.append("# 可用工具\n")
              .append("你可以调用工具 read/write/edit/bash 在沙箱工作目录中读写文件与执行命令；")
              .append("操作完成后用简短语言总结结果。").append("\n");
        }
        return sb.toString();
    }

    private List<Message> loadHistory(Long sessionId, Long assistantMsgId, Long userMsgId)
    {
        AiChatMessage q = new AiChatMessage();
        q.setSessionId(sessionId);
        List<AiChatMessage> all = messageMapper.selectAiChatMessageList(q);
        List<Message> history = new ArrayList<>();
        int from = Math.max(0, all.size() - HISTORY_LIMIT);
        for (int i = from; i < all.size(); i++)
        {
            AiChatMessage msg = all.get(i);
            if (msg.getMessageId().equals(assistantMsgId) || msg.getMessageId().equals(userMsgId))
            {
                continue;
            }
            if (!"done".equals(msg.getStatus()) || StringUtils.isEmpty(msg.getContent()))
            {
                continue;
            }
            if ("user".equals(msg.getRole()))
            {
                history.add(new UserMessage(stripSkillPrefix(msg.getContent())));
            }
            else if ("assistant".equals(msg.getRole()))
            {
                history.add(new AssistantMessage(msg.getContent()));
            }
        }
        return history;
    }

    private String stripSkillPrefix(String content)
    {
        Matcher m = SKILL_PREFIX.matcher(content == null ? "" : content);
        return m.find() ? content.substring(m.group().length()).trim() : (content == null ? "" : content.trim());
    }

    @Override
    public List<Map<String, Object>> listAgents()
    {
        AiAgent q = new AiAgent();
        q.setStatus("0");
        List<AiAgent> agents = agentMapper.selectAiAgentList(q);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiAgent a : agents)
        {
            AiModel model = modelMapper.selectAiModelById(a.getModelId());
            if (model == null)
            {
                continue;
            }
            AiProvider provider = providerMapper.selectAiProviderById(model.getProviderId());
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("agentId", a.getAgentId());
            vo.put("agentName", a.getAgentName());
            vo.put("description", a.getDescription());
            vo.put("thinkingLevel", a.getThinkingLevel());
            vo.put("enabledTools", a.getEnabledTools());
            vo.put("modelId", model.getModelId());
            vo.put("modelCode", model.getModelCode());
            vo.put("displayName", model.getDisplayName());
            vo.put("providerCode", provider == null ? "" : provider.getProviderCode());
            vo.put("providerName", provider == null ? "" : provider.getProviderName());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listSessions(Long userId)
    {
        AiChatSession q = new AiChatSession();
        q.setUserId(userId);
        List<AiChatSession> sessions = sessionMapper.selectAiChatSessionList(q);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiChatSession s : sessions)
        {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("sessionId", s.getSessionId());
            vo.put("sessionName", s.getSessionName());
            vo.put("agentId", s.getAgentId());
            vo.put("createTime", s.getCreateTime() == null ? null : new Date(s.getCreateTime().getTime()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public Map<String, Object> sessionMessages(Long sessionId, Long userId)
    {
        AiChatSession session = sessionMapper.selectAiChatSessionById(sessionId);
        if (session == null || !session.getUserId().equals(userId))
        {
            throw new ServiceException("会话不存在");
        }
        AiChatMessage q = new AiChatMessage();
        q.setSessionId(sessionId);
        List<AiChatMessage> messages = messageMapper.selectAiChatMessageList(q);
        List<Map<String, Object>> list = new ArrayList<>();
        for (AiChatMessage msg : messages)
        {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("messageId", msg.getMessageId());
            vo.put("role", msg.getRole());
            vo.put("content", msg.getContent());
            vo.put("reasoning", msg.getReasoning());
            vo.put("status", msg.getStatus());
            vo.put("elapsedMs", msg.getElapsedMs());
            vo.put("modelDesc", msg.getModelDesc());
            vo.put("createTime", msg.getCreateTime());
            if ("assistant".equals(msg.getRole()))
            {
                AiChatTrace tq = new AiChatTrace();
                tq.setMessageId(msg.getMessageId());
                List<AiChatTrace> traces = traceMapper.selectAiChatTraceList(tq);
                List<Map<String, Object>> tv = new ArrayList<>();
                for (AiChatTrace t : traces)
                {
                    Map<String, Object> tr = new LinkedHashMap<>();
                    tr.put("toolName", t.getToolName());
                    tr.put("args", t.getArgs());
                    tr.put("result", t.getResult());
                    tr.put("status", "0".equals(t.getStatus()) ? "ok" : "error");
                    tr.put("durationMs", t.getDurationMs());
                    tv.add(tr);
                }
                vo.put("traces", tv);
            }
            list.add(vo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("messages", list);
        return result;
    }

    @Override
    public int deleteSessions(Long[] sessionIds, Long userId)
    {
        int count = 0;
        for (Long id : sessionIds)
        {
            AiChatSession session = sessionMapper.selectAiChatSessionById(id);
            if (session == null || !session.getUserId().equals(userId))
            {
                continue;
            }
            messageMapper.deleteAiChatMessageBySessionIds(new Long[] { id });
            count += sessionMapper.deleteAiChatSessionByIds(new Long[] { id });
        }
        return count;
    }
}
