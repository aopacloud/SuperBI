// 授权相关

import request from '@/utils/request'

/**
 * 获取批量授权列表 - 看板
 * @param {QueryStringParameters} params 参数
 * @returns
 */
export const getDashboardAuthorList = params =>
  request({
    url: '/dashboardShare/batch/search',
    params,
  })

/**
 * 看板批量授权
 * @param {RequestPayload} data 参数
 * @returns
 */

export const postDashboardMultiple = data =>
  request({
    url: '/dashboardShare/batch/save',
    method: 'post',
    data,
  })

/**
 * 获取可批量授权的看板列表
 * @param {QueryStringParameters} params 参数
 * @returns
 */
export const getDashboardSharableList = params =>
  request({
    url: '/dashboardShare/batch/canShareDashboards',
    params,
  })

/**
 * 获取可复制权限的看板列表
 * @param {QueryStringParameters} params 参数
 * @returns
 */
export const getDashboardCopiedList = params =>
  request({
    url: '/dashboardShare/batch/canCopyDashboards',
    params,
  })

/**
 * 复制看板授权
 * @param {RequestPayload} data 参数
 */
export const postCopyDashboard = data =>
  request({
    url: '/dashboardShare/batch/copy',
    method: 'post',
    data,
  })

/**
 * 批量删除看板授权
 * @param {RequestPayload} data 参数
 * @returns
 */
export const deleteMultipleDashboard = data =>
  request({
    url: '/dashboardShare/batch/delete',
    method: 'delete',
    data,
  })

/**
 * 获取批量授权列表 - 数据集
 * @param {QueryStringParameters} params 参数
 * @returns
 */
export const getDatasetAuthorList = params =>
  request({
    url: '/datasetAuthorize/batch/search',
    params,
  })

/**
 * 数据集批量授权
 * @param {RequestPayload} data
 * @returns
 */
export const postDatasetMultiple = data =>
  request({
    url: '/datasetAuthorize/batch/save',
    method: 'post',
    data,
  })

/**
 * 数据集批量授权更新
 * @param {RequestPayload} data
 * @returns
 */
export const pustDatasetMultiple = data =>
  request({
    url: '/datasetAuthorize/batch/update',
    method: 'put',
    data,
  })

/**
 * 获取可以批量授权的数据集
 * @param {QueryStringParameters} params
 * @returns
 */
export const getDatasetAuthorizableList = params =>
  request({
    url: '/datasetAuthorize/batch/canAuthorizeDatasets',
    params,
  })

/**
 * 获取可以复制权限的数据集列表
 * @param {QueryStringParameters} params
 * @returns
 */
export const getDatasetCopiedList = params =>
  request({
    url: '/datasetAuthorize/batch/canCopyDatasets',
    params,
  })

/**
 * 获取数据集的交叉字段
 * @param {RequestPayload} data
 * @returns
 */
export const getIntersectionFields = data =>
  request({
    url: '/datasetAuthorize/batch/intersectionFields',
    method: 'post',
    data,
  })

/**
 * 获取数据集的交叉字段的枚举值
 * @param {RequestPayload} data
 * @returns
 */
export const getIntersectionEnums = data =>
  request({
    url: '/datasetAuthorize/batch/field/values/intersection',
    method: 'post',
    data,
  })

/**
 * 复制数据集授权
 * @param {RequestPayload} data
 * @returns
 */
export const postCopyDataset = data =>
  request({
    url: '/datasetAuthorize/batch/copy',
    method: 'post',
    data,
  })

/**
 * 获取看板内数据集的授权详情
 * @param {QueryStringParameters} params
 * @returns
 */
export const getDatasetByDashboardId = params =>
  request({
    url: '/datasetAuthorize/batch/findByDashboard',
    params,
  })

/**
 * 批量删除数据集授权
 * @param {RequestPayload} data 参数
 * @returns
 */
export const deleteMultipleDataset = data =>
  request({
    url: '/datasetAuthorize/batch/delete',
    method: 'delete',
    data,
  })

/**
 * 批量修改数据集授权类型
 * @param {RequestPayload} data 参数
 * @returns
 */
export const updateAuthorizePermission = data =>
  request({
    url: '/datasetAuthorize/batch/updatePermission',
    method: 'put',
    data,
  })
