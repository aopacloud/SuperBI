import request from '@/utils/request'

/* 资源管理接口 */

/**
 * 获取资源列表
 * @param {QueryStringParameters} params
 * @returns
 */
export const getResourceList = params =>
  request({ url: '/resource/manage', params })

/**
 * 获取资源中的图表
 * @param {QueryStringParameters} params
 * @returns
 */
export const getResourceReport = params =>
  request({ url: '/resource/manage/report', params })

/**
 * 批量删除
 * @param {QueryStringParameters} params
 * @param {RequestPayload} data
 * @returns
 */
export const deleteByIds = (params, data) =>
  request({ url: '/resource/manage/delete', method: 'delete', params, data })

/**
 * 批量下线
 * @param {QueryStringParameters} params
 * @param {RequestPayload} data
 * @returns
 */
export const postOfflineByIds = (params, data) =>
  request({ url: '/resource/manage/offline', method: 'post', params, data })

/**
 * 批量上线
 * @param {QueryStringParameters} params
 * @param {RequestPayload} data
 * @returns
 */
export const postOnlineByIds = (params, data) =>
  request({ url: '/resource/manage/online', method: 'post', params, data })

/**
 * 批量发布
 * @param {QueryStringParameters} params
 * @param {RequestPayload} data
 * @returns
 */
export const postPublishByIds = (params, data) =>
  request({ url: '/resource/manage/publish', method: 'post', params, data })

/**
 * 批量移动
 * @param {RequestPayload} data
 * @returns
 */
export const postMoveByList = data =>
  request({ url: '/resource/manage/moveResourceList', method: 'post', data })
