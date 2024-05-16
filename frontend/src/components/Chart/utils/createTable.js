import { transformFieldsByVs } from './index.js'
import {
  transformFieldToColumn,
  updateColumnsWithCompare,
  transformWithQuickIndex,
} from '../Table/utils.js'

export default function createTableData({
  originFields = [],
  originData = [],
  datasetFields = [],
  compare,
  config = {},
}) {
  // 处理对比字段
  const fields = transformFieldsByVs({ fields: originFields, compare })

  let columns = fields
    .map(transformWithQuickIndex)
    .map((field, index) => transformFieldToColumn({ field, index }))

  const list = originData.map(arr => {
    return arr.reduce((acc, pre, i) => {
      const { field: renderName } = columns[i]

      acc[renderName] = pre

      return acc
    }, {})
  })

  // 对比合并显示
  if (config.compare?.merge) {
    columns = updateColumnsWithCompare({
      columns,
      originData,
      datasetFields,
      compare: config.compare,
    })
  }

  return {
    columns,
    list,
  }
}
