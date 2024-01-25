import viteCDNPlugin from 'vite-plugin-cdn-import'

// （）Uncaught TypeError: Failed to resolve module specifier "vue". Relative references must start with either "/", "./", or "../".

export default function createCDN() {
  return viteCDNPlugin({
    // 需要 CDN 加速的模块
    modules: [
      // {
      //   name: 'vue',
      //   var: 'Vue',
      //   path: 'https://unpkg.com/vue@3/dist/vue.global.js',
      // },
      // {
      //   name: 'vue-router',
      //   var: 'VueRouter',
      //   path: 'https://unpkg.com/vue-router@4.2.5/dist/vue-router.global.js',
      // },
      // {
      //   name: 'vue-demi', // 安装vue-demi并导入 因为pinia中有用vue依赖中的vue-demi
      //   var: 'VueDemi',
      //   path: 'lib/index.iife.min.js',
      // },
      // {
      //   name: 'echarts',
      //   var: 'echarts',
      //   path: 'https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js',
      // },
      // {
      //   name: 'axios',
      //   var: 'axios',
      //   path: 'https://cdn.jsdelivr.net/npm/axios@1.1.2/dist/axios.min.js',
      // },
    ],
  })
}
