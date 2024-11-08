import { message } from 'ant-design-vue'
import { CATEGORY, COMPARE } from '@/CONST.dict'
import { toPercent, sum, avg, quantile } from 'common/utils/number'
import { upcaseFirst, getRandomKey } from 'common/utils/string'
import {
  is_vs,
  formatFieldDisplay,
  isEmpty
} from '@/components/Chart/utils/index.js'
import {
  TREE_GROUP_NAME,
  TREE_ROW_KEY,
  TREE_ROW_PARENT_KEY,
  SUMMARY_GROUP_NAME_JOIN,
  GROUP_PATH,
  MAX_GROUP_COUNT,
  MAX_GROUP_COUNT_MESSAGE
} from '@/components/Chart/utils/createGroupTable.js'
import { COLUMN_FIELDS_NAME_JOIN } from '@/components/Chart/utils/createIntersectionTable'
import {
  quickCalculateOptions,
  formatterByCustom,
  QUANTILE_PREFIX,
  formatterOptions,
  FORMAT_DEFAULT_CODE
} from '@/views/dataset/config.field'
import { deepFind } from 'common/utils/help'
import { flat } from 'common/utils/array'
import { isSpecialNumber } from '@/views/analysis/LayoutAside/components/SettingSections/config'
import { isRenderTable } from '@/views/analysis/utils'

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
export const getCompareDisplay = ({
  origin,
  target,
  mode = 0,
  config = {}
}) => {
  return function (field = {}, fields = []) {
    if (mode === COMPARE.MODE.DIFF_PERSENT) {
      const v = String(toPercent((target - origin) / Math.abs(origin), 2))
      if (isSpecialNumber(v.split('%')[0])) {
        if (
          !config.special?.measure ||
          config.special?.measure === 'original'
        ) {
          return v
        }

        return replaceSpecialBy({
          value: v.split('%')[0],
          category: CATEGORY.INDEX,
          config: config.special
        })
      } else {
        return parseInt(v) > 0 ? '+' + v : v
      }
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
        columns: indexColumns
      })

      return {
        ...node,
        ...summaries,
        children
      }
    } else {
      return {
        ...node
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
export function transformFieldToColumn(
  { field, index = 0, level = 0 },
  columnsWidth = []
) {
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
    _fields = [],
    _columnFields,
    children = [],
    renderNameByValue, // 值映射的渲染名
    fastCompute // 快速计算
  } = field

  // 默认插槽
  const slotDefault = _isMergeFields
    ? 'groupName'
    : dataType.includes('TIME')
      ? 'date'
      : _isVs
        ? 'vs'
        : fastCompute
          ? 'quickCalculate'
          : category

  // vxe表格渲染字段名
  const columnField = renderNameByValue || renderName
  // 保存的列宽
  const resizeWidth = columnsWidth.find(t => t.field === columnField)?.width

  return {
    treeNode,
    _width: resizeWidth,
    title: String(displayName), // 转为字符串
    field: columnField,
    sortable: _action && !children.length, // 可排序
    headerClassName: _isMergeFields ? 'merge-header' : '',
    footerClassName: 'summary-cell',
    params: {
      field,
      fields: _fields.map((field, i) =>
        transformFieldToColumn({ field, index: i })
      ), // 关联字段
      _isVs, // 是否同环比字段
      _isMergeFields, // 是否合并的字段
      _columnFields: _columnFields?.map((field, i) =>
        transformFieldToColumn({ field, index: i })
      ), // 列分组字段
      fastCompute, // 快速计算
      _level: level,
      formable: _action && !children.length // 可自定义格式化
    },
    children: children.map((child, i) =>
      transformFieldToColumn({
        field: child,
        index: i,
        level: level + 1
      })
    ),
    align,
    slots: {
      header: 'header',
      default: slotDefault,
      footer:
        level === 0 && index === 0
          ? 'footerSummary'
          : 'footer' + upcaseFirst(category)
    }
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
  compare = {}
}) {
  let hasDelOriginField = false

  return columns.slice().reduce((acc, cur, i) => {
    if (cur.children && cur.children.length) {
      cur.children = updateColumnsWithCompare({
        columns: cur.children,
        originData,
        datasetFields,
        compare
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
        cur.params.fastCompute = preCol.params.fastCompute

        if (cur.params.fastCompute) {
          const opt = quickCalculateOptions.find(
            t => t.value === cur.params.fastCompute
          )
          cur.title = cur.title + '(' + opt.label + ')'
        }

        hasDelOriginField = true
      }

      acc.push(cur)
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
  renderType = 'table'
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
      [TREE_GROUP_NAME]: isEmpty(key) ? '-' : key, // 不能为空字符串
      // 为每个树节点生成一个唯一的行键
      [TREE_ROW_KEY]: getRandomKey(6),
      // 父节点的键
      [TREE_ROW_PARENT_KEY]: parentId,
      // 树节点的层级
      _level_,
      // 子节点列表
      children,
      ...res
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
    groupKeys.forEach((key, i, keys) => {
      // 判断当前键是否为最后一级的键
      const isLastGroup = i === keys.length - 1
      // 获取当前键的值
      const keyValue = data[key]
      // if(isTable && keyValue ===)

      // 在当前节点的子节点中查找是否存在对应键值的节点
      let child = node.children.get(keyValue)

      const groupPath =
        typeof node[GROUP_PATH] !== 'undefined'
          ? node[GROUP_PATH] + SUMMARY_GROUP_NAME_JOIN + keyValue
          : keyValue

      // 如果不存在，则创建该节点
      if (!child) {
        // 汇总数据空数据key为 __- , 所以需要填充 -
        let summaryKey = groupPath
        if (isEmpty(keyValue)) {
          summaryKey =
            typeof node[GROUP_PATH] !== 'undefined'
              ? (node[GROUP_PATH] || '-') + SUMMARY_GROUP_NAME_JOIN + '-'
              : '-'
        }

        // 获取父节点的汇总数据
        const summary = summaryMap[summaryKey]

        // 创建子节点
        child = createTreeNode({
          _level_: node._level_ + 1,
          key: keyValue,
          [key]: keyValue,
          parentId: i === 0 ? undefined : node[TREE_ROW_KEY],
          [GROUP_PATH]: groupPath,
          ...summary
        })
        // 如果是最后一级，则将当前数据合并到子节点中，否则只保留子节点
        if (isLastGroup) {
          child = { ...child, ...data }

          delete child.children
        } else {
          count++
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
  if (renderType !== 'table' && count > MAX_GROUP_COUNT) {
    message.warn(MAX_GROUP_COUNT_MESSAGE)

    throw Error(MAX_GROUP_COUNT_MESSAGE)
  }

  const getValueFromMap = item => {
    const [, value] = item
    if (value.children) {
      return {
        ...value,
        children: [...value.children].map(getValueFromMap)
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
export const createSummaryMap = (rows = [], columns = [], config) => {
  if (!rows.length) return {}

  const result = {}

  const pFields = columns.filter(t => t.category === CATEGORY.PROPERTY),
    iFields = columns.filter(t => t.category === CATEGORY.INDEX),
    rowFields = pFields.filter(
      t => typeof t._group === 'undefined' || t._group === 'row'
    ),
    columnFields = pFields.filter(t => t._group === 'column')

  const getSpe = (value, category) =>
    replaceSpecialBy(
      {
        value,
        category,
        config: config?.special
      },
      '-'
    )

  const newRows = [...rows].map(t =>
    t.map((c, i) => {
      if (i >= pFields.length) return c

      c = getSpe(c, CATEGORY.PROPERTY)

      return c
    })
  )

  newRows.forEach(row => {
    const groupPath = row
      .slice(columnFields.length, pFields.length)
      .filter(t => t !== '-£-')
      .join(SUMMARY_GROUP_NAME_JOIN)

    const rest = row.slice(pFields.length)

    result[groupPath] = rest.reduce((a, v, i) => {
      const columnPath = row
        .slice(0, columnFields.length)
        .filter(t => t !== '-£-')
        .join(COLUMN_FIELDS_NAME_JOIN)

      const colIndexPath = columnPath
        ? columnPath + COLUMN_FIELDS_NAME_JOIN
        : columnPath

      if (!iFields[i]) return a // 避免边界问题报错

      a[colIndexPath + iFields[i].renderName] = v

      return a
    }, result[groupPath] || {})
  })

  return Object.keys(result).reduce((acc, key) => {
    if (key === '') {
      acc = { ...acc, ...result[key] }
    } else {
      if (key.includes('-£-')) {
        acc[key.replace(/-£-/g, '-')] = result[key]
      } else {
        acc[key] = result[key]
      }
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

const quickCalculateMap = ({
  renderType = 'table',
  columns = [],
  listTree = [],
  quickType,
  summary = {},
  isGroupTable,
  special = { dimension: 'original', measure: '0' }
}) => {
  columns = columns.filter(
    t => t.type !== 'seq' && t.field !== 'column_summary'
  )

  const _getListByFlat = list => {
    return list.reduce((acc, cur) => {
      if (cur.children?.length) {
        acc = acc.concat(_getListByFlat(cur.children))
      } else {
        acc.push(cur)
      }

      return acc
    }, [])
  }

  // 获取分组名
  const _getGroupName = (row, end = -1) => {
    const columnField = columns.find(t => t.params._columnFields?.length)
    if (columnField) {
      return columnField.params._columnFields
        .map(t => t.renderName)
        .map(key => row[key])
        .join(SUMMARY_GROUP_NAME_JOIN)
    } else {
      const groupKeys = columns
        .filter(t => t.params.field.category === CATEGORY.PROPERTY)
        .map(t => t.params.field.renderName)
        .slice(0, end)

      if (!groupKeys.length) return

      return groupKeys.map(key => row[key]).join(SUMMARY_GROUP_NAME_JOIN)
    }
  }

  // 从渲染数据树结构中获取所在父级
  const _getByDfs = (s, k) => deepFind(listTree, item => item[k] === s)

  // 获取真实的指标渲染名
  const _getRenderNameByVs = field => {
    const renderKey =
      renderType === 'intersectionTable' ? 'renderNameByValue' : 'renderName'

    return field._isVs
      ? field[renderKey].split(VS_FIELD_SUFFIX)[0]
      : field[renderKey]
  }

  // 没有值
  const _isNull = val => typeof val === 'undefined' || val === null

  const _toReplaced = v => {
    const [s] = String(v).split('%')

    if (isSpecialNumber(s)) {
      if (!special?.measure || special?.measure === 'original') return v

      return replaceSpecialBy({
        value: s,
        category: CATEGORY.INDEX,
        config: special
      })
    } else {
      return v
    }
  }

  const map = {
    // 占比 - 当前指标(包含交叉表格中的指标)在汇总指标中的占比
    ratio: (row, field) => {
      const fieldName = _getRenderNameByVs(field)
      const value = row[fieldName]
      if (_isNull(value)) return ''

      return _toReplaced(toPercent(value / summary[fieldName]))
    },
    // 组内占比 - 当前指标在其所在组内的占比
    ratio_group: (row, field) => {
      const fieldName = _getRenderNameByVs(field)
      const value = row[fieldName]
      if (_isNull(value)) return ''

      if (isGroupTable) {
        if (row._level_ === 0) {
          return _toReplaced(toPercent(value / summary[fieldName]))
        }

        const parent = deepFind(
          listTree,
          item => item[TREE_ROW_KEY] === row[TREE_ROW_PARENT_KEY]
        )
        if (!parent) return value

        return _toReplaced(toPercent(value / parent[fieldName]))
      } else {
        let groupName = ''
        if (renderType === 'intersectionTable') {
          // 分组名
          groupName = fieldName
            .split(COLUMN_FIELDS_NAME_JOIN)
            .slice(0, -1)
            .join(COLUMN_FIELDS_NAME_JOIN)
        } else {
          groupName = _getGroupName(row)
        }

        if (typeof groupName === 'undefined') {
          return _toReplaced(toPercent(value / summary[fieldName]))
        }

        // 获取树结构中的parent
        const parent = deepFind(
          listTree,
          item => item[GROUP_PATH] === groupName
        )

        if (parent) return _toReplaced(toPercent(value / parent[fieldName]))

        return _toReplaced(
          toPercent(
            value /
              summary[groupName + COLUMN_FIELDS_NAME_JOIN + field.renderName]
          )
        )
      }
    },
    // 排名 - 当前指标在所有行数据中指标集合的排名
    rank: (row, field) => {
      if (row.children?.length) return '-' // 只在指标上进行排名

      const fieldName = _getRenderNameByVs(field)
      const value = row[fieldName]
      if (_isNull(value)) return ''

      if (renderType === 'intersectionTable') {
        // 交叉表格需要去具体指标值作为排名集合
        const dataList = _getListByFlat(listTree)
        const list = dataList.reduce((acc, item) => {
          Object.keys(item).forEach(key => {
            const _name = key.split(COLUMN_FIELDS_NAME_JOIN).pop()

            if (_name === field.renderName) acc.push(item[key])
          })
          return acc
        }, [])

        return getRankIndex(list, value)
      } else {
        const dataList = _getListByFlat(listTree)
        const list = dataList.map(t => t[fieldName])

        return getRankIndex(list, value)
      }
    },
    // 组内排名
    rank_group: (row, field) => {
      const fieldName = _getRenderNameByVs(field)
      const value = row[fieldName]
      if (_isNull(value)) return ''

      if (isGroupTable) {
        if (row._level_ === 0) {
          let list = listTree.map(t => t[fieldName])

          if (renderType === 'intersectionTable') {
            list = list.filter(t => typeof t !== 'undefined' || t !== null)
          }

          return getRankIndex(list, value)
        }
        const parentGroupName = _getGroupName(row)
        const parent = deepFind(
          listTree,
          item => item[GROUP_PATH] === parentGroupName
        )
        if (!parent || !parent.children) return '-'

        const list = parent.children.map(t => t[fieldName])

        return getRankIndex(list, value)
      } else {
        if (renderType === 'intersectionTable') {
          const dataList = _getListByFlat(listTree)
          const list = dataList.map(t => t[fieldName])

          return getRankIndex(list, value)
        } else {
          // 分组名
          const parentGroupName = _getGroupName(row)
          if (typeof parentGroupName === 'undefined') {
            const list = listTree.map(t => t[fieldName])
            return getRankIndex(list, value)
          }

          const parent = deepFind(
            listTree,
            item => item[GROUP_PATH] === parentGroupName
          )
          if (!parent || !parent.children) return '-'

          const list = parent.children.map(t => t[fieldName])

          return getRankIndex(list, value)
        }
      }
    },
    total: (row, column) => {
      const fieldName = _getRenderNameByVs(column)

      if (isGroupTable) {
        const [groupFields] = columns
        const { fields } = groupFields.params

        // 当前数据在树的第几层
        const _level_ = row['_level_']
        if (typeof _level_ === 'undefined') return ''

        // 非日期分组显示 -
        const _field = fields[_level_]
        if (!isDateField(_field)) return '-'

        // 所有当前层的数据
        const flatOriginList = flat(listTree).filter(t => t._level_ === _level_)

        // 当前日期值
        const dateValue = row[TREE_GROUP_NAME]
        const list = flatOriginList
          .filter(t => {
            const _dateValue = t[TREE_GROUP_NAME]
            return new Date(dateValue) - new Date(_dateValue) >= 0
          })
          .map(t => t[fieldName])
          .filter(Boolean)

        return list.reduce((a, b) => a + b, 0)
      } else {
        // 查找最后一个日期分组的列
        const dateColumn = findLast(columns, t => {
          const _f = t.params.field
          return _f.category === CATEGORY.PROPERTY && isDateField(_f)
        })
        if (!dateColumn) return '-'

        // 日期的字段名
        const dateFieldName = dateColumn.params.field.renderName
        // 当前日期值
        const dateValue = row[dateFieldName]

        const list = listTree
          .filter(t => {
            const _dateValue = t[dateFieldName]

            return new Date(dateValue) - new Date(_dateValue) >= 0
          })
          .map(t => t[fieldName])

        return list.reduce((a, b) => a + b, 0)
      }
    },
    total_group: (row, column) => {
      const fieldName = _getRenderNameByVs(column)

      if (isGroupTable) {
        const [groupFields] = columns
        const { fields } = groupFields.params

        // 当前数据在树的第几层
        const _level_ = row['_level_']
        if (typeof _level_ === 'undefined') return ''

        // 非日期分组显示 -
        const _field = fields[_level_]
        if (!isDateField(_field)) return '-'

        // 当前日期值
        const dateValue = row[TREE_GROUP_NAME]

        const parent = _getByDfs(row[TREE_ROW_PARENT_KEY], TREE_ROW_KEY)
        if (!parent?.children.length) return row[fieldName]

        // 当前分组内的数据
        const list = parent.children
          .filter(t => {
            const _dateValue = t[TREE_GROUP_NAME]
            return new Date(dateValue) - new Date(_dateValue) >= 0
          })
          .map(t => t[fieldName])

        return list.reduce((a, b) => a + b, 0)
      } else {
        // 查找最后一个日期分组的列
        const dateColumn = findLast(columns, t => {
          const _f = t.params.field
          return _f.category === CATEGORY.PROPERTY && isDateField(_f)
        })
        if (!dateColumn) return '-'

        // 日期的字段名
        const dateFieldName = dateColumn.params.field.renderName
        // 当前日期值
        const dateValue = row[dateFieldName]

        // 日期字段的索引, 用作获取当前值的分组
        const dateFieldI = columns.findIndex(t => t.field === dateColumn.field)

        // 分组名
        const parentGroupName = _getGroupName(row, dateFieldI)
        // 获取树结构中的parent
        const parent = _getByDfs(parentGroupName, GROUP_PATH)
        if (!parent?.children.length) return row[fieldName]

        // 当前分组内的数据
        const list = parent.children
          .filter(t => {
            const _dateValue = t[TREE_GROUP_NAME]
            return new Date(dateValue) - new Date(_dateValue) >= 0
          })
          .map(t => t[fieldName])

        return list.reduce((a, b) => a + b, 0)
      }
    }
  }

  return map[quickType]
}

export const displayQuickCalculateValue = ({
  row,
  field,
  renderType = 'table',
  columns,
  listTree,
  summary,
  isGroupTable,
  special
}) => {
  const { renderName, fastCompute } = field
  const value = row[renderName]

  if (!fastCompute) return

  // 图表类型的按照明细表格的逻辑处理
  if (!isRenderTable(renderType)) {
    renderType = 'table'
  }

  const quickItem = quickCalculateMap({
    renderType,
    columns,
    listTree,
    quickType: fastCompute,
    summary,
    isGroupTable,
    special
  })
  if (typeof quickItem === 'undefined') return value

  return quickItem(row, field)
}

export const updateFieldsWithCompare = ({ fields = [], compare = {} }) => {
  let hasDelOriginField = false
  return fields.reduce((acc, cur) => {
    if (cur.children && cur.children.length) {
      cur.children = updateFieldsWithCompare({
        fields: cur.children,
        compare
      })
    }

    if (!is_vs(cur.renderName)) {
      hasDelOriginField = false
    } else {
      if (!hasDelOriginField) {
        const preCol = acc.pop()

        // 将上一列的字段加上对比字段字段中
        if (!cur.fields) {
          cur.fields = []
        }
        cur.fastCompute = preCol.fastCompute

        if (cur.fastCompute) {
          const opt = quickCalculateOptions.find(
            t => t.value === cur.fastCompute
          )
          cur.displayName = cur.displayName + '(' + opt.label + ')'
        }

        hasDelOriginField = true
      }
    }
    acc.push(cur)

    return acc
  }, [])
}

/**
 * 显示底部汇总索引单元格
 *
 * @param column 列配置对象，默认值为空对象
 * @param list 数据列表，默认值为空数组
 * @param summaryOptions 汇总选项列表，默认值为空数组
 * @param formatters 格式化器列表
 * @param datasetFields 数据集字段
 * @param summaryValue 汇总值
 * @returns 返回格式化后的汇总值
 */
export const displayBottomSummaryIndexCell = ({
  field = {},
  values,
  list = [],
  summaryOptions = [],
  formatters = [],
  datasetFields,
  summaryValue
}) => {
  // 自动汇总
  const autoComputeSummaryValue = summaryValue

  if (field.fastCompute) {
    return formatIndexDisplay({
      value: autoComputeSummaryValue,
      field,
      fields: datasetFields,
      formatters
    })
  } else {
    summaryOptions = summaryOptions.reduce((acc, cur) => {
      const { name = [], aggregator, _all } = cur
      if (_all) {
        return acc.concat({ name: field.renderName, aggregator })
      } else {
        return acc.concat(name.map(t => ({ name: t, aggregator })))
      }
    }, [])

    const item = summaryOptions.find(t => t.name === field.renderName)
    const aggregator = item?.aggregator

    let cValues = values
    if (!cValues) {
      cValues = list
        .filter(t => !t._isSummaryRow)
        .map(t => {
          // 交叉表格使用 renderNameByValue 取值
          const k = field.renderNameByValue ?? field.renderName
          return t[k]
        })
        .filter(
          t => typeof t !== 'undefined' && t !== null && typeof t !== 'string'
        )
    }

    return getDisplayBySummaryAggregator({
      values: cValues,
      aggregator,
      defaultValue: autoComputeSummaryValue
    })({ field, fields: datasetFields, formatters })
  }
}

/**
 * 获取汇总值
 *
 * @param values 值数组，默认为空数组
 * @param aggregator 汇总方式，默认为'SUM'，可选值有'SUM'、'AVG'、'MAX'、'MIN'、'COUNT'、'COUNT_DISTINCT'以及以QUANTILE_PREFIX为前缀的分位数，如'QUANTILE_0.5'表示中位数
 * @param defaultValue 自定义汇总函数返回值，当aggregator参数不合法时返回此值
 * @returns 汇总后的值
 */
export const getSummarizedValue = (values = [], aggregator, defaultValue) => {
  if (!aggregator || aggregator === 'auto') return defaultValue

  values = values.filter(
    t => typeof t !== 'undefined' && t !== null && typeof t !== 'string'
  )

  if (!values.length) return 0
  if (values.length === 1) return values[0]

  if (aggregator === 'SUM') return sum(values)
  if (aggregator === 'AVG') return avg(values)
  if (aggregator === 'MAX') return Math.max(...values)
  if (aggregator === 'MIN') return Math.min(...values)
  if (aggregator === 'COUNT') return values.length
  if (aggregator === 'COUNT_DISTINCT') return new Set(values).size
  if (aggregator.includes(QUANTILE_PREFIX)) {
    const [, number] = aggregator.split(QUANTILE_PREFIX)
    return quantile(values, +number)
  }

  return defaultValue
}

/**
 * 格式化指标显示
 *
 * @param param0 参数对象
 * @param param0.value 值
 * @param param0.field 字段信息
 * @param param0.fields 字段数组，默认为空数组
 * @param param0.formatters 格式化器数组，默认为空数组
 * @returns 格式化后的值
 */
export const formatIndexDisplay = ({
  value,
  field,
  fields = [],
  formatters = []
}) => {
  if (!field) return value

  if (field.category !== CATEGORY.INDEX) return value

  const originField = fields.find(t => t.name === field.name)
  if (!originField) return value

  // 当前配置的格式化
  let fConfig = formatters.find(t => t.field === field.renderName)
  if (!fConfig || fConfig.code === FORMAT_DEFAULT_CODE) {
    // 无格式化 或 默认，使用数据集字段的格式化
    fConfig = {
      field: field.renderName,
      code: originField.dataFormat,
      config: originField.customFormatConfig
    }
  }

  // 格式化配置
  const formatItem = formatterOptions.find(t => t.value === fConfig.code)
  if (!formatItem) return value

  return formatItem.format(value, fConfig.config)
}

/**
 * 根据给定行值、列和聚合器计算汇总值
 *
 * @param row 行数据
 * @param {?} values 值数组
 * @param columns 列数组
 * @param aggregator 聚合器，默认为'SUM'
 * @returns 汇总值
 */
export const displayColumnSummaryCell = ({
  row,
  values,
  columns,
  aggregator = 'SUM'
}) => {
  let cValues = values
  if (!cValues) {
    const indexColumns = flat(columns).filter(
      t => t.params?.field?.category === CATEGORY.INDEX
    )
    const fields = indexColumns.map(t => t.field)
    cValues = fields.map(t => row[t]).filter(t => typeof t !== 'undefined')
  }

  return getSummarizedValue(cValues, aggregator)
}

/**
 * 根据替换后的特殊字符获取列表
 *
 * @param params 参数对象，包含以下属性：
 * - columns 列信息数组，每个元素包含以下属性：
 * - category 类别
 * - dataSource 数据源数组
 * - config 配置对象
 * @param defaultVal 默认值 - 汇总需要默认值，不能为 空值
 * @returns 替换后的列表数组
 */
export const getListByReplacedSpecial = (
  { columns = [], dataSource = [], config },
  defaultVal
) => {
  return dataSource.map(row => {
    return row.reduce((acc, val, colIndex) => {
      return acc.concat(
        replaceSpecialBy(
          {
            value: val,
            category: columns[colIndex].category,
            config
          },
          defaultVal
        )
      )
    }, [])
  })
}

export const replaceSpecialBy = (
  { value, category, config: { dimension = 'original', measure = '0' } = {} },
  defaultVal
) => {
  if (!isSpecialNumber(value)) return value

  const defaultValue = value || defaultVal
  if (category === CATEGORY.PROPERTY)
    return dimension === 'original' ? defaultValue : dimension
  if (category === CATEGORY.INDEX)
    return measure === 'original' ? defaultValue : measure

  return defaultValue
}

/**
 * 根据聚合函数获取显示值
 *
 * @param values 值数组
 * @param aggregator 聚合函数
 * @param defaultValue 回调函数返回值
 * @returns 返回根据聚合函数获取显示值的函数
 */
export const getDisplayBySummaryAggregator = ({
  values,
  aggregator,
  defaultValue
}) => {
  return ({ field, fields, formatters = [] } = {}) => {
    const value = getSummarizedValue(values, aggregator, defaultValue)

    if (!field || aggregator === 'COUNT' || aggregator === 'COUNT_DISTINCT') {
      return value
    } else if (aggregator === 'AVG') {
      const maxV = Math.max(...values)
      // 使用最大的值求得格式化后的范围（避免千分位无法触发，对接下来的千分位的判断有影响）
      const fv = formatIndexDisplay({
        value: maxV,
        field,
        fields,
        formatters
      })
      if (typeof fv === 'number' && isNaN(fv)) return value

      const isThousand = String(fv).indexOf(',') > -1
      const isPercent = String(fv).indexOf('%') > -1

      return formatterByCustom(value, {
        type: isPercent ? 1 : 0,
        digit: 2,
        thousand: isThousand
      })
    } else {
      return formatIndexDisplay({
        value,
        field,
        fields,
        formatters
      })
    }
  }
}

/**
 * 获取比较差异数组
 *
 * @param {Array<number>} arr1 数组1
 * @param {Array<number>}arr2 数组2
 * @param {boolean} percent 是否返回差异百分比，默认为false
 * @returns {Array<number>} 返回差异数组
 */
export const getCompareDifferArray = (arr1 = [], arr2 = [], percent) => {
  return arr1.map((a1, i) => {
    const b1 = arr2[i] || 0
    const d = a1 - b1
    if (percent) {
      const r = d / Math.abs(b1)

      return Number.isFinite(r) ? r : 0
    } else {
      return d
    }
  })
}
