import { CATEGORY, COMPARE } from '@/CONST.dict'
import { toPercent } from 'common/utils/number'
import { upcaseFirst } from 'common/utils/string'
import { getRandomKey, getWordWidth } from 'common/utils/help'
import { is_vs, formatFieldDisplay } from '@/components/Chart/utils/index.js'
import {
  TREE_GROUP_NAME,
  TREE_ROW_KEY,
  TREE_ROW_PARENT_KEY,
  SUMMARY_GROUP_NAME_JOIN,
  GROUP_NAME,
} from '@/components/Chart/utils/createGroupTable.js'
import { CELL_HEADER_PADDING, CELL_BODY_PADDING, CELL_MIN_WIDTH } from './config'

/**
 * 根据分页截取数据
 * @param {array<T>} data
 * @param {{ current: number, size: number }} param
 * @returns {array<T>}
 * @example
 *  getByPager([1, 2, 3, 4, 5], { current: 2, size: 2 }) // [3, 4]
 */
export const getByPager = (data, { current, size }) => {
  return data.slice((current - 1) * size, current * size)
}

/**
 *
 * @param {number} colIndex 当前列索引
 * @param {number} allCols 所有列索引
 * @param {array<number>} fixedColumnSpan 列冻结位置
 * @returns {''|'left'|'right'}
 */
export const getFixedPlace = (colIndex, allCols, fixedColumnSpan = []) => {
  const [start, end] = fixedColumnSpan

  if (isNaN(start)) {
    return ''
  } else {
    // 前几列 + 后几列 > 总列数 => 全部冻结
    if (start + end >= allCols) return 'left'
    // 当前列 <= 前几列 => 左冻结
    if (colIndex + 1 <= start) return 'left'
    // 总列数 - 当前列 = 剩余列 <= 后几列 => 右冻结
    if (allCols - colIndex <= end) return 'right'
  }
}

/**
 * 显示对比值
 * @param {number} origin 原值
 * @param {number} target 当前值
 * @param {number} mode 显示模式 0 原值 1 差值 2 差值百分比
 * @returns { (field: T, fields: Array<T>) => string}  (field, fields) => string
 */
export const getCompareDisplay = (origin, target, mode = 0) => {
  return function (field = {}, fields = []) {
    if (mode === COMPARE.MODE.DIFF_PERSENT) {
      const v = toPercent((target - origin) / origin, 2)

      return parseInt(v) > 0 ? '+' + v : v
    } else if (mode === COMPARE.MODE.DIFF) {
      const v = formatFieldDisplay(target - origin, field, fields)

      return parseInt(v) > 0 ? '+' + v : v
    } else {
      return formatFieldDisplay(origin, field, fields)
    }
  }
}

/**
 *  求和
 * @param {array} list
 * @param {string} key
 * @returns
 */
const getSum = (list = [], key = '') =>
  list.reduce((a, b) => {
    a = a + +(b[key] ?? 0)

    return a
  }, 0)

/**
 * 获取汇总行数据
 * @param {{keys: array<string>, column: array<Column>, list: array<any>}} param0
 * @returns
 */
export const getSummary = ({ keys = [], columns = [], list = [] }) => {
  return keys.reduce((acc, key, i) => {
    const { field } = columns[i].params
    if (field.category !== CATEGORY.INDEX) return acc

    acc[key] = getSum(list, key)

    return acc
  }, {})
}

// 树汇总
export const summaryTree = (tree = [], columns = []) => {
  const indexColumns = columns.filter(
    t => t.params.field.category === CATEGORY.INDEX
  )
  const keys = indexColumns.map(t => t.field)

  return tree.map(node => {
    if (node.children) {
      const children = summaryTree(node.children, indexColumns)
      const summaries = getSummary({
        keys,
        list: children,
        columns: indexColumns,
      })

      return {
        ...node,
        ...summaries,
        children,
      }
    } else {
      return {
        ...node,
      }
    }
  })
}

/**
 * 将字段信息转换为列信息
 * @param {Field} field 数据集字段
 * @param {number} i 索引
 * @returns
 */
export function transformFieldToColumn(field, i) {
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
    : category

  return {
    treeNode,
    title: displayName,
    field: renderName,
    sortable: true,
    params: { field, fields },
    align,
    slots: {
      header: 'header',
      default: slotDefault,
      footer: i === 0 ? 'footerSummary' : 'footer' + upcaseFirst(category),
    },
  }
}

/**
 * 同环比配置更新列
 * @param {{ columns: [T], originData: array, datasetFields: array, compare: {} }}
 * @returns {[T]} 返回新的列
 */
export function updateColumnsWithCompare({
  columns = [],
  originData = [],
  datasetFields = [],
  compare = {},
}) {
  let hasDelOriginField = false

  return columns.slice().reduce((acc, cur, i) => {
    if (!is_vs(cur.field)) {
      hasDelOriginField = false
      acc.push(cur)
    } else {
      if (!hasDelOriginField) {
        const preCol = acc.pop()

        // 将上一列的字段加上对比字段字段中
        if (!cur.params.fields) {
          cur.params.fields = []
        }

        cur.params.fields.push(preCol.params.field)

        hasDelOriginField = true
      }

      const { field } = cur.params
      const titleWidth = getWordWidth(cur.title) + CELL_HEADER_PADDING
      const maxContentWidth = Math.max(
        ...originData
          .map(t => {
            const targetValue = t[i - 1] // 前一个值为当前值
            const originValue = t[i] // '当前值' 为对比值

            const prevStr = formatFieldDisplay(targetValue, field, datasetFields)
            const nextStr =
              ' ( ' +
              getCompareDisplay(
                originValue,
                targetValue,
                compare.mode
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

/**
 * 将列表数据转换为树形结构。
 * @param {Array} list 列表数据。
 * @param {Array} keys 键数组，用于指定列表数据中哪些键作为树结构的层级划分依据。
 * @returns {Array} 转换后的树形结构数据。
 */
export function listDataToTreeByKeys({ list = [], keys = [], summaryMap = {} }) {
  // 创建树节点的函数
  const createTreeNode = ({
    key,
    parentId,
    children = new Map(),
    _level_ = 0,
    ...res
  }) => {
    // 返回一个构造好的树节点对象
    return {
      // 树节点的唯一标识
      [TREE_GROUP_NAME]: key || '-', // 不能为空字符串
      // 为每个树节点生成一个唯一的行键
      [TREE_ROW_KEY]: getRandomKey(6),
      // 父节点的键
      [TREE_ROW_PARENT_KEY]: parentId,
      // 树节点的层级
      _level_,
      // 子节点列表
      children,
      ...res,
    }
  }

  // 创建根节点
  const root = createTreeNode({ key: 'root', _level_: -1 })

  // 遍历输入的列表数据
  for (let data of list) {
    // 当前处理的节点
    let node = root

    // 遍历键数组，以确定数据在树中的位置
    keys.forEach((key, i) => {
      // 判断当前键是否为最后一级的键
      const isLastGroup = i === keys.length - 1
      // 获取当前键的值
      const keyValue = data[key]

      // 在当前节点的子节点中查找是否存在对应键值的节点
      let child = node.children.get(keyValue)
      // 如果不存在，则创建该节点
      if (!child) {
        const groupName =
          typeof node[GROUP_NAME] !== 'undefined'
            ? node[GROUP_NAME] + SUMMARY_GROUP_NAME_JOIN + keyValue
            : keyValue

        // 获取父节点的汇总数据
        const summary = summaryMap[groupName]

        // 创建子节点
        child = createTreeNode({
          _level_: node._level_ + 1,
          key: keyValue,
          parentId: i === 0 ? undefined : node[TREE_ROW_KEY],
          [GROUP_NAME]: groupName,
          ...summary,
        })
        // 如果是最后一级，则将当前数据合并到子节点中，否则只保留子节点
        if (isLastGroup) {
          child = { ...child, ...data }

          delete child.children
        }
        // 将新创建的子节点添加到当前节点的子节点列表中
        node.children.set(keyValue, child)
      }
      // 更新当前节点为找到或新创建的子节点，以便下一轮循环
      node = child
    })
  }

  const getValueFromMap = item => {
    const [, value] = item
    if (value.children) {
      return {
        ...value,
        children: [...value.children].map(getValueFromMap),
      }
    } else {
      return value
    }
  }

  // 返回根节点的子节点，即为转换后的树形结构
  return [...root.children].map(getValueFromMap)
}

/**
 * 生成汇总map
 * @param {String<>} row 行数据
 * @param {String<>} columns 列数据
 * @returns
 */
export const createSummaryMap = (row = [], columns = []) =>
  row.reduce((acc, col, i) => {
    acc[columns[i].renderName] = col

    return acc
  }, {})

/**
 * 生成树节点路径汇总map
 * @param {String<>} row 行数据
 * @param {String<>} columns 列数据
 * @returns
 */
export const createSummaryTreeMap = (rows = [], columns = []) => {
  if (!rows.length) return {}

  const result = {}
  const groupFields = columns.filter(t => t.category === CATEGORY.PROPERTY),
    iFields = columns.filter(t => t.category === CATEGORY.INDEX),
    groupL = groupFields.length
  const newRows = rows.map(t =>
    t.map((c, i) => {
      if (i >= groupFields.length) return c

      if (c === '') {
        c = '-'
      } else if (c === '-') {
        c = ''
      }

      return c
    })
  )

  newRows.forEach(row => {
    const path = row
      .slice(0, groupL - 1)
      .filter(t => t !== '')
      .join(SUMMARY_GROUP_NAME_JOIN)

    const rest = row.slice(groupL)

    result[path] = rest.reduce((a, v, i) => {
      a[iFields[i].renderName] = v

      return a
    }, {})
  })

  return result
}
