import request from '@/utils/request'
import type { AjaxResult, TableDataInfo } from '@/types'

/** AI 服务商配置 */
export interface AiProvider {
  providerId?: number
  providerName?: string
  providerCode?: string
  apiKey?: string
  baseUrl?: string
  status?: string
  createBy?: string
  createTime?: string
  remark?: string
}

// 查询服务商列表
export function listProvider(query?: AiProvider): Promise<TableDataInfo<AiProvider[]>> {
  return request({
    url: '/ai/provider/list',
    method: 'get',
    params: query
  })
}

// 查询服务商详细
export function getProvider(providerId: number): Promise<AjaxResult<AiProvider>> {
  return request({
    url: '/ai/provider/' + providerId,
    method: 'get'
  })
}

// 新增服务商
export function addProvider(data: AiProvider): Promise<AjaxResult> {
  return request({
    url: '/ai/provider',
    method: 'post',
    data: data
  })
}

// 修改服务商
export function updateProvider(data: AiProvider): Promise<AjaxResult> {
  return request({
    url: '/ai/provider',
    method: 'put',
    data: data
  })
}

// 删除服务商
export function delProvider(providerId: number | number[]): Promise<AjaxResult> {
  return request({
    url: '/ai/provider/' + providerId,
    method: 'delete'
  })
}

// 测试连通性并拉取可选模型列表
export function testProvider(providerId: number): Promise<AjaxResult<string[]>> {
  return request({
    url: '/ai/provider/test/' + providerId,
    method: 'get'
  })
}
