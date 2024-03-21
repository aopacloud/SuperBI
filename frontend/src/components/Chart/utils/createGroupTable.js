import { CATEGORY } from '@/CONST.dict'
import { transformFieldsByVs } from './index'
import {
  transformFieldToColumn,
  listDataToTreeByKeys,
  summaryTree,
  updateColumnsWithCompare,
} from '../Table/utils'

export const TREE_ROW_KEY = '_ROW_ID_' // 分组行数据key
export const TREE_ROW_PARENT_KEY = '_PARENT_ROW_ID_' // 分组父级行数据key
export const TREE_GROUP_NAME = '_GROUP_NAME_' // 分组显示字段

// 转换列数据
const transformFields = fields => {
  const groupFields = fields.filter(field => field.category === CATEGORY.PROPERTY)
  const indexFields = fields.filter(field => field.category === CATEGORY.INDEX)
  const groupLength = groupFields.length

  const mergeGroupFields = () => {
    if (!groupLength) return undefined

    return {
      ...groupFields[0],
      _isGroup: true,
      treeNode: true,
      align: 'left',
      name: groupFields.map(t => t.renderName).join('/'),
      renderName: TREE_GROUP_NAME,
      displayName: groupFields.map(t => t.displayName).join('/'),
      fields: groupFields.slice(),
    }
  }

  return {
    groupFields,
    indexFields,
    groupLength,
    mergeGroupFields,
  }
}

export default function createTableData({
  originFields = [],
  originData = [],
  datasetFields = [],
  compare,
  config = {},
}) {
  // 处理对比字段
  const fields = transformFieldsByVs({ fields: originFields, compare })

  // 将字段数据转为分组字段
  const { groupFields, indexFields, groupLength, mergeGroupFields } =
    transformFields(fields)

  // 对象数据
  const dataList = originData.map(row =>
    row.reduce((a, b, i) => {
      const { renderName } = fields[i]

      a[renderName] = i < groupLength ? b || '-' : b

      return a
    }, {})
  )

  // 分组字段
  const groupField = mergeGroupFields()

  // 将字段信息转为列信息
  let columns = [groupField, ...indexFields].map(transformFieldToColumn)

  // 树结构
  const treeData = listDataToTreeByKeys(
    dataList,
    groupFields.map(t => t.renderName)
  )

  // 计算汇总
  const list = summaryTree(treeData, columns)

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
    groupTable: true,
  }
}
