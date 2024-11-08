import request from '@/utils/request'

/**
 * 资源移交
 * @param {RequestPayload} data
 */
export const postResourceTransfer = data =>
  request({
    url: '/handOver/execute/auto',
    method: 'post',
    data
  })
