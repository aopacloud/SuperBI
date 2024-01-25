import request from '@/utils/request'

/**
 * 获取权限资源
 */
export const getResource = () =>
  request({
    url: '/sysResource',
  })

/**
 * 获取菜单资源
 */
export const getMenuResource = () =>
  request({
    url: '/sysMenu',
  })

/**
 * 获取角色下的权限权限
 * @param {QueryStringParameters} params
 */
export const getRoleResourceByRoleId = params =>
  request({
    url: '/sysRoleResource',
    params,
  })

/**
 * 保存角色权限
 * @param {RequestPayload} data
 */
export const postRoleResource = data =>
  request({
    url: '/sysRoleResource',
    method: 'post',
    data,
  })

/**
 * 保存角色菜单
 * @param {RequestPayload} data
 */
export const postRoleMenu = data =>
  request({
    url: '/sysRoleMenu',
    method: 'post',
    data,
  })

/**
 * 获取角色下的菜单权限
 * @param {QueryStringParameters} params
 */
export const getRoleMenuByRoleId = params =>
  request({
    url: '/sysRoleMenu',
    params,
  })
