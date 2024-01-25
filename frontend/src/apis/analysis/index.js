import request from '@/utils/request'

/**
 * 分析查询
 * @param {RequestPayload} data adsa
 */
export const postAnalysisQuery = data =>
  request({
    url: '/query',
    method: 'post',
    data,
    timeout: 60 * 1000,
  })

/**
 * 获取查询历史记录
 * @param {QueryStringParameters} params
 */
export const getQueryHistory = params =>
  request({
    url: '/dataset/queryLog',
    params,
  })

/**
 * 下载查询
 * @param {QueryStringParameters} params
 * @param {RequestPayload} data
 */
export const downloadQueryResult = (params, data) =>
  request({
    url: '/download',
    method: 'post',
    params,
    data,
    responseType: 'blob',
  })

/**
 * 下载查询历史
 * @param {number} id
 */
export const downloadHistoryResult = id =>
  request({
    url: `/dataset/queryLog/${id}/download`,
    method: 'post',
    responseType: 'blob',
  })

/**
 *  获取看板上筛选枚举值
 * @param {RequestPayload} data
 */
export const postDashboardFilter = data =>
  request({
    url: '/query/dashboard/filter',
    method: 'post',
    data,
  })
