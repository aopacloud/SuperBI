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

import { lazyImport, VxeResolver } from 'vite-plugin-lazy-import'

export default function vxeTableResolver() {
  return lazyImport({
    resolvers: [
      VxeResolver({
        libraryName: 'vxe-table'
      }),
      VxeResolver({
        libraryName: 'vxe-pc-ui'
      })
    ]
  })
}
