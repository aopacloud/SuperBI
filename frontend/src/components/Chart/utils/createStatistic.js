import { transformOriginBySort } from './utils.js'
import { deepClone } from 'common/utils/help'
import { CATEGORY } from '@/CONST.dict.js'

export default function createStatistic({
  originFields = [],
  originData = []
}) {
  // 处理源数据字段和数据的排序
  let { fields, data } = transformOriginBySort({
    originFields: deepClone(originFields),
    originData: deepClone(originData)
  })

  // 第一个指标的索引
  const index = fields.findIndex(t => t.category === CATEGORY.INDEX)
  if (index < 0) return {}

  // 第一个指标字段
  const indexField = fields[index]
  // 行数据
  const row = data[0]

  return {
    fields,
    field: indexField,
    row,
    value: row[index]
  }
}
