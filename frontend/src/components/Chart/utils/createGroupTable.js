import { CATEGORY } from '@/CONST.dict'
import { transformFieldsByVs } from './index'
import {
  transformFieldToColumn,
  listDataToTreeByKeys,
  updateColumnsWithCompare,
  createSummaryMap,
  getListByReplacedSpecial
} from '../Table/utils'
import { deepClone } from 'common/utils/help'
import { transformWithQuickIndex } from '@/views/analysis/utils'

export const TREE_ROW_KEY = '_ROW_ID_' // 分组行数据key
export const TREE_ROW_PARENT_KEY = '_PARENT_ROW_ID_' // 分组父级行数据key
export const TREE_GROUP_NAME = '_GROUP_NAME_' // 分组显示字段
export const GROUP_PATH = 'GROUP_PATH' // 分组路径
export const SUMMARY_GROUP_NAME_JOIN = '__' // 汇总分组连接符

export const MAX_GROUP_COUNT = 10000 // 分组最大数量

export const MAX_GROUP_COUNT_MESSAGE =
  '当前条件分组过多，不适合使用分组/交叉表格，请切换至明细表格'

// 转换列数据
const transformFields = fields => {
  const groupFields = fields.filter(
    field => field.category === CATEGORY.PROPERTY
  )
  const indexFields = fields.filter(field => field.category === CATEGORY.INDEX)
  const groupLength = groupFields.length

  const mergeGroupFields = () => {
    if (!groupLength) return undefined

    return {
      ...groupFields[0],
      treeNode: true,
      align: 'left',
      name: groupFields.map(t => t.renderName).join('/'),
      renderName: TREE_GROUP_NAME,
      displayName: groupFields.map(t => t.displayName).join('/'),
      _fields: groupFields.slice(),
      _isMergeFields: true
    }
  }

  return {
    groupFields,
    indexFields,
    groupLength,
    mergeGroupFields
  }
}

export default function createTableData({
  originFields = [],
  originData = [],
  summaryRows = [],
  datasetFields = [],
  compare,
  config = {}
}) {
  originData = getListByReplacedSpecial(
    {
      dataSource: deepClone(originData),
      columns: deepClone(originFields),
      config: config.special
    },
    '-'
  )

  // 处理对比字段
  const fields = transformFieldsByVs({ fields: originFields, compare })

  // 将字段数据转为分组字段
  const { groupFields, indexFields, mergeGroupFields } = transformFields(fields)

  // .map(row => row.map((t, i) => (t === '' && i < groupLength ? '-' : t)))
  // 对象数据
  const dataList = originData.map(row =>
    row.reduce((a, b, i) => {
      a[fields[i].renderName] = b
      return a
    }, {})
  )

  // 分组字段
  const groupField = mergeGroupFields()

  // 将字段信息转为列信息
  let columns = [groupField, ...indexFields]
    .filter(Boolean)
    .map(transformWithQuickIndex)
    .map((field, index) =>
      transformFieldToColumn({ field, index }, config.columnsWidth)
    )

  // 列汇总
  let columnSummaryColumn = [],
    summaryColumn = config.summary?.column
  if (summaryColumn?.enable) {
    const resizeWidth = config.columnsWidth?.find(
      t => t.field === 'column_summary'
    )?.width

    columnSummaryColumn = {
      field: 'column_summary',
      title: '汇总',
      _width: resizeWidth,
      className: 'summary-cell',
      params: {
        _isColumnSummary: true
      },
      slots: {
        default: 'columnSummary'
      }
    }

    if (summaryColumn.position === 'first') {
      columns.unshift(columnSummaryColumn)
    } else if (summaryColumn.position === 'afterInDimension') {
      const firstIndex = columns.findIndex(
        item => item.params.field.category === CATEGORY.INDEX
      )
      columns.splice(firstIndex, 0, columnSummaryColumn)
    } else {
      columns.push(columnSummaryColumn)
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
  const summaryMap = createSummaryMap(summaryRows, fields, config)

  // 树结构
  const treeData = listDataToTreeByKeys({
    list: dataList,
    groupKeys: groupFields.map(t => t.renderName),
    summaryMap,
    renderType: config.renderType
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

  return {
    columns,
    list: treeData,
    summaryMap
  }
}
