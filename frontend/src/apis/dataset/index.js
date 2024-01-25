/** 数据集列表相关 */
import request from '@/utils/request'

/**
 * 获取数据集列表
 * @param {QueryStringParameters} params
 */
export const getDatasetList = params => {
  return request({
    url: '/dataset',
    params,
  })
}

/**
 * 收藏
 * @param {number} id
 */
export const postFavorById = id =>
  request({
    url: `/dataset/${id}/collect`,
    method: 'post',
  })

/**
 * 取消收藏
 * @param {number} id
 */
export const postUnFavorById = id =>
  request({
    url: `/dataset/${id}/cancelCollect`,
    method: 'delete',
  })

/**
 * 获取详情
 * @param {number} id
 */
export const getDetailById = id =>
  request({
    url: `/dataset/${id}`,
  })

/**
 * 获取详情 - 最后一个版本
 * @param {number} id
 */
export const getLastVersionById = id =>
  request({
    url: `/dataset/${id}/version/lastEdit`,
  })

/**
 * 创建
 * @param {RequestPayload} data
 */
export const postDataset = data =>
  request({
    url: '/dataset',
    method: 'post',
    data,
  })

/**
 * 更新
 * @param {number} id
 * @param {RequestPayload} data
 */
export const putDataset = (id, data) => {
  return request({
    url: `/dataset/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 发布
 * @param {number} id
 */
export const postPublishById = id =>
  request({
    url: `/dataset/${id}/publish`,
    method: 'post',
  })

/**
 * 字段校验
 * @param {RequestPayload} data
 */
export const validateDatasetField = data =>
  request({
    url: '/dataset/field/check',
    method: 'post',
    data,
  })

/**
 * 数据集设置
 * @param {number} id
 * @param {RequestPayload} data
 */
export const postSet = (id, data) =>
  request({
    url: `/dataset/${id}/setting`,
    method: 'post',
    data,
  })

/**
 * 导出
 * @param {number} id
 */
export const exportById = id =>
  request({
    url: `/dataset/${id}/download`,
    method: 'post',
    responseType: 'blob',
  })

/**
 * 下线
 * @param {number} id
 */
export const postOfflineById = id =>
  request({
    url: `/dataset/${id}/offline`,
    method: 'post',
  })

/**
 * 上线
 * @param {number} id
 */
export const postOnlineById = id =>
  request({
    url: `/dataset/${id}/offline`,
    method: 'post',
  })

/**
 * 删除
 * @param {number} id
 */
export const deleteById = id =>
  request({
    url: '/dataset/' + id,
    method: 'delete',
  })
