import request from '@/utils/request'

/**
 * 我的申请列表
 * @param {QueryStringParameters} params
 */
export const myApplyList = params =>
  request({
    url: '/datasetApply/myApply',
    params,
  })

/**
 * 我的审批列表
 * @param {QueryStringParameters} params
 */
export const myApproveList = params =>
  request({
    url: '/datasetApply/myApprove',
    params,
  })

/**
 * 申请管理列表
 * @param {QueryStringParameters} params
 */
export const applyManageList = params =>
  request({
    url: '/datasetApply/applyManage',
    params,
  })

/**
 * 根据ID获取详情
 * @param {number} id
 */
export const getOne = id =>
  request({
    url: '/datasetApply/' + id,
  })

/**
 * 发起申请
 * @param {RequestPayload} data
 */
export const save = data =>
  request({
    url: '/datasetApply',
    method: 'post',
    data,
  })

/**
 * 撤销申请
 * @param {number} id
 */
export const revoke = id =>
  request({
    url: '/datasetApply/' + id + '/revoke',
    method: 'post',
  })

/**
 * 授权驳回
 * @param {number} id
 * @param {RequestPayload} data
 */
export const postAuthorizeReject = (id, data) =>
  request({
    url: '/datasetApply/' + id + '/reject',
    method: 'post',
    data,
  })

/**
 * 申请驳回
 * @param {number} id
 * @param {RequestPayload} data
 */
export const postApplyReject = (id, data) =>
  request({
    url: '/datasetApply/' + id + '/disagree',
    method: 'post',
    data,
  })

/**
 * 通过申请
 * @param {number} id
 */
export const pass = id =>
  request({
    url: '/datasetApply/' + id + '/pass',
    method: 'post',
  })
