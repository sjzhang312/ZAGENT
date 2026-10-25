import request from '@/utils/request'
import type { AjaxResult, TableDataInfo } from '@/types'

/** AI 技能附属资源（技能包文件，落盘于沙箱 skills/{技能名}/） */
export interface SkillResource {
  resourceId?: number
  skillId?: number
  relPath?: string
  fileSize?: number
}

/** AI 技能 */
export interface AiSkill {
  skillId?: number
  skillName?: string
  description?: string
  content?: string
  enabled?: string
  createBy?: string
  createTime?: string
  remark?: string
  /** 技能包附属资源（详情接口回填） */
  resources?: SkillResource[]
}

/** zip 导入明细行 */
export interface SkillImportRow {
  name: string
  action: string
  description: string
  resourceCount?: number
}

// 查询技能列表
export function listSkill(query?: AiSkill): Promise<TableDataInfo<AiSkill[]>> {
  return request({
    url: '/ai/skill/list',
    method: 'get',
    params: query
  })
}

// 已启用技能（供绑定下拉与聊天 / 下拉）
export function listEnabledSkill(): Promise<AjaxResult<AiSkill[]>> {
  return request({
    url: '/ai/skill/enabled',
    method: 'get'
  })
}

// 查询技能详细
export function getSkill(skillId: number): Promise<AjaxResult<AiSkill>> {
  return request({
    url: '/ai/skill/' + skillId,
    method: 'get'
  })
}

// 新增技能
export function addSkill(data: AiSkill): Promise<AjaxResult> {
  return request({
    url: '/ai/skill',
    method: 'post',
    data: data
  })
}

// 修改技能
export function updateSkill(data: AiSkill): Promise<AjaxResult> {
  return request({
    url: '/ai/skill',
    method: 'put',
    data: data
  })
}

// 删除技能
export function delSkill(skillId: number | number[]): Promise<AjaxResult> {
  return request({
    url: '/ai/skill/' + skillId,
    method: 'delete'
  })
}

// zip 导入技能
export function importSkill(file: File): Promise<AjaxResult<SkillImportRow[]>> {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/ai/skill/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
