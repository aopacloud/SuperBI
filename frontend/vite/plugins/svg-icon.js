import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import { resolve } from 'path'

export default function createSvgIcon(isBuild) {
  return createSvgIconsPlugin({
    iconDirs: [resolve(process.cwd(), 'src/components/SvgIcon/svg')],
    symbolId: 'svgIcon-[name]',
    svgoOptions: isBuild,
  })
}
