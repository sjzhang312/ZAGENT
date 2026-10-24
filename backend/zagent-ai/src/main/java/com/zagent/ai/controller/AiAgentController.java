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
import com.zagent.ai.domain.AiAgent;
import com.zagent.ai.service.IAiAgentService;
import com.zagent.common.annotation.Log;
import com.zagent.common.core.controller.BaseController;
import com.zagent.common.core.domain.AjaxResult;
import com.zagent.common.core.page.TableDataInfo;
import com.zagent.common.enums.BusinessType;

/**
 * AI 智能体配置
 *
 * @author zagent
 */
@RestController
@RequestMapping("/ai/agent")
public class AiAgentController extends BaseController
{
    @Autowired
    private IAiAgentService agentService;

    @PreAuthorize("@ss.hasPermi('ai:agent:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiAgent aiAgent)
    {
        startPage();
        List<AiAgent> list = agentService.selectAiAgentList(aiAgent);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:agent:query')")
    @GetMapping(value = "/{agentId}")
    public AjaxResult getInfo(@PathVariable("agentId") Long agentId)
    {
        return success(agentService.selectAiAgentById(agentId));
    }

    @PreAuthorize("@ss.hasPermi('ai:agent:add')")
    @Log(title = "AI 智能体", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AiAgent aiAgent)
    {
        aiAgent.setCreateBy(getUsername());
        return toAjax(agentService.insertAiAgent(aiAgent));
    }

    @PreAuthorize("@ss.hasPermi('ai:agent:edit')")
    @Log(title = "AI 智能体", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AiAgent aiAgent)
    {
        aiAgent.setUpdateBy(getUsername());
        return toAjax(agentService.updateAiAgent(aiAgent));
    }

    @PreAuthorize("@ss.hasPermi('ai:agent:remove')")
    @Log(title = "AI 智能体", businessType = BusinessType.DELETE)
    @DeleteMapping("/{agentIds}")
    public AjaxResult remove(@PathVariable Long[] agentIds)
    {
        return toAjax(agentService.deleteAiAgentByIds(agentIds));
    }
}
