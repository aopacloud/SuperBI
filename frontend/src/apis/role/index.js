import request from '@/utils/request'

/**
 * 获取空间下角色（用户组）
 * @param {QueryStringParameters} params
 */
export const getRoleByWorkspaceId = params =>
  request({
    url: '/authRole',
    params,
  })

/**
 * 获取用户组内用户
 * @param {number} id
 * @param {QueryStringParameters} params
 */
export const getUserByRoleId = (id, params) =>
  request({
    url: `/authRole/${id}/user`,
    params,
  })

/**
 * 向空间内角色的用户
 * @param {number} id 空间ID
 * @param {number[]} data 用户ID
 * @param {QueryStringParameters} params 用户ID
 */
export const posUsersByRoleId = (id, data, params) =>
  request({
    url: `/authRole/${id}/user`,
    method: 'post',
    data,
    params,
  })
