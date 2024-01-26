export default {
  /**
   * 网页标题
   */
  title: import.meta.env.VITE_APP_TITLE,
  /**
   * 侧边栏主题 深色主题theme-dark，浅色主题theme-light
   */
  sideTheme: 'theme-dark',

  /**
   * 侧边栏背景色
   */
  sideBackgroundColor: '',
  /**
   * 是否系统布局配置
   */
  showSettings: false,

  /**
   * 是否显示顶部导航
   */
  topNav: true,

  hamburger: false,

  /**
   * 是否显示 tagsView
   */
  tagsView: false,

  /**
   * 是否固定头部
   */
  fixedHeader: true,

  /**
   * 是否显示logo
   */
  sidebarLogo: true,

  /**
   * 是否显示动态标题
   */
  dynamicTitle: false,

  /**
   * @type {string | array} 'production' | ['production', 'development']
   * @description Need show err logs component.
   * The default is only used in the production env
   * If you want to also use it in dev, you can pass ['production', 'development']
   */
  errorLog: 'production',

  /**
   * 是否显示面包屑
   */
  showBreadcrumb: false,
}

/**
 * 应用pathname
 */
export const appPath = import.meta.env.VITE_APP_PATH

/**
 * 缓存键前缀 xxx-xxx-
 * 应用 - 环境
 */
export const storagePrefix = `${import.meta.env.VITE_APP_PATH.toUpperCase()}-${import.meta.env.VITE_APP_ENV.toUpperCase()}-`

/**
 * 当前时区偏移量
 */
export const defaultActiveTimeOffset = '+8'

/**
 * 布局
 */
export const UIStyle = {
  navbar: {
    height: '60px',
    backgroundColor: '#030852',
    color: '#fff',
  },
  sidebar: {
    width: '160px',
    backgroundColor: '#030852',

    navs: {
      activeBackgrouncColor: '#1677ff',
      activeColor: '#fff',
      color: 'rgba(255, 255, 255, 0.65)',
    },
  },
}
