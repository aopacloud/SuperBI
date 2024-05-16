import { message } from 'ant-design-vue'
import { CATEGORY, COMPARE } from '@/CONST.dict'
import { toPercent } from 'common/utils/number'
import { upcaseFirst, getRandomKey, getWordWidth } from 'common/utils/string'
import { is_vs, formatFieldDisplay } from '@/components/Chart/utils/index.js'
import {
  TREE_GROUP_NAME,
  TREE_ROW_KEY,
  TREE_ROW_PARENT_KEY,
  SUMMARY_GROUP_NAME_JOIN,
  GROUP_PATH,
  MAX_GROUP_COUNT,
  MAX_COUNT_MESSAGE,
} from '@/components/Chart/utils/createGroupTable.js'
import { CELL_HEADER_PADDING, CELL_BODY_PADDING, CELL_MIN_WIDTH } from './config'
import { COLUMN_FIELDS_NAME_JOIN } from '@/components/Chart/utils/createIntersectionTable'
import { quickCalculateOptions } from '@/views/dataset/config.field'

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
      const v = toPercent((target - origin) / Math.abs(origin), 2)

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
 * 转化快速计算指标字段
 * @param {T} item
 * @returns {T}
 */
export const transformWithQuickIndex = item => {
  const { category, _quick } = item

  if (category === CATEGORY.INDEX && _quick) {
    const opt = quickCalculateOptions.find(t => t.value === _quick)

    item.displayName = item.displayName + '(' + opt.label + ')'
  }

  return item
}

/**
 * 将字段信息转换为列信息
 * @param {Field} field 数据集字段
 * @param {number} i 索引
 * @returns
 */
export function transformFieldToColumn({ field, index = 0, level = 0 }) {
  const {
    align,
    treeNode,
    displayName,
    category,
    renderName,
    dataType,
    _action = true,
    _isMergeFields,
    _isVs, // 是否同环比字段
    _fields,
    _columnFields,
    _quick,
    children = [],
    renderNameByValue, // 值映射的渲染名
  } = field

  for (const key in field) {
    if (key.startsWith('_')) {
      delete field[key]
    }
  }

  // 默认插槽
  const slotDefault = _isMergeFields
    ? 'groupName'
    : dataType.includes('TIME')
    ? 'date'
    : _isVs
    ? 'vs'
    : _quick
    ? 'quickCalculate'
    : category

  return {
    treeNode,
    title: displayName,
    field: renderNameByValue || renderName,
    sortable: _action && !children.length, // 可排序
    headerClassName: _isMergeFields ? 'merge-header' : '',
    params: {
      field,
      fields: _fields, // 关联字段
      _isVs, // 是否同环比字段
      _isMergeFields, // 是否合并的字段
      _columnFields, // 列分组字段
      _quick, // 快速计算
      formable: _action && !children.length, // 可自定义格式化
    },
    children: children.map((child, i) =>
      transformFieldToColumn({
        field: child,
        index: i,
        level: level + 1,
      })
    ),
    align,
    slots: {
      header: 'header',
      default: slotDefault,
      footer:
        level === 0 && index === 0
          ? 'footerSummary'
          : 'footer' + upcaseFirst(category),
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
    if (cur.children && cur.children.length) {
      cur.children = updateColumnsWithCompare({
        columns: cur.children,
        originData,
        datasetFields,
        compare,
      })
    }

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
        cur.params._quick = preCol.params._quick

        if (cur.params._quick) {
          const opt = quickCalculateOptions.find(t => t.value === cur.params._quick)
          cur.title = cur.title + '(' + opt.label + ')'
        }

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
 * @param {Array} groupKeys 键数组，用于指定列表数据中哪些键作为树结构的层级划分依据。
 * @returns {Array} 转换后的树形结构数据。
 */
export function listDataToTreeByKeys({
  list = [],
  groupKeys = [],
  columnGroupKeys = [],
  summaryMap = {},
}) {
  if (!list.length) return []

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

  // 记录当前分组的数量
  let count = 0

  // 没有行分组
  if (!groupKeys.length) {
    if (Object.keys(list[0]).length === columnGroupKeys.length) return []

    return [list.reduce((acc, cur) => ((acc = { ...acc, ...cur }), acc), {})]
  }

  // 遍历输入的列表数据
  for (let data of list) {
    // 当前处理的节点
    let node = root

    // 遍历键数组，以确定数据在树中的位置
    groupKeys.forEach((key, i) => {
      // 判断当前键是否为最后一级的键
      const isLastGroup = i === groupKeys.length - 1
      // 获取当前键的值
      const keyValue = data[key]

      // 在当前节点的子节点中查找是否存在对应键值的节点
      let child = node.children.get(keyValue)

      const groupPath =
        typeof node[GROUP_PATH] !== 'undefined'
          ? node[GROUP_PATH] + SUMMARY_GROUP_NAME_JOIN + keyValue
          : keyValue

      // 如果不存在，则创建该节点
      if (!child) {
        count++

        // 获取父节点的汇总数据
        const summary = summaryMap[groupPath]

        // 创建子节点
        child = createTreeNode({
          _level_: node._level_ + 1,
          key: keyValue,
          parentId: i === 0 ? undefined : node[TREE_ROW_KEY],
          [GROUP_PATH]: groupPath,
          ...summary,
        })
        // 如果是最后一级，则将当前数据合并到子节点中，否则只保留子节点
        if (isLastGroup) {
          child = { ...child, ...data }

          delete child.children
        }
        // 将新创建的子节点添加到当前节点的子节点列表中
        node.children.set(keyValue, child)
      } else {
        if (columnGroupKeys.length) {
          child = { ...child, ...data, ...summaryMap[groupPath] }
          node.children.set(keyValue, child)
        }
      }
      // 更新当前节点为找到或新创建的子节点，以便下一轮循环
      node = child
    })
  }

  // 检测最大分组数量
  if (count > MAX_GROUP_COUNT) {
    message.warn(MAX_COUNT_MESSAGE)

    throw Error(MAX_COUNT_MESSAGE)
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
 * 生成树节点路径汇总map
 * @param {String<>} row 行数据
 * @param {String<>} columns 列数据
 * @returns
 */
export const createSummaryMap = (rows = [], columns = []) => {
  if (!rows.length) return {}

  const result = {}

  const pFields = columns.filter(t => t.category === CATEGORY.PROPERTY),
    iFields = columns.filter(t => t.category === CATEGORY.INDEX),
    rowFields = pFields.filter(
      t => typeof t._group === 'undefined' || t._group === 'row'
    ),
    columnFields = pFields.filter(t => t._group === 'column')

  const newRows = rows.map(t =>
    t.map((c, i) => {
      if (i >= pFields.length) return c

      if (c === '') {
        c = '-'
      } else if (c === '-') {
        c = ''
      }

      return c
    })
  )

  newRows.forEach(row => {
    const groupPath = row
      .slice(columnFields.length, pFields.length)
      .filter(t => t !== '')
      .join(SUMMARY_GROUP_NAME_JOIN)

    const rest = row.slice(pFields.length)

    result[groupPath] = rest.reduce((a, v, i) => {
      const columnPath = row
        .slice(0, columnFields.length)
        .join(COLUMN_FIELDS_NAME_JOIN)
      const colIndexPath = columnPath
        ? columnPath + COLUMN_FIELDS_NAME_JOIN
        : columnPath

      a[colIndexPath + iFields[i].renderName] = v

      return a
    }, result[groupPath] || {})
  })

  return Object.keys(result).reduce((acc, key) => {
    if (key === '') {
      acc = { ...acc, ...result[key] }
    } else {
      acc[key] = result[key]
    }

    return acc
  }, {})
}

/**
 * 从一组值中获取排名
 * @param {Array<number>} list 总的值
 * @param {number} val 当前值
 * @returns {string|number} 排名
 */
export const getRankIndex = (list = [], val) => {
  // 无值不参与排序
  if (typeof val === 'undefined' || val === null) return '-'

  // 倒序
  const sorted = list.sort((a, b) => b - a)

  const index = sorted.findIndex(v => v === val)

  if (index < 0) return '-'

  return index + 1
}
