import { VS_FIELD_SUFFIX } from './index.js'
import { createSortByOrders } from 'common/utils/help'

export default function createReversedTable({
  data = [],
  columns = [],
  config = {},
  sorters = []
}) {
  data = data.sort(createSortByOrders(sorters))

  const rData = columns.map(col => {
    const r = { col0: col.title, col0_field: col }
    const [oName, vsName] = col.field.split(VS_FIELD_SUFFIX)
    return data.reduce((acc, row, rowIndex) => {
      if (config.compare?.merge) {
        // 同环比合并显示给同环比添加对比字段
        acc[`col${rowIndex + 1}`] = row[oName]

        if (vsName) {
          acc[`col${rowIndex + 1}` + VS_FIELD_SUFFIX + vsName] = row[col.field]
        }
      } else {
        acc[`col${rowIndex + 1}`] = row[col.field]
        if (col.params._isVs) {
          acc[`col${rowIndex + 1}` + VS_FIELD_SUFFIX + 'TARGET'] = row[oName]
        }
      }

      return acc
    }, r)
  })

  const rColumns = data.reduce(
    (acc, row, index) => {
      return acc.concat({
        field: `col${index + 1}`,
        slots: {
          default: 'reversedDefault',
          footer: 'reversedRowSummary'
        },
        params: { row },
        minWidth: 80
      })
    },
    [
      {
        field: 'col0',
        fixed: 'left',
        slots: {
          footer: 'footerSummary'
        },
        width: 80
      }
    ]
  )

  const { row = {} } = config.summary
  if (row.enable) {
    rColumns.push({
      field: 'column_summary',
      title: '汇总',
      className: 'summary-cell',
      params: {
        _isColumnSummary: true
      },
      slots: {
        default: 'reversedColumnSummary'
      }
    })
  }

  return { columns: rColumns, data: rData }
}
