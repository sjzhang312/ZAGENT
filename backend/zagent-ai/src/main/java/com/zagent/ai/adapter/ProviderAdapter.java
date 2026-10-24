package com.zagent.ai.adapter;

import reactor.core.publisher.Flux;

/**
 * 服务商适配器 SPI：把某一家 API 格式（anthropic / openai）归一为 UnifiedEvent 流。
 * 新增厂商 = 新增一个实现并注册为 Spring Bean。
 *
 * @author zagent
 */
public interface ProviderAdapter
{
    /** 支持的服务商编码 */
    String providerCode();

    /** 执行流式对话（含工具循环），返回统一事件流；流结束（complete/error）由调用方统一收尾 */
    Flux<UnifiedEvent> stream(ChatRequest request);
}
