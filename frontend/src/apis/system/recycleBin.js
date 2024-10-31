import request from '@/utils/request'

/**
 * 获取列表
 * @param {QueryStringParameters} params 查询参数
 */
export const getList = params =>
  request({
    url: '/recycle',
    params
  })

/**
 * 删除
 * @param {QueryStringParameters} params 查询参数
 */
export const deleteByIds = (params, data) =>
  request({
    url: '/recycle/delete',
    method: 'delete',
    params,
    data
  })

/**
 * 恢复
 * @param {QueryStringParameters} params 查询参数
 */
export const postRevert = (params, data) =>
  request({
    url: '/recycle/restore',
    method: 'post',
    params,
    data
  })
