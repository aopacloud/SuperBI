import Cookies from 'js-cookie'

// const TokenKey = 'Admin-Token'
const TokenKey = 'weauth_token'

const isIP = str =>
  /((2[0-4]\d|25[0-5]|[01]?\d\d?)\.){3}(2[0-4]\d|25[0-5]|[01]?\d\d?)/.test(str)

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token = '') {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  const hostname = window.location.hostname
  let domain = ''
  if (isIP(hostname)) {
    domain = hostname
  } else {
    domain = hostname.split('.').slice(-2).join('.')
  }

  Cookies.remove(TokenKey, { domain })

  return Promise.resolve()
}

export const HEADER_TOKEN = 'Authorization'
