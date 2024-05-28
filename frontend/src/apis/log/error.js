import fetchFn from '@/utils/request/fetch'
import request from '@/utils/request'

// 获取错误原因
export const fetchErrorReason = params =>
  request({ url: '/query/log/summarize', params })

// 获取错误分析
export const fetchErrorAnalysis = body => fetchFn('/query/log/analysis', { body })
