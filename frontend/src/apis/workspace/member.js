import request from '@/utils/request'

/**
 * 获取空间成员
 * @param {QueryStringParameters} query
 */
export const getWorkspaceMembers = params =>
  request({
    url: '/workspace/member',
    params
  })

/**
 * 获取空间所有成员
 * @param {QueryStringParameters} params
 */
export const getWorkspaceAllMembers = params =>
  request({
    url: '/workspace/member',
    params: {
      ...params,
      pageSize: 10000,
      pageNum: 1
    }
  })

/**
 * 添加空间成员
 * @param {RequestPayload} data
 */
export const postWorkspaceMembers = data =>
  request({
    url: '/workspace/member',
    method: 'post',
    data,
    forceWorkspaceId: true // 强制使用工作空间id
  })

/**
 * 更新空间成员
 * @param {number} id 空间id
 * @param {RequestPayload} data
 * @param {QueryStringParameters} params
 */
export const putWorkspaceMembers = (id, data, params) =>
  request({
    url: '/workspace/member/' + id,
    method: 'put',
    data,
    params
  })

/**
 * 删除空间成员
 * @param {number} id
 * @param {QueryStringParameters} params
 */
export const deleteWorkspaceMembers = (id, params) =>
  request({
    url: '/workspace/member/' + id,
    method: 'delete',
    params
  })
