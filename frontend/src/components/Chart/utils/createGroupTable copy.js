import { COMPARE, CATEGORY } from '@/CONST.dict'
import { getWordWidth, getRandomKey } from 'common/utils/help'
import { upcaseFirst } from 'common/utils/string'
import { is_vs, transformFieldsByVs, formatFieldDisplay } from './index'
import { getCompareDisplay, summaryTree } from '../Table/utils'
import {
  CELL_HEADER_PADDING,
  CELL_BODY_PADDING,
  CELL_MIN_WIDTH,
} from '../Table/config'

// 分组行数据key
export const TREE_ROW_KEY = '_ROW_ID_'
// 分组父级行数据key
export const TREE_ROW_PARENT_KEY = '_PARENT_ROW_ID_'
// 分组显示字段
export const TREE_GROUP_NAME = '_GROUP_NAME_'

/**
 * 创建层级
 * @param {array} parent 父级
 * @param {array} children 子级
 * @param {?number} level 层级
 * @param {array} groupFields 分组字段
 */
const createTreeNode = ({
  parent = [],
  children = [],
  level,
  groupFields = [],
}) => {
  const groupLength = groupFields.length
  let res = []

  parent.forEach(i => {
    let pNode = {
      field: groupFields[level - 2]['name'],
      children: [],
      [TREE_GROUP_NAME]: i,
      _level_: level - 2,
    }
    children.forEach(j => {
      if (level == groupLength) {
        pNode.children.push({
          field: groupFields[level - 1]['name'],
          isLeaf: true,
          [TREE_GROUP_NAME]: j,
          _level_: level - 1,
        })
      } else {
        pNode.children.push(j)
      }
    })
    res.push(pNode)
  })

  return res
}

/**
 * 填充数据
 * @param {array} tree 树
 * @param {array} groupPath 分组路径
 * @param {number} groupLength 分组长度
 * @param {array} data 数据
 * @param {array} indexColumns 指标列
 */
const fillTreeData = ({
  tree = [],
  groupPath = [],
  data = [],
  groupLength,
  indexColumns = [],
}) => {
  return tree.map(node => {
    if (node.isLeaf) {
      const indexValue = getValueByGroup({
        data,
        groupLength,
        groupPath: groupPath.concat(node[TREE_GROUP_NAME]),
        indexColumns,
      })

      node = {
        ...node,
        ...indexValue,
        _empty: typeof indexValue === 'undefined',
      }
    } else {
      const children = fillTreeData({
        tree: node.children,
        groupPath: groupPath.concat(node[TREE_GROUP_NAME]),
        groupLength,
        data,
        indexColumns,
      }).filter(t => !t._empty)

      node = {
        ...node,
        children,
      }
    }

    return node
  })
}

/**
 * 根据分组获取数据
 * @param {array} data 数据
 * @param {array} groupPath 分组路径
 * @param {number} groupLength 分组长度
 * @param {array} indexColumns 列数据
 */
const getValueByGroup = ({
  data = [],
  groupPath = [],
  groupLength,
  indexColumns = [],
}) => {
  for (let i = 0; i < data.length; i++) {
    const row = data[i]

    const group = row.slice(0, groupLength)
    const indexVal = row.slice(groupLength)

    if (group.join(',') === groupPath.join(',')) {
      return indexVal.reduce((a, b, i) => {
        const { field } = indexColumns[i]

        a[field] = b
        return a
      }, {})
    }
  }
}

/**
 * 创建分组
 * @param {array} data 数据
 * @param {number} groupLength 分组长度
 */
const createGroup = (data, groupLength) => {
  const res = Array.from({ length: groupLength }, () => [])

  for (let i = 0; i < data.length; i++) {
    const row = data[i]

    for (let j = 0; j < row.length; j++) {
      const val = row[j]

      if (j < groupLength) {
        if (!res[j].includes(val)) {
          res[j].push(val)
        }
      }
    }
  }

  return res
}

/**
 * 创建树
 * @param {array} groupValues 分组值
 * @param {array} groupFields 分组字段
 */
const createTree = (groupValues = [], groupFields = []) => {
  let res = []
  const groupLength = groupFields.length

  for (let i = groupLength; i > 1; i--) {
    if (groupLength === i) {
      res = groupValues[i - 1]
    }

    res = createTreeNode({
      parent: groupValues[i - 2],
      children: res,
      level: i,
      groupFields,
    })
  }

  return res
}

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
      _isGroup: true,
      treeNode: true,
      align: 'left',
      name: TREE_GROUP_NAME,
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

/**
 * 将字段转换为列
 * @param {Field} field 字段
 * @param {number} i 索引
 */
const transformFieldsToColumns = (field, i) => {
  const {
    align,
    _isGroup,
    treeNode,
    displayName,
    category,
    renderName,
    dataType,
    _isVs,
    fields,
  } = field

  // 默认插槽
  const slotDefault = _isGroup
    ? 'groupName'
    : dataType.includes('TIME')
    ? 'date'
    : _isVs
    ? 'vs'
    : category //'default'

  return {
    treeNode,
    title: displayName,
    field: renderName,
    sortable: true,
    params: {
      field: {
        ...field,
      },
      fields,
    },
    align,
    slots: {
      header: 'header',
      default: slotDefault,
      footer: i === 0 ? 'footerSummary' : 'footer' + upcaseFirst(category),
    },
  }
}

const fillEmptyGroupWithStr = (list = [], groupLength = 0, str = '-') => {
  return list.map(row => {
    return row.map((val, i) => {
      if (i < groupLength) {
        return val || str
      } else {
        return val
      }
    })
  })
}

export default function createTableData({
  originFields = [],
  originData = [],
  datasetFields = [],
  compare,
  config = {},
}) {
  // 对比字段加上后缀
  const fields = transformFieldsByVs({
    fields: originFields,
    compare,
    compareOption: config.compare,
  })

  const { groupFields, indexFields, groupLength, mergeGroupFields } =
    transformFields(fields)

  const groupField = mergeGroupFields()

  let columns = [groupField, ...indexFields].map(transformFieldsToColumns)

  // 指标列
  const indexColumns = columns.filter(
    t => t.params.field.category !== CATEGORY.PROPERTY
  )

  // 填充空分组值
  const data = fillEmptyGroupWithStr(originData, groupLength)

  // 分组
  const groupRes = createGroup(data, groupLength)

  // 树
  const treeRes = createTree(groupRes, groupFields)

  // 树数据
  const treeData = fillTreeData({
    tree: treeRes,
    groupLength,
    data: data,
    indexColumns,
  })

  const f = (item, parentId) => {
    item[TREE_ROW_PARENT_KEY] = parentId
    item[TREE_ROW_KEY] = getRandomKey(6)
    if (item.children && item.children.length) {
      item.children = item.children.map(t => f(t, item[TREE_ROW_KEY]))
    }

    return item
  }

  const list = summaryTree(treeData, columns).map(t => f(t))

  /**
   * 同环比配置
   * mode: 显示模式, 0 默认，1 差值， 2 差值百分比
   * merge: 合并显示, false 单独显示， true 合并显示
   */
  const { mode = COMPARE.MODE.ORIGIN, merge = COMPARE.MERGE.FALSE } =
    config.compare || {}
  // 对比 单独显示 则过滤掉过滤字段
  if (merge) {
    // 删除过原始字段
    let hasDealOriginField = false

    columns = columns.reduce((acc, cur, i) => {
      if (!is_vs(cur.field)) {
        hasDealOriginField = false
        acc.push(cur)
      } else {
        if (!hasDealOriginField) {
          acc.pop()

          hasDealOriginField = true
        }

        const { field } = cur.params
        const titleWidth = getWordWidth(cur.title) + CELL_HEADER_PADDING
        const maxContentWidth = Math.max(
          ...data
            .map(t => {
              const targetValue = t[i - 1] // 前一个值为当前值
              const originValue = t[i] // '当前值' 为对比值

              const prevStr = formatFieldDisplay(
                targetValue,
                field,
                datasetFields
              )
              const nextStr =
                ' ( ' +
                getCompareDisplay(
                  originValue,
                  targetValue,
                  mode
                )(field, datasetFields) +
                ' ) '

              return getWordWidth(prevStr) + getWordWidth(nextStr)
            })
            .flat()
        )

        acc.push({
          ...cur,
          _width: Math.max(
            Math.max(titleWidth, maxContentWidth + CELL_BODY_PADDING),
            CELL_MIN_WIDTH
          ),
        })
      }

      return acc
    }, [])
  }

  return {
    columns,
    list,
    groupTable: true,
  }
}
