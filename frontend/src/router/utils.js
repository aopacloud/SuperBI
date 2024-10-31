import router, { dynamicRoutes } from '@/router'
import { versionJs } from '@/versions'
import { flat, unique } from 'common/utils/help'
import useUserStore from '@/store/modules/user'

/**
 * 路由是否有角色权限
 * @param {RouteConfig} route
 * @returns {boolean}
 */
export const hasRoleAccessWithRoute = route => {
  const roles = route.meta.roles || []

  if (!roles.length) return true

  return roles.some(t => useUserStore().roles.includes(t))
}

/**
 * 注册动态路由
 * @param {Array<ResouceMenu>} resources
 * @returns {RegistableResouces}
 */
export const registeDynamicRoutes = resources => {
  const accessableRoutes = flat(resources).reduce((acc, cur) => {
    // 从资源中匹配路由表的路由, 或者有角色权限的路由
    const item = [
      ...dynamicRoutes,
      ...versionJs.RouterIndex.dynamicRoutes
    ].filter(dr => {
      return dr.path.startsWith(cur.url) || dr.alias === cur.url
    })

    return acc.concat(item)
  }, [])

  const uniqueRoutes = unique(accessableRoutes, 'path')

  register(uniqueRoutes)

  return uniqueRoutes
}

/**
 * 注册路由
 * @param {array} accessableRoutes 有权限的资源
 * @param {string} parentRouteName 父级路由name
 */
function register(accessableRoutes, parentRouteName = 'Index') {
  accessableRoutes.forEach(route => {
    if (!route.matched) {
      if (route.meta.parentRouteName === null) {
        router.addRoute(route)
      } else {
        router.addRoute(parentRouteName, route)
      }

      route.matched = true

      if (route.meta?.related?.length) {
        const relatedRoutes = route.meta.related.map(r => {
          return [
            ...dynamicRoutes,
            ...versionJs.RouterIndex.dynamicRoutes
          ].find(rt => rt.name === r)
        })

        register(relatedRoutes)
      }
    }
  })
}

export const transformResourceWithRoute = resources => {
  return resources.map(r => {
    const route =
      [...dynamicRoutes, ...versionJs.RouterIndex.dynamicRoutes].find(
        rt => rt.path === r.url || rt.alias === r.url
      ) || {}

    return {
      ...r,
      children: transformResourceWithRoute(r.children),
      redirect: route.redirect,
      routeName: route.name,
      meta: route.meta
    }
  })
}
