package com.zagent.ai.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zagent.ai.domain.AiProvider;
import com.zagent.ai.service.IAiProviderService;
import com.zagent.common.annotation.Log;
import com.zagent.common.core.controller.BaseController;
import com.zagent.common.core.domain.AjaxResult;
import com.zagent.common.core.page.TableDataInfo;
import com.zagent.common.enums.BusinessType;

/**
 * AI 服务商配置
 *
 * @author zagent
 */
@RestController
@RequestMapping("/ai/provider")
public class AiProviderController extends BaseController
{
    @Autowired
    private IAiProviderService providerService;

    @PreAuthorize("@ss.hasPermi('ai:provider:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiProvider aiProvider)
    {
        startPage();
        List<AiProvider> list = providerService.selectAiProviderList(aiProvider);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:provider:query')")
    @GetMapping(value = "/{providerId}")
    public AjaxResult getInfo(@PathVariable("providerId") Long providerId)
    {
        return success(providerService.selectAiProviderById(providerId));
    }

    @PreAuthorize("@ss.hasPermi('ai:provider:add')")
    @Log(title = "AI 服务商", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AiProvider aiProvider)
    {
        aiProvider.setCreateBy(getUsername());
        return toAjax(providerService.insertAiProvider(aiProvider));
    }

    @PreAuthorize("@ss.hasPermi('ai:provider:edit')")
    @Log(title = "AI 服务商", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AiProvider aiProvider)
    {
        aiProvider.setUpdateBy(getUsername());
        return toAjax(providerService.updateAiProvider(aiProvider));
    }

    @PreAuthorize("@ss.hasPermi('ai:provider:remove')")
    @Log(title = "AI 服务商", businessType = BusinessType.DELETE)
    @DeleteMapping("/{providerIds}")
    public AjaxResult remove(@PathVariable Long[] providerIds)
    {
        return toAjax(providerService.deleteAiProviderByIds(providerIds));
    }

    /** 测试连通性并返回可选模型列表 */
    @PreAuthorize("@ss.hasPermi('ai:provider:query')")
    @GetMapping(value = "/test/{providerId}")
    public AjaxResult test(@PathVariable("providerId") Long providerId)
    {
        return success(providerService.testConnection(providerId));
    }
}
