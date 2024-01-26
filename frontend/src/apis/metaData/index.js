/** 数据源相关 */
import request from '@/utils/request'

/**
 * 获取数据源
 * @param {QueryStringParameters} params
 */
export const getDatabase = params =>
  request({
    url: '/metaData/database',
    params,
  })

/**
 * 根据数据源获取数据表
 * @param {QueryStringParameters} params
 */
export const getTableListByDatabaseItem = params =>
  request({
    url: '/metaData/table',
    params,
  })

/**
 * 根据数据表获取字段
 * @param {QueryStringParameters} params
 */
export const getFieldsByTableItem = params =>
  request({
    url: '/metaData/table/detail',
    params,
  })
