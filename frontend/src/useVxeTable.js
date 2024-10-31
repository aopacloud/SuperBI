import { Grid, Table, Column } from 'vxe-table'
import { VxeUI, VxeIcon, VxeLoading } from 'vxe-pc-ui'
import zhCN from 'vxe-pc-ui/lib/language/zh-CN'
import 'vxe-table/styles/cssvar.scss'
import 'vxe-pc-ui/styles/cssvar.scss'

export default function useVxeTable(app) {
  app.use(Grid)
  app.use(Table)
  app.use(Column)
  app.use(VxeIcon)
  app.use(VxeLoading)

  VxeUI.setI18n('zh-CN', zhCN)
  VxeUI.setLanguage('zh-CN')
}
