/**
 * 该文件依赖较多其他模块的方法和配置项，不适合放在webworker中
 */

import { CATEGORY, COMPARE } from '@/CONST.dict'
import {
  formatDtWithOption,
  VS_FIELD_SUFFIX,
  formatFieldDisplay
} from '../utils/index'
import {
  getCompareDisplay,
  displayQuickCalculateValue,
  listDataToTreeByKeys,
  displayBottomSummaryIndexCell,
  displayColumnSummaryCell,
  getSummarizedValue,
  getDisplayBySummaryAggregator,
  getCompareDifferArray
} from './utils'
import {
  FORMAT_DEFAULT_CODE,
  FORMAT_CUSTOM_CODE
} from '@/views/dataset/config.field'
import { createSortByOrders, getDiffColor, deepClone } from 'common/utils/help'
import { downloadByUrl } from 'common/utils/file'
import { TREE_GROUP_NAME } from '../utils/createGroupTable'
import { toPercent, toDigit } from 'common/utils/number'

let worker = null
/**
 * 初始化Web Worker
 */
export const initWorker = () => {
  worker = new Worker(new URL('@/worker.js', import.meta.url), {
    type: 'module'
  })

  worker.addEventListener('message', e => {
    const { type, payload, error } = e.data
    if (error) throw Error(error)

    if (type === 'exportResult') {
      const { url, filename } = payload

      downloadByUrl(url, filename)
    }
  })

  worker.addEventListener('error', e => {
    console.error('worker init error: ', e)
  })
}

/**
 * 销毁 worker 线程
 */
export const uninitWorker = () => {
  worker?.terminate()
  worker = null
}

const indexFormatMap = {
  // 默认格式，整数部分最多显示3位，小数按照原值的位数显示
  [FORMAT_DEFAULT_CODE]: ({ value }) => {
    const digit = String(value).split('.')[1]?.length || 0

    if (!digit) return '#,##0'

    return '#,##0' + '.0'.padEnd(digit + 1, '0')
  },
  INTEGER: '#,##0',
  DECIMAL_1: '#,##0.0',
  DECIMAL_2: '#,##0.00',
  PERCENT: '0.00%',
  PERCENT_DECIMAL_1: '0.0%',
  PERCENT_DECIMAL_2: '0.00%',
  // 自定义
  [FORMAT_CUSTOM_CODE]: ({ config }) => {
    config = typeof config === 'string' ? JSON.parse(config) : config
    const { type = 0, thousand = true, digit = 2 } = config
    const thousandStr = thousand ? '#,##0' : ''

    // 数字
    if (type === 0) {
      let digitStr = digit > 0 ? '.0'.padEnd(digit + 2, '0') : ''
      if (!thousandStr) digitStr = '0' + digitStr

      return thousandStr + digitStr
    } else {
      let percentStr = digit > 0 ? '.0'.padEnd(digit + 2, '0') + '%' : '.00%'
      if (!thousandStr) percentStr = '0' + percentStr

      return thousandStr + percentStr
    }
  }
}

/**
 * 获取格式化字符串
 * @param value 需要格式化的值
 * @param code 格式化代码，默认为 FORMAT_DEFAULT_CODE
 * @param config 格式化配置对象
 * @returns 格式化后的字符串
 */
const getFormatStr = (value, code = FORMAT_DEFAULT_CODE, config) => {
  const format = indexFormatMap[code]
  if (typeof format === 'function') return format({ value, config })

  return format
}

/**
 * 导出文件
 * @param {{data: Array<Record>, columns: Array<Column>, summary: Record}} param0 包含数据、列和汇总信息的对象
 * @param {string} filename 文件名，默认为当前时间戳
 */
export default function transformExport({
  data = [],
  columns = [],
  summary = {},
  dataset = {},
  tableConfig = {},
  sorters = [],
  formatters = [],
  filename = Date.now()
}) {
  if (!worker) {
    initWorker()
  }

  const renderType = tableConfig.renderType

  const tableTypes = ['table', 'bar', 'line']

  const listTree = tableTypes.includes(renderType)
    ? listDataToTreeByKeys({
        renderType: renderType,
        list: data,
        groupKeys: columns
          .filter(t => t.params?.field?.category === CATEGORY.PROPERTY)
          .map(t => t.params.field.renderName),
        summaryMap: summary
      })
    : data

  // 数据集字段
  const fields = dataset.fields || []

  // 获取合并的排序
  const getMergeSorters = () => {
    let oldSorters = []
    // 交叉表格
    if (
      renderType === 'intersectionTable' ||
      renderType === 'bar' ||
      renderType === 'line'
    ) {
      const { row = [], column = [] } = deepClone(sorters)
      oldSorters = row
        .map(t => ({ ...t, _group: 'row' }))
        .concat(column.map(t => ({ ...t, _group: 'column' })))
    } else {
      oldSorters = deepClone(sorters)
    }

    // 行列转置的下载排序不应用表格表头的排序
    const tableSorter =
      !tableConfig.sorter || tableConfig.reverse
        ? []
        : Array.isArray(tableConfig.sorter)
          ? tableConfig.sorter
          : [tableConfig.sorter]

    const newSorters = tableSorter.filter(t => !!t.order)

    return [...oldSorters, ...newSorters].reduce((acc, cur) => {
      const item = acc.find(t => t.field === cur.field)

      if (!item) {
        acc.push({
          ...cur,
          field:
            renderType === 'table'
              ? cur.field
              : cur._group === 'column'
                ? 'title'
                : TREE_GROUP_NAME
        })
      } else {
        item.order = cur.order
        if (cur.order !== 'custom') {
          item.custom = undefined
        }
      }

      return acc
    }, [])
  }

  // 排序
  const mergedSorters = getMergeSorters()
  if (mergedSorters.length) {
    data = data.sort(createSortByOrders(mergedSorters))
  }

  /**
   * 获取字段格式化字符串
   * @param val 字段值
   * @param field 字段对象
   * @returns 格式化后的字符串
   */
  const getFieldFormatStr = (val, field) => {
    const originField = fields.find(t => t.name === field.name)
    if (!originField) getFormatStr(val)

    let formatItem = formatters.find(t => t.field === field.renderName)
    if (!formatItem || !formatItem.code) {
      formatItem = {
        field: field.renderName,
        code: originField.dataFormat,
        config: originField.customFormatConfig
      }
    }

    return getFormatStr(val, formatItem.code, formatItem.config)
  }

  const summaryConfig = tableConfig.summary
  let rowSummary = {},
    columnSummary = {}
  if (Array.isArray(summaryConfig)) {
    rowSummary = summaryConfig
  } else {
    const { row = {}, column } = summaryConfig
    rowSummary = row
    columnSummary = column
  }

  // 列汇总（也可以在数据填充时去处理）
  // if (columnSummary?.enable) {
  //   const fn = (list = []) => {
  //     return list.map(item => {
  //       return {
  //         ...item,
  //         children: fn(item.children),
  //         column_summary: displayColumnSummaryCell({
  //           row: item,
  //           columns,
  //           summaryConfig: columnSummary
  //         })
  //       }
  //     })
  //   }
  //   data = fn(data)
  // }

  const hasRowSummary = rowSummary.enable
  const tableData = hasRowSummary
    ? [...data, { ...summary, _isSummaryRow: true }]
    : data

  const listToTree = (list = [], children) => {
    const fn = (l = []) => {
      if (!l.length) return children
      const [f, ...rest] = l
      return [
        {
          ...f,
          children: fn(rest)
        }
      ]
    }
    return fn(list)
  }

  // 列分组排序
  const columnSorters = mergedSorters.filter(t => t._group === 'column')
  if (columnSorters.length) {
    const sortedColumns = arr => {
      // 行， 列
      const [rows, cols] = arr.reduce(
        (acc, cur) => {
          if (cur.field === TREE_GROUP_NAME) {
            acc[0].push(cur)
          } else {
            acc[1].push(cur)
          }
          return acc
        },
        [[], []]
      )

      const fn = (list, sorts) => {
        return list
          .map(t => {
            if (t.children?.length) {
              return {
                ...t,
                children: fn(t.children, sorts)
              }
            } else {
              return t
            }
          })
          .sort(createSortByOrders(columnSorters))
      }

      return rows.concat(fn(cols, toRaw(sorters)))
    }

    columns = sortedColumns(columns)
  }

  // 分组表头的字段展开
  const expandTreeNodeColumns = columns.reduce((acc, col) => {
    const { params: { fields = [], _columnFields = [] } = {} } = col

    let item = {}
    if (_columnFields.length) {
      item = listToTree(_columnFields, fields.length ? [...fields] : [col])
    } else {
      item = [col]
    }
    acc.push(...item)

    return acc
  }, [])

  const expandChildren = list => {
    return list.reduce((acc, row) => {
      const { children = [] } = row
      if (children.length) {
        acc.push(...expandChildren(children))
      } else {
        acc.push(row)
      }
      return acc
    }, [])
  }

  const expandChildrenColumns = expandChildren(expandTreeNodeColumns)

  const _isColumnSummaryFirst =
    columnSummary?.enable && columnSummary?.position === 'first'

  // 获取同环比单元格显示
  const getVsCellDisplay = (
    cell,
    {
      origin,
      target,
      field,
      aggregator,
      originValues = [],
      targetValues = [],
      colored = true
    },
    { merge, mode }
  ) => {
    if (colored) {
      const color = getDiffColor(origin, target)
      cell.s.font = { color: { rgb: color.split('#')[1] } }
    }

    let originStr
    // 汇总
    if (aggregator && mode !== COMPARE.MODE.ORIGIN) {
      const differ = getCompareDifferArray(
        targetValues,
        originValues,
        mode === COMPARE.MODE.DIFF_PERSENT
      )

      originStr = getSummarizedValue(differ, aggregator)

      if (mode === COMPARE.MODE.DIFF_PERSENT) {
        originStr = toPercent(originStr, 2)
      } else {
        originStr = toDigit(originStr, 2)
      }
    }

    // 拆分显示
    if (!merge) {
      if (mode === COMPARE.MODE.ORIGIN) {
        cell = {
          ...cell,
          t: 'n',
          v: origin,
          z: getFieldFormatStr(origin, field)
        }
      } else {
        cell = {
          ...cell,
          v:
            originStr ||
            getCompareDisplay({
              origin,
              target,
              mode,
              config: tableConfig
            })(field, fields)
        }
      }
    } else {
      // 合并显示
      const targetStr = formatFieldDisplay(target, field, fields)
      const r =
        originStr ||
        getCompareDisplay({
          origin,
          target,
          mode,
          config: tableConfig
        })(field, fields)

      cell.v = targetStr + '(' + r + ')'
    }

    return cell
  }

  const toList = (list = []) => {
    return list.reduce((acc, row) => {
      const r = expandChildrenColumns.reduce((a, col, colIndex, arr) => {
        // 单元格对象 https://docs.sheetjs.com/docs/csf/cell
        let cell = {
          t: 's',
          s: {
            alignment: {
              vertical: 'center',
              horizontal: tableConfig.align ?? 'left'
            }
          }
        }

        const {
          field: renderName,
          slots,
          params: { field }
        } = col
        const { default: sDefault } = slots
        const colVal = row[renderName] ?? '' // excel填充空值单元格
        const isSummary = row._isSummaryRow

        // 列汇总
        if (sDefault === 'columnSummary') {
          if (!isSummary) {
            // 列汇总（也可以提前处理完数据）
            const cV = displayColumnSummaryCell({
              row,
              columns: arr,
              aggregator: columnSummary.aggregator
            })

            cell = {
              ...cell,
              t: 'n',
              v: cV,
              z: getFormatStr(cV)
            }
          }
        } else if (isSummary) {
          // 行汇总
          let i = 0
          if (_isColumnSummaryFirst) i++

          if (colIndex === i) {
            // 汇总的第一列
            cell.v = '汇总'
          } else {
            if (field.category === CATEGORY.INDEX) {
              if (sDefault !== 'vs') {
                cell.v = displayBottomSummaryIndexCell({
                  field,
                  list,
                  summaryOptions: rowSummary?.list,
                  formatters,
                  datasetFields: fields,
                  summaryValue: colVal
                })
              } else {
                const mode = tableConfig.compare.mode

                const dataList = list.filter(t => !t._isSummaryRow)
                const tName = renderName.split(VS_FIELD_SUFFIX)[0]
                const tValues = dataList.map(t => t[tName])
                const oValues = dataList.map(t => t[renderName])
                let target = row[tName],
                  origin = colVal

                let summaryOptions = rowSummary?.list || []
                summaryOptions = summaryOptions.reduce((acc, cur) => {
                  const { name = [], aggregator, _all } = cur
                  if (_all) {
                    return acc.concat({ name: tName, aggregator })
                  } else {
                    return acc.concat(name.map(t => ({ name: t, aggregator })))
                  }
                }, [])

                const item = summaryOptions.find(t => t.name === tName)
                const aggregator = item?.aggregator

                // 自动汇总
                if (!aggregator || aggregator === 'auto') {
                  return getCompareDisplay({
                    origin,
                    target,
                    mode,
                    config: tableConfig
                  })(field, fields)
                }

                // 这里不需要获取格式化的值
                // field,
                // fields,
                // formatters
                target = getDisplayBySummaryAggregator({
                  values: tValues,
                  aggregator,
                  defaultValue: target
                })()

                origin = getDisplayBySummaryAggregator({
                  values: oValues,
                  aggregator,
                  defaultValue: origin
                })()

                cell = getVsCellDisplay(
                  cell,
                  {
                    origin,
                    target,
                    field,
                    aggregator,
                    colored: false,
                    originValues: oValues,
                    targetValues: tValues
                  },
                  tableConfig.compare
                )
              }
            }
          }
        } else if (sDefault === 'date') {
          // 日期
          cell.v = formatDtWithOption(colVal, field)
        } else if (sDefault === 'vs') {
          const origin = colVal
          const target = row[renderName.split(VS_FIELD_SUFFIX)[0]]

          cell = getVsCellDisplay(
            cell,
            { origin, target, field },
            tableConfig.compare
          )
        } else if (sDefault === 'quickCalculate') {
          if (!isSummary) {
            // 快速计算
            cell.v = displayQuickCalculateValue({
              row,
              field,
              renderType,
              columns: arr,
              listTree,
              summary,
              isGroupTable: tableConfig.isGroupTable,
              special: tableConfig.special
            })
          } else {
            cell = {
              ...cell,
              t: 'n',
              v: colVal,
              z: getFieldFormatStr(colVal, field)
            }
          }
        } else if (field.category === CATEGORY.INDEX) {
          // 指标
          cell = {
            ...cell,
            ...{ t: 'n', v: colVal, z: getFieldFormatStr(colVal, field) }
          }
        } else {
          // 默认
          cell.v = colVal
        }

        a.push(cell)

        return a
      }, [])

      if (row.children?.length) {
        acc.push(...toList(row.children))
      } else {
        acc.push(r)
      }

      return acc
    }, [])
  }

  // 处理导出的数据
  const formattedData = toList(tableData)

  const name = filename
    ? filename + '-' + new Date().toLocaleString()
    : new Date().toLocaleString()

  worker.postMessage({
    type: 'export',
    payload: {
      data: formattedData,
      columns: expandTreeNodeColumns,
      tableLayout: tableConfig.layout,
      options: {
        alignment: tableConfig.align,
        filename: name
      }
    }
  })
}
