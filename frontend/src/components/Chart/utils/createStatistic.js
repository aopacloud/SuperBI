import { transformOriginBySort } from './utils.js'
import { CATEGORY } from '@/CONST.dict'
import { formatFieldDisplay } from './index'
import { deepClone } from 'common/utils/help'

export default function createStatistic({
  originFields = [],
  originData = [],
  datasetFields = [],
}) {
  // 处理源数据字段和数据的排序
  const { fields: fieldsSorted, data } = transformOriginBySort({
    originFields: deepClone(originFields),
    originData: deepClone(originData),
  })

  const pFields = fieldsSorted.filter(t => t.category === CATEGORY.PROPERTY)
  const iFields = fieldsSorted.filter(t => t.category === CATEGORY.INDEX)
  // 优先第一个指标，否则第一个维度
  const field = iFields.length ? iFields[0] : pFields[0]
  const vIndex = fieldsSorted.findIndex(t => t.name === field.name)
  const value = data[0]?.[vIndex]

  return {
    field: field ?? {},
    value: formatFieldDisplay(value, field, datasetFields),
  }
}
