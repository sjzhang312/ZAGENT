package com.zagent.ai.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * 适配器基类：负责 ChatClient 流式管线、统一事件发射与工具回调事件包装。
 * 子类只实现「构建 ChatModel」与「构建 ChatOptions」与「思考增量提取」。
 *
 * @author zagent
 */
public abstract class AbstractProviderAdapter implements ProviderAdapter
{
    /** ChatModel 缓存：key = providerId:key:url */
    private final Map<String, ChatModel> modelCache = new ConcurrentHashMap<>();

    protected static final int EVENT_ARG_LIMIT = 2048;

    protected static final int EVENT_RESULT_LIMIT = 4096;

    /** 构建对应厂商的 ChatModel（实现方应使用缓存） */
    protected abstract ChatModel buildChatModel(ChatRequest request, String cacheKey);

    /** 构建本次请求的模型参数 */
    protected abstract ChatOptions buildOptions(ChatRequest request);

    /**
     * 从流式 chunk 中提取思考增量（无则返回 null）。
     * state 为本次流调用的共享状态（串行回调，线程安全），
     * 供需要做累积差分的协议（OpenAI 系 reasoning_content）保存上次累积值。
     */
    protected abstract String extractThinking(ChatResponse chunk, AtomicReference<String> state);

    @Override
    public Flux<UnifiedEvent> stream(ChatRequest request)
    {
        Sinks.Many<UnifiedEvent> out = Sinks.many().unicast().onBackpressureBuffer();

        List<Message> messages = new ArrayList<>();
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank())
        {
            messages.add(new SystemMessage(request.getSystemPrompt()));
        }
        if (request.getHistory() != null)
        {
            messages.addAll(request.getHistory());
        }
        messages.add(new UserMessage(request.getUserMessage()));

        String cacheKey = request.getProviderId() + ":" + request.getApiKey() + ":" + request.getBaseUrl();
        ChatModel chatModel = modelCache.computeIfAbsent(cacheKey, k -> buildChatModel(request, k));
        Prompt prompt = new Prompt(messages, buildOptions(request));
        List<ToolCallback> wrapped = wrapToolCallbacks(request.getToolCallbacks(), out);
        AtomicReference<String> thinkingState = new AtomicReference<>("");

        ChatClient.create(chatModel)
                .prompt(prompt)
                .toolCallbacks(wrapped)
                .stream()
                .chatResponse()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    chunk -> handleChunk(chunk, out, thinkingState),
                    err -> out.tryEmitError(err),
                    () -> out.tryEmitComplete());

        return out.asFlux();
    }

    private void handleChunk(ChatResponse chunk, Sinks.Many<UnifiedEvent> out, AtomicReference<String> thinkingState)
    {
        try
        {
            String thinking = extractThinking(chunk, thinkingState);
            if (thinking != null && !thinking.isEmpty())
            {
                out.tryEmitNext(UnifiedEvent.thinking(thinking));
            }
            if (chunk.getResults() != null)
            {
                for (Generation g : chunk.getResults())
                {
                    AssistantMessage m = g.getOutput();
                    if (m == null)
                    {
                        continue;
                    }
                    String text = m.getText();
                    if (text != null && !text.isEmpty())
                    {
                        out.tryEmitNext(UnifiedEvent.delta(text));
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    /** 包装工具回调：执行前后发射 tool_start / tool_end 事件 */
    protected List<ToolCallback> wrapToolCallbacks(List<ToolCallback> callbacks, Sinks.Many<UnifiedEvent> out)
    {
        List<ToolCallback> wrapped = new ArrayList<>();
        if (callbacks == null)
        {
            return wrapped;
        }
        for (ToolCallback cb : callbacks)
        {
            wrapped.add(new ToolCallback()
            {
                @Override
                public ToolDefinition getToolDefinition()
                {
                    return cb.getToolDefinition();
                }

                @Override
                public String call(String toolInput)
                {
                    String tool = cb.getToolDefinition().name();
                    out.tryEmitNext(UnifiedEvent.toolStart(tool, truncate(toolInput, EVENT_ARG_LIMIT)));
                    long start = System.currentTimeMillis();
                    try
                    {
                        String result = cb.call(toolInput);
                        out.tryEmitNext(UnifiedEvent.toolEnd(tool, "ok", System.currentTimeMillis() - start, truncate(result, EVENT_RESULT_LIMIT)));
                        return result;
                    }
                    catch (Exception e)
                    {
                        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                        out.tryEmitNext(UnifiedEvent.toolEnd(tool, "error", System.currentTimeMillis() - start, truncate(msg, EVENT_RESULT_LIMIT)));
                        throw e;
                    }
                }
            });
        }
        return wrapped;
    }

    protected static String truncate(String s, int limit)
    {
        if (s == null)
        {
            return "";
        }
        return s.length() <= limit ? s : s.substring(0, limit) + "...(截断)";
    }

    /** 归一化 baseUrl：去掉末尾斜杠 */
    protected static String trimBase(String url)
    {
        if (url == null || url.isBlank())
        {
            return null;
        }
        String u = url.trim();
        while (u.endsWith("/"))
        {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }
}
