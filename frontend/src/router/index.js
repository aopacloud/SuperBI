import { createWebHashHistory, createRouter } from 'vue-router'
import Layout from '@/layout/index.vue'
import exeptionRoutes from './exeption'
export { default as dynamicRoutes } from './routes'
import { versionJs } from '@/versions'

/**
 * 路由配置项
 * hidden: true                           // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true               [TODO]  // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 * redirect: noRedirect           [TODO]  // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'                     // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * permissions: ['a:a:a', 'b:b:b']        // 访问路由的权限codes
 * meta : {
    keepAlive: false              [TODO]  // 如果设置为true，则会被 <keep-alive> 缓存(默认 false)
    title: 'title'                        // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                      // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false             [TODO]  // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'    [TODO]  // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const staticRoutes = [
  {
    path: '/',
    name: 'Index',
    component: Layout,
    children: [...exeptionRoutes],
  },
  {
    path: '/redirect',
    component: Layout,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue'),
      },
    ],
  },

  {
    path: '/system',
    name: 'System',
    redirect: '/system/profile',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: '/system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', roles: ['SuperAdmin'] },
      },
      ...versionJs.RouterIndex.systemRoutes,
    ],
  },

  ...versionJs.RouterIndex.staticRoutes,
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: staticRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

export default router
