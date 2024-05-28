/**
 * 看板相关 接口
 * @return {Promise}
 */

import request from '@/utils/request'

/**
 * 获取列表
 * @param {QueryStringParameters} params 查询参数
 */
export const getDashboardList = params =>
  request({
    url: '/dashboard',
    params,
  })

/**
 * 获取详情
 * @param {number} id
 */
export const getDetailById = id =>
  request({
    url: '/dashboard/' + id,
  })

/**
 * 获取最后一个编辑版本的详情
 * @param {number} id
 */
export const getLastVersionById = id =>
  request({
    url: '/dashboard/' + id + '/version/lastEdit',
  })

/**
 * 保存
 * @param {RequestPayload} data
 */
export const postSave = data =>
  request({
    url: '/dashboard',
    method: 'post',
    data,
  })

/**
 * 更新
 * @param {number} id
 * @param {RequestPayload} data
 */
export const putUpdate = (id, data) =>
  request({
    url: '/dashboard/' + id,
    method: 'put',
    data,
  })

/**
 * 删除
 * @param {number} id
 */
export const deleteById = id =>
  request({
    url: '/dashboard/' + id,
    method: 'delete',
  })

/**
 * 发布
 * @param {number} id
 */
export const postPublishById = id =>
  request({
    url: '/dashboard/' + id + '/publish',
    method: 'post',
  })

/**
 * 上线
 * @param {number} id
 */
export const postOnlineById = id =>
  request({
    url: '/dashboard/' + id + '/online',
    method: 'post',
  })

/**
 * 下线
 * @param {number} id
 */
export const postOfflineById = id =>
  request({
    url: '/dashboard/' + id + '/offline',
    method: 'post',
  })

/**
 * 获取看板共享的用户
 * @param {QueryStringParameters} params 请求参数
 */
export const getSharedUser = params =>
  request({
    url: '/dashboardShare/users',
    params,
  })

/**
 * 获取看板共享的用户组
 *  @param {QueryStringParameters} params 请求参数
 */
export const getSharedGroup = params =>
  request({
    url: '/dashboardShare/roles',
    params,
  })

/**
 * 设置共享
 * @param {RequestPayload} data
 */
export const postShare = data =>
  request({
    url: '/dashboardShare',
    method: 'post',
    data,
  })

/**
 * 修改共享权限
 * @param {RequestPayload} data
 */
export const putShare = (id, data) =>
  request({
    url: `/dashboardShare/${id}`,
    method: 'put',
    data,
  })

/**
 * 删除共享权限
 * @param {number} id
 */
export const deleteShareRecordById = id =>
  request({
    url: `/dashboardShare/${id}`,
    method: 'delete',
  })

/**
 * 取消看板共享
 * @param {number} id
 */
export const deleteShareById = params =>
  request({
    url: '/dashboardShare/clear',
    method: 'delete',
    params,
  })

/**
 * 获取看板推送设置
 * @param {QueryStringParameters} params
 */
export const getPushSettingById = params =>
  request({
    url: '/dashboard/pushSetting',
    params,
  })

/**
 * 开启看板设置
 * @param {number} id 看板ID
 */
export const postPushSettingActiveById = id =>
  request({
    url: `/dashboard/pushSetting/${id}/on`,
    method: 'post',
  })

/**
 * 关闭看板设置
 * @param {number} id 看板ID
 */
export const postPushSettingInActiveById = id =>
  request({
    url: `/dashboard/pushSetting/${id}/off`,
    method: 'post',
  })

/**
 * 校验频道
 * @param {RequestPayload} data
 */
export const validateChannel = data =>
  request({
    url: '/dashboard/pushSetting/channel/validate',
    method: 'post',
    data,
  })

/**
 * 保存推送设置
 * @param {RequestPayload} data
 */
export const postPushSetting = data =>
  request({
    url: '/dashboard/pushSetting',
    method: 'post',
    data,
  })

/**
 * 更新推送设置
 * @param {number} id
 * @param {RequestPayload} data
 */
export const putPushSetting = (id, data) =>
  request({
    url: `/dashboard/pushSetting/${id}`,
    method: 'put',
    data,
  })

/**
 * 更新推送设置
 * @param {QueryStringParameters} params
 */
export const getDasboardsByReportId = params =>
  request({
    url: '/dashboard/byReport',
    params,
  })

/**
 * 更新看板自动刷新配置
 * @param {number} id
 * @param {RequestPayload} data
 * @returns
 */
export const postRefreshIntervalById = (id, data) =>
  request({
    url: `/dashboard/${id}/refreshInterval`,
    method: 'post',
    data,
  })

/**
 * 更新列表可见性
 * @param {number} id 列表ID
 * @param {RequestPayload} data 可见性数据
 * @returns 返回请求结果
 */
export const updateListVisibility = (id, data) =>
  request({
    url: `/dashboard/${id}/visibility`,
    method: 'post',
    data,
  })
