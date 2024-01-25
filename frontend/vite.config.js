import { defineConfig, loadEnv } from 'vite'
import path from 'path'
import createVitePlugins from './vite/plugins'

// https://cn.vitejs.dev/config
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd() + '/envs')
  const { VITE_APP_ENV, VITE_BUILD_SOURCEMAP, VITE_BUILD_OUTPUT } = env

  const isLocal = VITE_APP_ENV.startsWith('development')
  const isBuild = !isLocal

  return {
    envDir: './envs',
    base: './',
    plugins: createVitePlugins(env, isBuild),
    resolve: {
      // https://cn.vitejs.dev/config/shared-options.html#resolve-alias
      alias: {
        '~': path.resolve(__dirname, './'),
        '@': path.resolve(__dirname, './src'),
        common: path.resolve(__dirname, './src/common'),
      },
      // https://cn.vitejs.dev/config/shared-options.html#resolve-extensions
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json'],
    },
    build: {
      outDir: typeof VITE_BUILD_OUTPUT === 'string' ? VITE_BUILD_OUTPUT : 'dist-' + mode,
      sourcemap: VITE_BUILD_SOURCEMAP || false,
      rollupOptions: {
        output: {
          manualChunks: id => {
            // 将 node_modules 中的代码单独打包成一个 JS 文件
            if (id.includes('node_modules')) {
              return 'vendor'
              // return id.toString().split('node_modules/')[1].split('/')[0].toString()
            }
          },
        },
      },
    },
    // vite 相关配置
    server: {
      port: 80,
      host: true,
      open: true,
      proxy: {
        // https://cn.vitejs.dev/config/#server-proxy
      },
    },
    //fix:error:stdin>:7356:1: warning: "@charset" must be the first rule in the file
    css: {
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: atRule => {
                if (atRule.name === 'charset') {
                  atRule.remove()
                }
              },
            },
          },
        ],
      },
      preprocessorOptions: {
        scss: {
          additionalData: `@import "@/assets/styles/preset.global.scss";`,
        },
      },
    },
  }
})
