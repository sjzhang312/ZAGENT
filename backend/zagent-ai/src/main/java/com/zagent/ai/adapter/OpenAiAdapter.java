package com.zagent.ai.adapter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

/**
 * OpenAI 适配器：
 * - 官方 SDK OkHttp 客户端 + Spring AI OpenAiChatModel（兼容 DeepSeek 等中转）
 * - GPT-5 / o 系推理模型：不设 temperature，使用 maxCompletionTokens，思考等级映射 reasoning_effort
 * - 兼容服务的推理内容（reasoning_content）映射到 metadata "reasoningContent"，
 *   官方 issue #6833 表明流式时为累积值 → 本地做差分后再作为增量下发
 *
 * @author zagent
 */
@Component
public class OpenAiAdapter extends AbstractProviderAdapter
{
    public static final String CODE = "openai";

    public static final String DEFAULT_BASE = "https://api.openai.com";

    @Override
    public String providerCode()
    {
        return CODE;
    }

    @Override
    protected ChatModel buildChatModel(ChatRequest request, String cacheKey)
    {
        String base = normalizeBase(request.getBaseUrl());
        // Spring AI 2.0 流式走 async 客户端、非流式走 sync 客户端，
        // 仅设置 openAiClient 时 stream() 会在内部重建 async 客户端并因缺少凭证失败，
        // 因此两个客户端都要显式构建传入
        com.openai.client.OpenAIClient sync = OpenAIOkHttpClient.builder()
                .baseUrl(base)
                .apiKey(request.getApiKey())
                .timeout(Duration.ofSeconds(120))
                .build();
        com.openai.client.OpenAIClientAsync async = OpenAIOkHttpClientAsync.builder()
                .baseUrl(base)
                .apiKey(request.getApiKey())
                .timeout(Duration.ofSeconds(120))
                .build();
        return OpenAiChatModel.builder()
                .openAiClient(sync)
                .openAiClientAsync(async)
                .build();
    }

    @Override
    protected ChatOptions buildOptions(ChatRequest request)
    {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder().model(request.getModelCode());
        int maxTokens = request.getMaxTokens() == null ? 4096 : request.getMaxTokens();
        if (isReasoningModel(request.getModelCode()))
        {
            // 官方推理模型不支持 temperature；使用 maxCompletionTokens
            options.maxCompletionTokens(maxTokens);
            if (request.getThinkingLevel() > 0)
            {
                options.reasoningEffort(effortOf(request.getThinkingLevel()));
            }
        }
        else
        {
            options.maxTokens(maxTokens);
            Double t = request.getTemperature();
            options.temperature(t == null ? 0.7 : t);
        }
        return options.build();
    }

    @Override
    protected String extractThinking(ChatResponse chunk, AtomicReference<String> state)
    {
        if (chunk.getResults() == null || chunk.getResults().isEmpty())
        {
            return null;
        }
        var output = chunk.getResults().get(0).getOutput();
        if (output == null)
        {
            return null;
        }
        var md = output.getMetadata();
        if (md == null || !md.containsKey("reasoningContent"))
        {
            return null;
        }
        Object rc = md.get("reasoningContent");
        if (rc == null)
        {
            return null;
        }
        String accum = rc.toString();
        String last = state.get();
        if (accum.equals(last))
        {
            // 累积值未变化：思考已结束后正文 chunk 仍携带同一快照，跳过避免整段重发
            return null;
        }
        if (accum.length() <= last.length() || !accum.startsWith(last))
        {
            // 服务端未按累积下发（已是增量）或发生重置
            state.set(accum);
            return accum;
        }
        String delta = accum.substring(last.length());
        state.set(accum);
        return delta;
    }

    /** GPT-5 家族与 o 系列为官方推理模型 */
    private boolean isReasoningModel(String modelCode)
    {
        if (modelCode == null)
        {
            return false;
        }
        String m = modelCode.toLowerCase();
        return m.startsWith("gpt-5") || m.startsWith("o1") || m.startsWith("o3") || m.startsWith("o4")
                || m.contains("deepseek-r") || m.contains("deepseek-reasoner");
    }

    private String effortOf(int level)
    {
        switch (level)
        {
            case 1: return "low";
            case 2: return "medium";
            default: return "high";
        }
    }

    /** OpenAI 系 SDK 以含 /v1 的根地址为准，缺失时补上 */
    private String normalizeBase(String url)
    {
        String base = trimBase(url == null || url.isBlank() ? DEFAULT_BASE : url);
        if (!base.endsWith("/v1"))
        {
            base = base + "/v1";
        }
        return base;
    }
}
