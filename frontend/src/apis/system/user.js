import request from '@/utils/request'

/**
 * 获取系统成员
 * @param {QueryStringParameters} params
 * @returns
 */
export const searchSysUser = params =>
  request({
    url: '/sysUser/list',
    params,
  })

export const checkExistResources = query =>
  request({
    url: `/sysUser/${query.username}/existResources`,
    params: query,
  })

export const transferResources = data =>
  request({
    url: `/sysUser/${data.fromUsername}/transferResources`,
    method: 'put',
    data: data,
  })

export const transferResourcesAndDeleteUser = data =>
  request({
    url: `/sysUser/${data.fromUsername}/transferResources`,
    method: 'delete',
  })

export const resetPassword = data =>
  request({
    url: `/sysUser/${data.username}/password`,
    method: 'put',
    data: data,
    responseType: 'blob',
  })

export const updateUser = data =>
  request({
    url: `/sysUser/${data.username}`,
    method: 'put',
    data: data,
  })

export const addUsers = data =>
  request({
    url: '/sysUser/users',
    method: 'post',
    data: data,
    responseType: 'blob',
  })

export const deleteUser = data =>
  request({
    url: `/sysUser/${data.username}`,
    method: 'delete',
  })
