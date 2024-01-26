/** 数据源相关 */

import request from '@/utils/request'

/**
 * 获取数据源
 * @param {QueryStringParameters} params
 */
export const getDatasourceList = params =>
  request({
    url: '/datasource',
    params,
  })

/**
 * 获取数据源详情
 * @param {number} id 数据源id
 */
export const getDatasourceDetailById = id =>
  request({
    url: '/datasource/' + id,
  })

/**
 * 获取数据源详情
 * @param {number} id 数据源id
 */
export const getDatasourceTableById = id =>
  request({
    url: `/datasource/${id}/table`,
  })

/**
 * 添加数据源
 * @param {RequestPayload} data
 */
export const postDatasource = data =>
  request({
    url: '/datasource',
    method: 'post',
    data,
  })

/**
 * 更新数据源
 * @param {number} id
 * @param {RequestPayload} data
 */
export const putDatasource = (id, data) =>
  request({
    url: '/datasource/' + id,
    method: 'put',
    data,
  })

/**
 * 删除数据源
 * @param {number} id
 */
export const deleteDatasourceById = id =>
  request({
    url: '/datasource/' + id,
    method: 'delete',
  })

/**
 * 测试数据库连接
 * @param {RequestPayload} data
 */
export const postTestConnect = data =>
  request({
    url: '/datasource/connectTest',
    method: 'post',
    data,
  })

/**
 * 获取数据源的数据表详情
 * @param {number} datasourceId
 * @param {string} tableName
 */
export const getTableDetail = (datasourceId, tableName) =>
  request({
    url: `/datasource/${datasourceId}/table/${tableName}`,
  })

/**
 * 获取数据源的数据集详情
 * @param {number} datasourceId
 * @param {string} tableName
 */
export const getDatasetDetail = (datasourceId, tableName) =>
  request({
    url: `/datasource/${datasourceId}/table/${tableName}/dataset`,
  })
