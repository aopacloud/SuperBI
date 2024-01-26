import request from '@/utils/request'

/**
 * 登录方法
 * @param {RequestPayload} data
 * @returns
 */
export const login = data =>
  request({
    url: '/login',
    headers: {
      token: false,
    },
    method: 'post',
    data,
  })

/**
 * 获取用户详细信息
 */
export const getInfo = () =>
  request({
    url: '/sysUser/whoami',
  })

/**
 * 退出方法
 */
export const logout = () =>
  request({
    url: '/logout',
    method: 'post',
  })

/**
 * 获取验证码
 */
export const getCodeImg = () =>
  request({
    url: '/captchaImage',
    headers: {
      token: false,
    },
  })
