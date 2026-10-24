package com.zagent.ai.controller;

import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.zagent.ai.domain.AiSkill;
import com.zagent.ai.service.IAiSkillService;
import com.zagent.common.annotation.Log;
import com.zagent.common.core.controller.BaseController;
import com.zagent.common.core.domain.AjaxResult;
import com.zagent.common.core.page.TableDataInfo;
import com.zagent.common.enums.BusinessType;

/**
 * AI 技能管理
 *
 * @author zagent
 */
@RestController
@RequestMapping("/ai/skill")
public class AiSkillController extends BaseController
{
    @Autowired
    private IAiSkillService skillService;

    @PreAuthorize("@ss.hasPermi('ai:skill:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiSkill aiSkill)
    {
        startPage();
        List<AiSkill> list = skillService.selectAiSkillList(aiSkill);
        return getDataTable(list);
    }

    /** 已启用技能（供智能体绑定下拉与聊天 / 下拉使用） */
    @GetMapping("/enabled")
    public AjaxResult enabled()
    {
        AiSkill q = new AiSkill();
        q.setEnabled("1");
        return success(skillService.selectAiSkillList(q));
    }

    @PreAuthorize("@ss.hasPermi('ai:skill:query')")
    @GetMapping(value = "/{skillId}")
    public AjaxResult getInfo(@PathVariable("skillId") Long skillId)
    {
        return success(skillService.selectAiSkillById(skillId));
    }

    @PreAuthorize("@ss.hasPermi('ai:skill:add')")
    @Log(title = "AI 技能", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AiSkill aiSkill)
    {
        aiSkill.setCreateBy(getUsername());
        return toAjax(skillService.insertAiSkill(aiSkill));
    }

    @PreAuthorize("@ss.hasPermi('ai:skill:edit')")
    @Log(title = "AI 技能", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AiSkill aiSkill)
    {
        aiSkill.setUpdateBy(getUsername());
        return toAjax(skillService.updateAiSkill(aiSkill));
    }

    @PreAuthorize("@ss.hasPermi('ai:skill:remove')")
    @Log(title = "AI 技能", businessType = BusinessType.DELETE)
    @DeleteMapping("/{skillIds}")
    public AjaxResult remove(@PathVariable Long[] skillIds)
    {
        return toAjax(skillService.deleteAiSkillByIds(skillIds));
    }

    /** zip 导入：每个技能一个目录 {name}/SKILL.md（frontmatter: name/description + 正文），兼容根目录散装 *.md */
    @PreAuthorize("@ss.hasPermi('ai:skill:import')")
    @Log(title = "AI 技能导入", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importSkills(@RequestParam("file") MultipartFile file) throws Exception
    {
        List<Map<String, Object>> detail = skillService.importSkills(file);
        return success(detail);
    }
}
