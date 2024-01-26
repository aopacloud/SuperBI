import request from '@/utils/request'

/**
 * 获取函数列表
 */
export const getFunctionList = () => request({ url: '/functions' })
