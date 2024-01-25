/** 申请相关 */
import request from '@/utils/request'

/**
 * 申请
 * @param {RequestPayload} data
 */
export const postApply = data =>
  request({
    url: '/datasetApply',
    method: 'post',
    data,
  })

/**
 * 申请通过
 * @param {number} id
 */
export const postPassApply = id =>
  request({
    url: `/datasetApply/${id}/pass`,
    method: 'post',
  })

/**
 * 获取授权消息数量
 */
export const getAuthorizeMesage = () =>
  request({
    url: '/datasetApply/applyMessage',
    hideErrorMessage: true,
  })
