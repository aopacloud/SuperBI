/** 授权相关 */
import request from '@/utils/request'

/**
 * 获取授权用户
 * @param {QueryStringParameters} params
 */
export const getAuthorizeUsers = params =>
  request({
    url: '/datasetAuthorize/user',
    params,
  })

/**
 * 获取授权用户组
 * @param {QueryStringParameters} params
 */
export const getAuthorizeGroups = params =>
  request({
    url: '/datasetAuthorize/role',
    params,
  })

/**
 * 获取授权详情
 * @param {number} id
 */
export const getDetailById = id =>
  request({
    url: '/datasetAuthorize/' + id,
  })

/**
 * 获取授权详情(根据数据集id和用户名)
 * @param {QueryStringParameters} params
 */
export const getDetailByDatasetIdAndUsername = params =>
  request({
    url: '/datasetAuthorize/lastOne',
    params,
  })

/**
 * 保存授权
 * @param {RequestPayload} data
 */
export const postAuthorize = data =>
  request({
    url: '/datasetAuthorize',
    method: 'post',
    data,
  })

/**
 * 更新授权
 * @param {number} id
 * @param {RequestPayload} data
 */
export const putAuthorize = (id, data) =>
  request({
    url: '/datasetAuthorize/' + id,
    method: 'put',
    data,
  })

/**
 * 删除授权
 * @param {QueryStringParameters} id
 */
export const deleteAuthorize = id =>
  request({
    url: '/datasetAuthorize/' + id,
    method: 'delete',
  })

/**
 * 续期授权
 * @param {number} id
 * @param {RequestPayload} data
 */
export const renewAuthorize = (id, data) =>
  request({
    url: `/datasetAuthorize/${id}/renewed`,
    method: 'put',
    data,
  })

/**
 * 更新协作权限类型
 * @param {number} id
 * @param {RequestPayload} data
 */
export const putAuthorizeRole = (id, data) =>
  request({
    url: `/datasetAuthorize/${id}/permission`,
    method: 'put',
    data,
  })

/**
 * 取消授权
 * @param {number} id
 */
export const delAuthor = id =>
  request({
    url: `/dataset/privilege/authorize/${id}/cancel`,
    method: 'post',
  })
