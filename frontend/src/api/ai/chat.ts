import request from '@/utils/request'
import type { AjaxResult } from '@/types'
import type { ChatAgentVo } from './agent'

/** 会话视图 */
export interface ChatSessionVo {
  sessionId: number
  sessionName: string
  agentId: number
  createTime: string
}

/** 工具轨迹（历史回放） */
export interface ChatTraceVo {
  toolName: string
  args: string
  result: string
  status: 'ok' | 'error'
  durationMs: number
}

/** 消息视图（历史回放） */
export interface ChatMessageVo {
  messageId: number
  role: 'user' | 'assistant'
  content: string
  reasoning: string
  status: string
  elapsedMs: number
  modelDesc: string
  createTime: string
  traces?: ChatTraceVo[]
}

/** 可用智能体列表 */
export function listChatAgents(): Promise<AjaxResult<ChatAgentVo[]>> {
  return request({
    url: '/ai/chat/agents',
    method: 'get'
  })
}

/** 会话列表 */
export function listChatSessions(): Promise<AjaxResult<ChatSessionVo[]>> {
  return request({
    url: '/ai/chat/session/list',
    method: 'get'
  })
}

/** 会话消息（含工具轨迹） */
export function listSessionMessages(sessionId: number): Promise<AjaxResult<{ sessionId: number; messages: ChatMessageVo[] }>> {
  return request({
    url: '/ai/chat/session/' + sessionId + '/messages',
    method: 'get'
  })
}

/** 删除会话 */
export function delChatSession(sessionId: number | number[]): Promise<AjaxResult> {
  return request({
    url: '/ai/chat/session/' + sessionId,
    method: 'delete'
  })
}
