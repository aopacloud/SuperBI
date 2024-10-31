import request from '@/utils/request'

/**
 * 解析excel
 * @param {RequestPayload} data
 * @returns
 */
export const postParseExcel = data =>
  request({
    url: '/excel/parse',
    method: 'post',
    data,
    headers: {
      'content-type': 'multipart/form-data' // 设置内容类型为 JSON
    }
  })
