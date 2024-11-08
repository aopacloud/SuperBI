export const steps = [
  { title: '文件上传', disabled: true },
  { title: '字段设置', disabled: true },
  { title: '数据集信息', disabled: true }
]

export const uploadStatusMap = {
  READY: 'ready',
  SUCCESS: 'success',
  FAIL: 'fail',
  ERROR: 'error',
  DONE: 'done'
}

export { default as uploadReadyImg } from '@/assets/images/uploadReady.png'
export { default as uploadErrorImg } from '@/assets/images/uploadError.png'
export { default as uploadSuccessImg } from '@/assets/images/uploadSuccess.png'

export const uploadTemplateUrl = new URL(
  '/assets/template/模板-多维本地文件上传.xlsx',
  import.meta.url
).href

export const uploadNormUrl =
  'https://data-platform.aopacloud.net/blog-prod/%E5%A4%9A%E7%BB%B4%E5%88%86%E6%9E%90/%E6%93%8D%E4%BD%9C%E6%8C%87%E5%8D%97/%E6%95%B0%E6%8D%AE%E5%88%86%E6%9E%90/%E6%95%B0%E6%8D%AE%E9%9B%86.html#%E5%9B%9B%E6%9C%AC%E5%9C%B0%E6%96%87%E4%BB%B6%E6%95%B0%E6%8D%AE%E5%88%86%E6%9E%90'
