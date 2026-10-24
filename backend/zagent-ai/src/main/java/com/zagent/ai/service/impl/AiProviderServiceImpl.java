package com.zagent.ai.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zagent.ai.adapter.AnthropicAdapter;
import com.zagent.ai.adapter.OpenAiAdapter;
import com.zagent.ai.domain.AiProvider;
import com.zagent.ai.mapper.AiProviderMapper;
import com.zagent.ai.service.IAiProviderService;
import com.zagent.common.exception.ServiceException;
import com.zagent.common.utils.StringUtils;

/**
 * 服务商Service业务层处理
 *
 * @author zagent
 */
@Service
public class AiProviderServiceImpl implements IAiProviderService
{
    @Autowired
    private AiProviderMapper aiProviderMapper;

    @Override
    public AiProvider selectAiProviderById(Long providerId)
    {
        return aiProviderMapper.selectAiProviderById(providerId);
    }

    @Override
    public List<AiProvider> selectAiProviderList(AiProvider aiProvider)
    {
        return aiProviderMapper.selectAiProviderList(aiProvider);
    }

    @Override
    public int insertAiProvider(AiProvider aiProvider)
    {
        return aiProviderMapper.insertAiProvider(aiProvider);
    }

    @Override
    public int updateAiProvider(AiProvider aiProvider)
    {
        return aiProviderMapper.updateAiProvider(aiProvider);
    }

    @Override
    public int deleteAiProviderByIds(Long[] providerIds)
    {
        return aiProviderMapper.deleteAiProviderByIds(providerIds);
    }

    /**
     * 测试连通性：GET {base}/v1/models（anthropic 用 x-api-key，openai 用 Bearer），
     * 成功返回可选模型标识列表（便于快速挂模型）。
     */
    @Override
    public List<String> testConnection(Long providerId)
    {
        AiProvider provider = aiProviderMapper.selectAiProviderById(providerId);
        if (provider == null)
        {
            throw new ServiceException("服务商不存在");
        }
        String code = provider.getProviderCode();
        String base = provider.getBaseUrl();
        if (StringUtils.isEmpty(base))
        {
            base = AnthropicAdapter.CODE.equals(code) ? AnthropicAdapter.DEFAULT_BASE : OpenAiAdapter.DEFAULT_BASE;
        }
        base = base.trim();
        while (base.endsWith("/"))
        {
            base = base.substring(0, base.length() - 1);
        }
        String url = base.endsWith("/v1") ? base + "/models" : base + "/v1/models";

        HttpHeaders headers = new HttpHeaders();
        if (AnthropicAdapter.CODE.equals(code))
        {
            headers.set("x-api-key", provider.getApiKey());
            headers.set("anthropic-version", "2023-06-01");
        }
        else
        {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + provider.getApiKey());
        }
        try
        {
            String body = RestClient.create().get().uri(url).headers(h -> h.addAll(headers)).retrieve().body(String.class);
            List<String> models = new ArrayList<>();
            JSONObject json = JSONObject.parseObject(body);
            JSONArray data = json == null ? null : json.getJSONArray("data");
            if (data != null)
            {
                for (int i = 0; i < data.size() && models.size() < 100; i++)
                {
                    JSONObject item = data.getJSONObject(i);
                    if (item != null && item.getString("id") != null)
                    {
                        models.add(item.getString("id"));
                    }
                }
            }
            return models;
        }
        catch (Exception e)
        {
            String msg = e.getMessage();
            throw new ServiceException("连接失败: " + (msg == null ? e.getClass().getSimpleName() : msg));
        }
    }
}
