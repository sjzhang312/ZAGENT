import request from '@/utils/request'
import type { AjaxResult, TableDataInfo } from '@/types'

/** AI 模型配置 */
export interface AiModel {
  modelId?: number
  providerId?: number
  modelCode?: string
  displayName?: string
  supportReasoning?: string
  status?: string
  createBy?: string
  createTime?: string
  remark?: string
}

// 查询模型列表
export function listModel(query?: AiModel): Promise<TableDataInfo<AiModel[]>> {
  return request({
    url: '/ai/model/list',
    method: 'get',
    params: query
  })
}

// 查询模型详细
export function getModel(modelId: number): Promise<AjaxResult<AiModel>> {
  return request({
    url: '/ai/model/' + modelId,
    method: 'get'
  })
}

// 新增模型
export function addModel(data: AiModel): Promise<AjaxResult> {
  return request({
    url: '/ai/model',
    method: 'post',
    data: data
  })
}

// 修改模型
export function updateModel(data: AiModel): Promise<AjaxResult> {
  return request({
    url: '/ai/model',
    method: 'put',
    data: data
  })
}

// 删除模型
export function delModel(modelId: number | number[]): Promise<AjaxResult> {
  return request({
    url: '/ai/model/' + modelId,
    method: 'delete'
  })
}
