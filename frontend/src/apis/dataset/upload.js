/** 上传相关 */
import request from '@/utils/request'

// 分片上传
export const uploadChunk = (params, data) =>
  request({
    url: '/upload/slice',
    method: 'post',
    params,
    data,
    headers: {
      'content-type': 'multipart/form-data'
    }
  })

// 本地文件上传
export const upload = data =>
  request({
    url: '/upload',
    method: 'post',
    data,
    timeout: 1000 * 60 * 3
  })

/**
 * 从查询平台上传文件
 *
 * @param {{queryId: string, RunId: string}} data 上传文件所需的数据
 * @returns 返回上传结果
 */
export const uploadFromInquiry = params =>
  request({
    url: '/upload/query',
    params
  })

export const getPreview = params => request({ url: '/upload/preview', params })

/**
 * 创建CK表
 *
 * @param {RequestPayload} data 表数据
 * @returns 返回请求结果
 */
export const createCK = (params, data) =>
  request({ url: '/upload/createTable', method: 'post', params, data })
