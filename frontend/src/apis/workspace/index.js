import request from '@/utils/request'

/**
 * 获取所有空间
 */
export const getWorkspaceList = () =>
  request({
    url: '/workspace',
  })

/**
 * 获取当前可管理的空间列表
 */
export const getManagableWorkspaceList = () =>
  request({
    url: '/workspace/canManageList',
  })

/**
 * 获取空间详情
 * @param {number} id
 */
export const getOneWorkspace = id =>
  request({
    url: '/workspace/' + id,
  })

/**
 * 创建空间
 * @param {RequestPayload} data
 */
export const createWorkspace = data =>
  request({
    url: '/workspace',
    method: 'post',
    data,
  })

/**
 * 更新空间
 * @param {number} id
 * @param {RequestPayload} data
 */
export const updateWorkspace = (id, data) =>
  request({
    url: '/workspace/' + id,
    method: 'put',
    data,
  })

/**
 * 删除空间
 * @param {number} id
 */
export const deleteWorkspace = id =>
  request({
    url: '/workspace/' + id,
    method: 'delete',
  })

/**
 * 更新空间列表顺序
 * @param {RequestPayload} data
 */
export const updateWorkspaceList = data =>
  request({
    url: '/workspace/sortBy',
    method: 'put',
    data,
  })

/**
 * 获取我有权限的空间
 */
export const getBeloneMeWorkspace = () =>
  request({
    url: '/workspace/belongMe',
  })

/**
 * 获取所属空间的权限资源
 * @param {number} id
 */
export const getPermissionByWorkspaceId = id =>
  request({
    url: `/workspace/${id}/userResource`,
  })
