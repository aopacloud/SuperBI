/**
 * 该文件依赖较多其他模块的方法和配置项，不适合放在webworker中
 */

import { CATEGORY } from '@/CONST.dict'
import {
  formatDtWithOption,
  VS_FIELD_SUFFIX,
  formatFieldDisplay,
} from '../utils/index'
import {
  getCompareDisplay,
  displayQuickCalculateValue,
  listDataToTreeByKeys,
} from './utils'
import {
  formatterOptions,
  FORMAT_DEFAULT_CODE,
  FORMAT_CUSTOM_CODE,
} from '@/views/dataset/config.field'
import { createSortByOrder } from 'common/utils/help'
import { downloadByUrl } from 'common/utils/file'

let worker = null
/**
 * 初始化Web Worker
 */
export const initWorker = () => {
  worker = new Worker(new URL('@/worker.js', import.meta.url), {
    type: 'module',
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
  },
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
  config = {},
  filename = Date.now(),
}) {
  if (!worker) {
    initWorker()
  }

  const tableConfig = config.table || {}
  const listTree = tableConfig.isGroupTable
    ? data
    : listDataToTreeByKeys({
        renderType: config.renderType,
        list: data,
        groupKeys: columns
          .filter(t => t.params.field.category === CATEGORY.PROPERTY)
          .map(t => t.params.field.renderName),
        summaryMap: summary,
      })

  // 数据集字段
  const fields = dataset.fields || []

  // 排序
  const {
    sorter,
    sorter: { order: sOrder, field: sField },
  } = tableConfig
  if (sorter) {
    data = data.sort(createSortByOrder(sOrder === 'asc', sField))
  }

  const displayIndexFormat = (value, field) => {
    // 指标
    const originField = fields.find(t => t.name === field.name)
    if (!originField) return value

    const formatter = tableConfig.formatter || []
    let formatItem = formatter.find(t => t.field === field.renderName)
    if (!formatItem || !formatItem.code) {
      formatItem = {
        field: field.renderName,
        code: originField.dataFormat,
        config: originField.customFormatConfig,
      }
    }

    const formatOpt = formatterOptions.find(t => t.value === formatItem.code)
    if (!formatOpt) {
      return value
    } else {
      return formatOpt.format(value, formatItem.config)
    }
  }

  const summaryRows = columns.reduce((acc, cur, i) => {
    acc[cur.field] = i === 0 ? '汇总' : summary[cur.field]
    return acc
  }, [])

  /**
   * 获取字段格式化字符串
   * @param val 字段值
   * @param field 字段对象
   * @returns 格式化后的字符串
   */
  const getFieldFormatStr = (val, field) => {
    const originField = fields.find(t => t.name === field.name)
    if (!originField) getFormatStr(val)

    const formatter = tableConfig.formatter || []
    let formatItem = formatter.find(t => t.field === field.renderName)
    if (!formatItem || !formatItem.code) {
      formatItem = {
        field: field.renderName,
        code: originField.dataFormat,
        config: originField.customFormatConfig,
      }
    }

    return getFormatStr(val, formatItem.code, formatItem.config)
  }

  const tableData = tableConfig.showSummary
    ? [...data, { ...summary, _isSummaryRow: true }]
    : data

  // 处理导出的数据
  const formattedData = tableData.map(row => {
    return columns.reduce((acc, col, colIndex, arr) => {
      const {
        field: renderName,
        slots,
        params: { field },
      } = col
      const { default: sDefault } = slots
      const colVal = row[renderName]
      const isSummary = row._isSummaryRow

      if (isSummary && colIndex === 0) {
        // 汇总的第一列
        acc[colIndex] = '汇总'
      } else if (sDefault === 'date') {
        acc[colIndex] = { t: 's', v: formatDtWithOption(colVal, field) }
      } else if (sDefault === 'vs') {
        // 同环比
        const { mode, merge } = config.compare || {}
        const origin = colVal
        const target = row[renderName.split(VS_FIELD_SUFFIX)[0]]

        // 拆分显示
        if (!merge) {
          if (mode === 0) {
            acc[colIndex] = {
              t: 'n',
              v: origin,
              z: getFieldFormatStr(origin, field),
            }
          } else {
            acc[colIndex] = {
              v: getCompareDisplay(origin, target, mode)(field, fields),
            }
          }
        } else {
          // 合并显示
          const targetStr = formatFieldDisplay(target, field, fields)

          acc[colIndex] = {
            t: 's',
            v:
              targetStr +
              '(' +
              getCompareDisplay(origin, target, mode)(field, fields) +
              ')',
          }
        }
      } else if (sDefault === 'quickCalculate') {
        if (!isSummary) {
          // 快速计算
          acc[colIndex] = {
            t: 's',
            v: displayQuickCalculateValue({
              row,
              field,
              renderType: config.renderType,
              columns: arr,
              listTree,
              summary,
              isGroupTable: tableConfig.isGroupTable,
            }),
          }
        } else {
          acc[colIndex] = {
            t: 'n',
            v: colVal,
            z: getFieldFormatStr(colVal, field),
          }
        }
      } else if (field.category === CATEGORY.INDEX) {
        // 指标
        acc[colIndex] = {
          t: 'n',
          v: colVal,
          z: getFieldFormatStr(colVal, field),
        }
      } else {
        // 默认
        acc[colIndex] = colVal
      }

      return acc
    }, [])
  })

  const name = filename
    ? filename + '-' + new Date().toLocaleString()
    : new Date().toLocaleString()

  worker.postMessage({
    type: 'export',
    payload: {
      data: formattedData,
      columns,
      tableLayout: tableConfig.layout,
      options: {
        filename: name,
      },
    },
  })
}
