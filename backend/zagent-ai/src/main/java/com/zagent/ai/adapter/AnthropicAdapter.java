package com.zagent.ai.adapter;

import java.time.Duration;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClientAsync;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Component;

/**
 * Anthropic 适配器：
 * - 官方 SDK OkHttp 客户端 + Spring AI AnthropicChatModel
 * - 思考等级映射 extended thinking 预算（低 2048 / 中 8196 / 高 16384），
 *   开启思考时 temperature 强制 1.0（Anthropic 约束），预算必须 >= 1024 且 < maxTokens
 * - 流式思考增量经 chunk metadata（thinkingText）提取，与正文天然分离
 *
 * @author zagent
 */
@Component
public class AnthropicAdapter extends AbstractProviderAdapter
{
    public static final String CODE = "anthropic";

    public static final String DEFAULT_BASE = "https://api.anthropic.com";

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
        // 仅设置 anthropicClient 时 stream() 会在内部重建无凭证的 async 客户端，
        // 两个客户端都要显式构建传入
        com.anthropic.client.AnthropicClient sync = AnthropicOkHttpClient.builder()
                .apiKey(request.getApiKey())
                .baseUrl(base)
                .timeout(Duration.ofSeconds(120))
                .build();
        com.anthropic.client.AnthropicClientAsync async = AnthropicOkHttpClientAsync.builder()
                .apiKey(request.getApiKey())
                .baseUrl(base)
                .timeout(Duration.ofSeconds(120))
                .build();
        return AnthropicChatModel.builder()
                .anthropicClient(sync)
                .anthropicClientAsync(async)
                .build();
    }

    @Override
    protected ChatOptions buildOptions(ChatRequest request)
    {
        int maxTokens = request.getMaxTokens() == null ? 4096 : request.getMaxTokens();
        AnthropicChatOptions.Builder options = AnthropicChatOptions.builder()
                .model(request.getModelCode())
                .maxTokens(maxTokens);
        if (request.getThinkingLevel() > 0 && request.isSupportReasoning())
        {
            long budget = budgetOf(request.getThinkingLevel());
            // 预算必须小于 maxTokens，必要时抬高 maxTokens
            if (budget >= maxTokens)
            {
                maxTokens = (int) budget + 2048;
                options = AnthropicChatOptions.builder()
                        .model(request.getModelCode())
                        .maxTokens(maxTokens);
            }
            options.thinkingEnabled(budget);
            // Anthropic 开启思考时 temperature 必须为 1.0
        }
        else
        {
            options.thinkingDisabled();
            Double t = request.getTemperature();
            options.temperature(t == null ? 0.7 : t);
        }
        return options.build();
    }

    @Override
    protected String extractThinking(ChatResponse chunk, java.util.concurrent.atomic.AtomicReference<String> state)
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
        if (md != null && Boolean.TRUE.equals(md.get(AnthropicChatModel.THINKING_METADATA_KEY)))
        {
            Object text = md.get(AnthropicChatModel.THINKING_TEXT_METADATA_KEY);
            return text == null ? null : text.toString();
        }
        return null;
    }

    private long budgetOf(int level)
    {
        switch (level)
        {
            case 1: return 2048;
            case 2: return 8196;
            default: return 16384;
        }
    }

    /** Anthropic SDK 以根地址为准（自动拼 /v1/...），若用户填了 /v1 结尾则去掉 */
    private String normalizeBase(String url)
    {
        String base = trimBase(url == null || url.isBlank() ? DEFAULT_BASE : url);
        if (base.endsWith("/v1"))
        {
            base = base.substring(0, base.length() - 3);
        }
        return base;
    }
}
