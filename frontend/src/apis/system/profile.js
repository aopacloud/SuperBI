import request from '@/utils/request'

/**
 * 上传头像
 * @param {RequestPayload} data
 */
export const postAvatar = data => {
  return request({
    url: '/sysProfile/avatar',
    method: 'post',
    data,
    headers: {
      'Content-type': 'multipart/form-data',
    },
  })
}

/**
 * 更新密码
 * @param {RequestPayload} data
 */
export const putPassword = data => {
  return request({
    url: 'sysProfile/password',
    method: 'put',
    data,
  })
}

/**
 * 更新邮箱
 * @param {RequestPayload} data
 */
export const putEmail = data => {
  return request({
    url: 'sysProfile/email',
    method: 'put',
    data,
  })
}

/**
 * 更新手机号
 * @param {RequestPayload} data
 */
export const putMobile = data => {
  return request({
    url: 'sysProfile/mobile',
    method: 'put',
    data: data,
  })
}
