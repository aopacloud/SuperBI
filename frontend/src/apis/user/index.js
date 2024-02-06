import request from '@/utils/request'

/**
 * 获取所有用户
 * @param {QueryStringParameters} params
 */
export const getAllUser = params =>
  request({
    url: '/sysUser/list',
    params,
  })

/**
 * 获取空间下用户
 * @param {QueryStringParameters} params
 */
export const getUserByWorkspaceId = params =>
  request({
    url: '/workspace/member',
    params,
  })
