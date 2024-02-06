import request, { useLoopRequest } from '@/utils/request'

/**
 * 获取未读消息数量
 */
export const getCount = () =>
  request({
    url: '/notice/unreadCount',
    hideErrorMessage: true,
  })

/**
 * 获取消息列表
 * @param {QueryStringParameters} params
 */
export const getNotificationList = params =>
  request({
    url: '/notice',
    params,
  })

/**
 * 消息已读
 * @param {RequestPayload} data
 */
export const postReaded = data =>
  request({
    url: '/notice/read',
    method: 'post',
    data,
  })
