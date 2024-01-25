/** 文件夹相关 */
import request from '@/utils/request'

/**
 * 获取文件夹
 * @param {QueryStringParameters} params
 */
export const getDirectory = params =>
  request({
    url: '/folder/tree',
    params,
  })

/**
 * 获取文件夹(非数量)
 * @param {QueryStringParameters} params
 */
export const getDirectoryWithoutCount = params =>
  request({
    url: '/folder/treeWithoutCount',
    params,
  })

/**
 * 新增文件夹
 * @param {RequestPayload} data
 */
export const postDirectory = data =>
  request({
    url: '/folder',
    method: 'post',
    data,
  })

/**
 * 更新文件夹
 * @param {number} id
 * @param {RequestPayload} data
 */
export const putDirectory = (id, data) =>
  request({
    url: `/folder/${id}`,
    method: 'put',
    data,
  })

/**
 * 删除文件夹
 * @param {number} id
 */
export const delDirectory = id =>
  request({
    url: `/folder/${id}`,
    method: 'delete',
  })

/**
 * 移动至文件夹
 * @param {RequestPayload} data
 */
export const moveDirectory = data =>
  request({
    url: '/folder/moveResource',
    method: 'post',
    data,
  })
