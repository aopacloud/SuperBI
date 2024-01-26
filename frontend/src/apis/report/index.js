import request from '@/utils/request'

/**
 * 图表列表
 * @param {QueryStringParameters} query 查询对象
 */
export const getReportList = params =>
  request({
    url: '/report',
    params,
  })

/**
 * 获取图表详情
 * @param {number} id 主键id
 */
export const getDetailById = id =>
  request({
    url: '/report/' + id,
  })

/**
 * 删除图表
 * @param {number} report 图表ID
 */
export const deleteById = id =>
  request({
    url: '/report/' + id,
    method: 'delete',
  })

/**
 * 获取图表详情
 * @param {number} report 图表ID
 */
export const getDetail = id => {
  return request({
    url: '/report/' + id,
  })
}

/**
 * 保存图表
 * @param {RequestPayload} data 图表数据
 */
export const postReport = data =>
  request({
    url: '/report',
    method: 'post',
    data,
  })

/**
 * 更新图表
 * @param {number} id 图表数据
 * @param {RequestPayload} data 图表数据
 */
export const putReport = (id, data) =>
  request({
    url: '/report/' + id,
    method: 'put',
    data,
  })

/**
 * 保存图表另存为
 * @param {RequestPayload} data 图表数据
 */
export const postReportAnother = data =>
  request({
    url: '/report/saveAs',
    method: 'post',
    data,
  })

/**
 * 获取被应用的看板
 * @param {numebr} id 看板ID
 */
export const getDashboardsByReportId = id =>
  request({
    url: `/report/${id}/dashboards`,
  })
