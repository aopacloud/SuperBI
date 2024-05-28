import { message } from 'ant-design-vue'
import { CATEGORY } from '@/CONST.dict'
import { transformFieldsByVs, createSortByOrder, isEmpty } from './index'
import {
  transformFieldToColumn,
  listDataToTreeByKeys,
  updateColumnsWithCompare,
  createSummaryMap,
  transformWithQuickIndex,
} from '../Table/utils'
import { pickFlatten } from 'common/utils/array'
import { TREE_GROUP_NAME } from './createGroupTable'

export const COLUMN_FIELDS_NAME_JOIN = '__' // 列分组值的分隔符

export const MAX_COLUMNS_COUNT = 500 // 列字段最大数量

export const MAX_COUNT_MESSAGE =
  '当前条件分组过多，不适合使用交叉表格，请切换至明细表格'

// 转换列数据
const transformFields = fields => {
  const pFields = fields.filter(field => field.category === CATEGORY.PROPERTY)
  const rowFields = pFields.filter(field => field._group !== 'column')
  const columnFields = pFields.filter(field => field._group === 'column')
  const indexFields = fields.filter(field => field.category === CATEGORY.INDEX)
  const groupLength = rowFields.length

  const mergeGroupFields = () => {
    if (!groupLength) {
      return {
        ...columnFields[0],
        align: 'left',
        name: columnFields.map(t => t.renderName).join('/'),
        renderName: TREE_GROUP_NAME,
        displayName: indexFields.length ? ' ' : null,
        _action: null,
        _isMergeFields: true,
        _fields: columnFields,
        _columnFields: columnFields,
        _showSummary: false,
      }
    }

    return {
      ...rowFields[0],
      treeNode: rowFields.length > 1,
      align: 'left',
      name: rowFields.map(t => t.renderName).join('/'),
      renderName: TREE_GROUP_NAME,
      displayName: rowFields.map(t => t.displayName).join('/'),
      _isMergeFields: true,
      _fields: rowFields,
      _columnFields: columnFields,
    }
  }

  return {
    rowFields,
    columnFields,
    indexFields,
    groupLength,
    mergeGroupFields,
  }
}

const createIndexColumn = ({
  columnFields = [],
  indexFields = [],
  data = [],
  parentVal,
}) => {
  const prefix = parentVal ? parentVal + COLUMN_FIELDS_NAME_JOIN : ''
  if (!columnFields.length)
    return indexFields.map(t => {
      return {
        ...t,
        renderNameByValue: prefix + t.renderName,
      }
    })

  const [first, ...rest] = columnFields
  const firstData = [
    ...new Set(
      data.map(t => {
        const v = t[first.renderName]
        return isEmpty(v) ? '-' : v
      })
    ),
  ].sort(createSortByOrder(true))

  return firstData.map(t => {
    const renderNameByValue = prefix + t

    return {
      ...first,
      displayName: t,
      renderNameByValue,
      children: createIndexColumn({
        columnFields: rest,
        indexFields,
        data,
        parentVal: renderNameByValue,
      }),
    }
  })
}

export default function createTableData({
  originFields = [],
  originData = [],
  summaryRows = [],
  datasetFields = [],
  compare,
  config = {},
}) {
  // 处理对比字段
  const fields = transformFieldsByVs({ fields: originFields, compare })

  // 将字段数据转为分组字段
  const { rowFields, columnFields, indexFields, groupLength, mergeGroupFields } =
    transformFields(fields)

  // 对象数据
  let dataList = originData
    .map(row => row.map((t, i) => (t === '' && i < groupLength ? '-' : t)))
    .map(row => {
      const cValue = row.slice(0, columnFields.length),
        cValueStr = cValue.join(COLUMN_FIELDS_NAME_JOIN)

      return row.reduce((a, b, i) => {
        const { renderName } = fields[i]
        if (i >= groupLength + columnFields.length) {
          const prefix = cValueStr ? cValueStr + COLUMN_FIELDS_NAME_JOIN : ''

          a[prefix + renderName] = b
        } else {
          a[renderName] = b
        }
        return a
      }, {})
    })

  const indexColumns = createIndexColumn({
    columnFields,
    indexFields: indexFields.map(transformWithQuickIndex),
    data: dataList,
  })

  const getChildren = (list = []) => {
    if (!list.length) return []

    return list.reduce((acc, item) => {
      if (item.children && item.children.length) {
        return acc.concat(getChildren(item.children))
      } else {
        return acc.concat(item)
      }
    }, [])
  }
  // 检测列分组字段的数量
  const allIndexFields = columnFields.length
    ? pickFlatten(indexColumns)
    : indexColumns

  if (allIndexFields.length > MAX_COLUMNS_COUNT) {
    message.warn(MAX_COUNT_MESSAGE)

    throw Error(MAX_COUNT_MESSAGE)
  }

  // 分组字段
  const groupField = mergeGroupFields()

  // 将字段信息转为列信息
  let columns = (groupField ? [groupField, ...indexColumns] : indexColumns).map(
    (field, index) => transformFieldToColumn({ field, index })
  )

  // 汇总数据
  const summaryMap = createSummaryMap(summaryRows, fields)

  // 树结构
  const treeData = listDataToTreeByKeys({
    list: dataList,
    groupKeys: rowFields.map(t => t.renderName),
    columnGroupKeys: columnFields.map(t => t.renderName),
    summaryMap,
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
    list: treeData,
    summaryMap,
  }
}
