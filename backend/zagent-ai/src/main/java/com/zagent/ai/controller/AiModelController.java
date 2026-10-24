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
import com.zagent.ai.domain.AiModel;
import com.zagent.ai.service.IAiModelService;
import com.zagent.common.annotation.Log;
import com.zagent.common.core.controller.BaseController;
import com.zagent.common.core.domain.AjaxResult;
import com.zagent.common.core.page.TableDataInfo;
import com.zagent.common.enums.BusinessType;

/**
 * AI 模型配置
 *
 * @author zagent
 */
@RestController
@RequestMapping("/ai/model")
public class AiModelController extends BaseController
{
    @Autowired
    private IAiModelService modelService;

    @PreAuthorize("@ss.hasPermi('ai:model:query')")
    @GetMapping("/list")
    public TableDataInfo list(AiModel aiModel)
    {
        startPage();
        List<AiModel> list = modelService.selectAiModelList(aiModel);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:model:query')")
    @GetMapping(value = "/{modelId}")
    public AjaxResult getInfo(@PathVariable("modelId") Long modelId)
    {
        return success(modelService.selectAiModelById(modelId));
    }

    @PreAuthorize("@ss.hasPermi('ai:model:add')")
    @Log(title = "AI 模型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AiModel aiModel)
    {
        aiModel.setCreateBy(getUsername());
        return toAjax(modelService.insertAiModel(aiModel));
    }

    @PreAuthorize("@ss.hasPermi('ai:model:edit')")
    @Log(title = "AI 模型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AiModel aiModel)
    {
        aiModel.setUpdateBy(getUsername());
        return toAjax(modelService.updateAiModel(aiModel));
    }

    @PreAuthorize("@ss.hasPermi('ai:model:remove')")
    @Log(title = "AI 模型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{modelIds}")
    public AjaxResult remove(@PathVariable Long[] modelIds)
    {
        return toAjax(modelService.deleteAiModelByIds(modelIds));
    }
}
