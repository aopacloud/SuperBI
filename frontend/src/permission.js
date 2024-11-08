import router from './router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/token'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useResourceStore from '@/store/modules/resource'
import { hasRoleAccessWithRoute } from '@/router/utils'
import { versionJs } from '@/versions'
import setEntry, { clearEntryQuery } from './entry'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login']

// 是否有访问权限
let canAccess = true

// 检测路由的权限
const checkRoutePermission = route => {
  const permissions = route.meta?.permissions

  if (!hasRoleAccessWithRoute(route)) return false

  if (!Array.isArray(permissions) || !permissions.length === 0) return true

  return useUserStore().hasPermission(permissions)
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  canAccess = true
  if (!getToken()) {
    // 没有token
    if (whiteList.indexOf(to.path) !== -1) {
      // 在免登录白名单，直接进入
      next()
    } else {
      versionJs.Permission.toLogin(to, next)
    }
  } else {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      setEntry(to)
      if (!useResourceStore().loaded) {
        useResourceStore()
          .fetchResource()
          .then(async () => {
            next({ ...to, replace: true })
          })
          .catch(error => {
            console.error('获取用户信息和资源信息错误', error)
            next(`/login?redirect=${to.fullPath}`)
          })
      } else {
        // 首页默认为有权限的第一个路由
        if (to.path === '/') {
          const first = useResourceStore()
            .resources.sort((a, b) => a.sort - b.sort)
            .filter(t => t.url)[0]

          if (!first) {
            canAccess = false

            next()
          } else {
            if (first.children && first.children.length) {
              next(first.children[0].url)
            } else {
              next(first.url)
            }
          }
        } else {
          // 检查路由权限
          canAccess = checkRoutePermission(to)

          next()
        }
      }
    }
  }
})

router.afterEach(a => {
  clearEntryQuery(a)

  NProgress.done()

  if (!canAccess) {
    useAppStore().setUnAccessableInfo(
      `未开通系统权限，${versionJs.Permission.contactText}`
    )
    useAppStore().setUnAccessable(true)
  } else {
    useAppStore().setUnAccessable(false)
  }
})
