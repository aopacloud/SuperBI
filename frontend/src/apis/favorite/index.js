import request from '@/utils/request'

/**
 * 收藏
 * @param {RequestPayload} data
 */
export const postFavorite = data =>
  request({
    url: '/favorite',
    method: 'post',
    data,
  })

/**
 * 取消收藏
 * @param {RequestPayload} data
 */
export const postUnFavorite = data =>
  request({
    url: '/favorite/cancel',
    method: 'delete',
    data,
  })
