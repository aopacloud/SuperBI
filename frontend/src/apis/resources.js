import request from '@/utils/request'

/**
 * 获取门户配置的资源
 */
export const getAdminResource = () =>
  request({
    url: '/platform/resources',
  })
