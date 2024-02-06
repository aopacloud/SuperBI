import request from '@/utils/request'

/**
 * 获取角色列表
 * @param {QueryStringParameters} params
 */
export const getRoleList = params =>
  request({
    url: '/sysRole',
    params,
  })

/**
 * 新增角色
 * @param {RequestPayload} data
 */
export const postRole = data =>
  request({
    url: '/sysRole',
    method: 'post',
    data,
  })

/**
 * 更新角色
 * @param {number} id
 * @param {RequestPayload} data
 */
export const updateRole = (id, data) =>
  request({
    url: '/sysRole/' + id,
    method: 'put',
    data,
  })

/**
 * 删除角色
 * @param {number} id
 */
export const deleteRole = id =>
  request({
    url: '/sysRole/' + id,
    method: 'delete',
  })

/**
 * 获取角色的所有菜单权限
 */
export const getMenuTree = () =>
  request({
    url: '/sysRoleResource/getMenuTree',
  })

/**
 * 获取角色的所有功能权限
 */
export const getFunctionTree = () =>
  request({
    url: '/sysRoleResource/getFunctionTree',
  })

/**
 * 获取角色的所有分析权限
 */
export const getAnalyseScope = () =>
  request({
    url: '/sysRoleResource/getAnalyseScope',
  })

/**
 * 获取角色的权限
 * @param {number} id
 */
export const getRolePermission = id =>
  request({
    url: 'sysRoleResource/permission/' + id,
  })

/**
 * 保存角色的权限
 * @param {RequestPayload} data
 */
export const postPermission = data =>
  request({
    url: '/sysRoleResource/postPermission',
    method: 'post',
    data: data,
  })
