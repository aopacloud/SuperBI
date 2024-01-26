import vue from '@vitejs/plugin-vue'

import createAutoImport from './auto-import'
import createCompression from './compression'
import createSetupExtend from './setup-extend'
import antDesignVueResolver from './ant-design-vue-resolver'
import vxeTableResolver from './vxe-table-resolver'
import vueJsx from './vue-jsx'
// 修复 defineOptions 报错
import DefineOptions from 'unplugin-vue-define-options/vite'
import createVisualizer from './visualizer'

export default function createVitePlugins(viteEnv, isBuild = false) {
  const vitePlugins = [vue(), DefineOptions()]

  vitePlugins.push(createAutoImport())
  vitePlugins.push(createSetupExtend())
  vitePlugins.push(antDesignVueResolver())
  vitePlugins.push(vxeTableResolver())
  vitePlugins.push(vueJsx())

  if (isBuild) {
    if (viteEnv.VITE_BUILD_COMPRESS) {
      vitePlugins.push(createCompression())
    }

    vitePlugins.push(createVisualizer())
  }

  return vitePlugins
}
