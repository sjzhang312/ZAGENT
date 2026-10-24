import request from '@/utils/request'
import type { AjaxResult, TableDataInfo } from '@/types'

/** AI 智能体配置 */
export interface AiAgent {
  agentId?: number
  agentName?: string
  description?: string
  modelId?: number
  thinkingLevel?: number
  temperature?: number
  maxTokens?: number
  enabledTools?: string
  systemPrompt?: string
  status?: string
  createBy?: string
  createTime?: string
  remark?: string
}

/** 聊天页可用的智能体视图（含模型/服务商信息） */
export interface ChatAgentVo {
  agentId: number
  agentName: string
  description: string
  thinkingLevel: number
  enabledTools: string
  modelId: number
  modelCode: string
  displayName: string
  providerCode: string
  providerName: string
}

// 查询智能体列表
export function listAgent(query?: AiAgent): Promise<TableDataInfo<AiAgent[]>> {
  return request({
    url: '/ai/agent/list',
    method: 'get',
    params: query
  })
}

// 查询智能体详细
export function getAgent(agentId: number): Promise<AjaxResult<AiAgent>> {
  return request({
    url: '/ai/agent/' + agentId,
    method: 'get'
  })
}

// 新增智能体
export function addAgent(data: AiAgent): Promise<AjaxResult> {
  return request({
    url: '/ai/agent',
    method: 'post',
    data: data
  })
}

// 修改智能体
export function updateAgent(data: AiAgent): Promise<AjaxResult> {
  return request({
    url: '/ai/agent',
    method: 'put',
    data: data
  })
}

// 删除智能体
export function delAgent(agentId: number | number[]): Promise<AjaxResult> {
  return request({
    url: '/ai/agent/' + agentId,
    method: 'delete'
  })
}
