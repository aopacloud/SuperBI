import { transformFieldsByVs } from './index.js'
import {
  transformFieldToColumn,
  updateColumnsWithCompare,
  createSummaryMap,
  getListByReplacedSpecial
} from '../Table/utils.js'
import { CATEGORY } from '@/CONST.dict.js'
import createReversedTable from './createReversedTable.js'
import { deepClone } from 'common/utils/help'
import { transformWithQuickIndex } from '@/views/analysis/utils'

export default function createTableData({
  originFields = [],
  originData = [],
  datasetFields = [],
  compare,
  config = {},
  sorters,
  summaryRows = []
}) {
  originData = getListByReplacedSpecial({
    dataSource: deepClone(originData),
    columns: deepClone(originFields),
    config: config.special
  })

  // 处理对比字段
  const fields = transformFieldsByVs({ fields: originFields, compare })

  let columns = fields
    .map(transformWithQuickIndex)
    .map((field, index) =>
      transformFieldToColumn({ field, index }, config.columnsWidth)
    )

  let list = originData.map(arr => {
    return arr.reduce((acc, pre, i) => {
      acc[columns[i].field] = pre

      return acc
    }, {})
  })

  // 对比合并显示
  if (config.compare?.merge) {
    columns = updateColumnsWithCompare({
      columns,
      originData,
      datasetFields,
      compare: config.compare
    })
  }

  // 行列反转
  const isReversed = config.reverse
  if (isReversed) {
    const { data: rData, columns: rColumns } = createReversedTable({
      data: list,
      columns,
      config,
      sorters
    })

    list = rData
    columns = rColumns
  } else {
    // 列汇总
    let columnSummaryColumn = [],
      summaryColumn = config.summary?.column
    if (summaryColumn?.enable) {
      const resizeWidth = config.columnsWidth?.find(
        t => t.field === 'column_summary'
      )?.width

      columnSummaryColumn = {
        field: 'column_summary',
        title: '列汇总',
        _width: resizeWidth,
        className: 'summary-cell',
        params: {
          _isColumnSummary: true
        },
        slots: {
          default: 'columnSummary'
        }
      }

      // 首列
      if (summaryColumn.position === 'first') {
        if (isReversed) {
          columns.splice(1, 0, columnSummaryColumn)
        } else {
          columns.unshift(columnSummaryColumn)
        }
      } else if (summaryColumn.position === 'afterInDimension') {
        // 维度列后
        if (isReversed) {
          columns.splice(1, 0, columnSummaryColumn)
        } else {
          // 最后一列
          const firstIndex = columns.findIndex(
            item => item.params.field.category === CATEGORY.INDEX
          )
          columns.splice(firstIndex, 0, columnSummaryColumn)
        }
      } else {
        columns.push(columnSummaryColumn)
      }
    }
  }

  // 序号
  if (config.orderSeq) {
    columns.unshift({
      type: 'seq',
      title: '序号',
      align: 'center',
      fixed: 'left',
      resizable: false
    })
  }

  // 汇总数据
  const summaryMap = createSummaryMap(summaryRows, fields)

  return {
    columns,
    list,
    summaryMap
  }
}
