import request from '@/utils/request'

/**
 * 获取空间角色列表
 * @param {QueryStringParameters} params
 */
export const getRoleList = params =>
  request({
    url: '/authRole',
    params,
  })

/**
 * 创建空间角色
 * @param {RequestPayload} data
 */
export const createRole = data =>
  request({
    url: '/authRole',
    method: 'post',
    data,
  })

/**
 * 更新空间角色
 * @param {number} id
 * @param {RequestPayload} data
 */
export const updateRole = (id, data) =>
  request({
    url: '/authRole/' + id,
    method: 'put',
    data,
  })

/**
 * 删除空间角色
 * @param {number} id
 * @param {QueryStringParameters} params
 */
export const deleteRole = (id, params) =>
  request({
    url: '/authRole/' + id,
    method: 'delete',
    params,
  })
