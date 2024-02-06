// import Components from 'unplugin-vue-components/vite'
// import { kebabCase } from 'unplugin-vue-components'

// const getSideEffects = name => {
//   return `vxe-table/es/${name}/style.css`
// }

// export default function vxeTableResolver() {
//   return Components({
//     resolvers: [
//       {
//         type: 'component',
//         resolve: name => {
//           if (/^Vxe/.test(name)) {
//             const compName = name.slice(3)
//             const partialName = kebabCase(compName)
//             return {
//               from: `vxe-table/es/${partialName}`,
//               sideEffects: getSideEffects(partialName),
//             }
//           }
//         },
//       },
//     ],
//     dts: true,
//   })
// }

import { createStyleImportPlugin, VxeTableResolve } from 'vite-plugin-style-import'

export default function vxeTableResolver() {
  return createStyleImportPlugin({
    resolves: [VxeTableResolve()],
  })
}
