export const toLogin = (to, next) => {
  next(`/login?redirect=${to.fullPath}`)
}

export const contactText = '请联系管理员'
