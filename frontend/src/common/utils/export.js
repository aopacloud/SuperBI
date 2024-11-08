import { getOnlyChild, getMaxDepth, getWordWidth } from 'common/utils/help'
import { utils, write, writeFile, writeFileXLSX } from 'xlsx-js-style'

const getPtWidth = s => getWordWidth(s, 1.25, 2.5)

/**
 * 将列数据转换为表头数据, 添加 rowSpan 和 colSpan, startRow, startCol
 * @param {Array<T>} cols 列数据数组
 * @returns {Array<T>} 转换后的表头数据数组
 */
export const transformColumns = cols => {
  const maxLevel = getMaxDepth(cols)

  /**
   * 将列表转换为表格头信息
   * @param l 列表数据
   * @param initR 初始行号
   * @param initC 初始列号
   * @returns 返回表格头信息数组
   */
  const _toHeaders = (l = [], initR = 0, initC = 0) => {
    let startC = initC

    return l.map((t, i) => {
      let startR = initR
      const oldSC = startC

      if (t.children?.length) {
        const children = _toHeaders(t.children, startR + 1, startC)
        const colSpans = children.reduce((a, t) => a + t.colSpan, 0)
        startC += colSpans

        return {
          title: t.title,
          startRow: startR,
          startCol: oldSC,
          rowSpan: 1,
          colSpan: colSpans,
          children,
        }
      } else {
        startC += 1

        return {
          title: t.title,
          startRow: startR,
          startCol: oldSC,
          rowSpan: maxLevel - startR,
          colSpan: 1,
        }
      }
    })
  }

  return _toHeaders(cols)
}

/**
 * 创建一个表格
 * @param {Array<Column>} columns 列定义数组，默认为空数组
 * @param  {Array<Record>} dataSource 数据源数组，默认为空数组
 * @param {{dataIndex: string, align: string, cellCb: (v, row, column) => any}} options 配置项对象，默认为空对象
 * @returns 生成的表格元素
 */
export const createTable = (columns = [], dataSource = [], options = {}) => {
  const table = document.createElement('table')

  table.setAttribute('border', 1)
  table.style = 'border-collapse: collapse'

  const { dataIndex = 'key', cellCb } = options

  // 创建表头
  const createTHead = columns => {
    let tHead = '<thead">'

    // 创建表头行
    const _createTr = (cols = []) => {
      if (!cols.length) return ''

      let tr = '<tr>',
        children = []

      cols.forEach(col => {
        tr += `<th rowspan=${col.rowSpan} colspan=${col.colSpan}>${col.title}</th>`

        if (col.children?.length) {
          children.push(...col.children)
        }
      })

      tr += '</tr>'
      tHead += tr

      children.length && _createTr(children)
    }

    _createTr(transformColumns(columns))
    tHead += '</thead>'

    return tHead
  }

  const createTBody = (columns, dataSource) => {
    const children = getOnlyChild(columns)

    if (!dataSource.length) return ''

    let tbody = '<tbody>'

    dataSource.forEach(row => {
      let tr = '<tr>'

      children.forEach(col => {
        let v = row[col[dataIndex]] || ''
        v = typeof cellCb === 'function' ? cellCb(v, row, col) : v
        tr += `<td>${v}</td>`
      })

      tr += '</tr>'
      tbody += tr
    })

    tbody += '</tbody>'
    return tbody
  }

  const thead = createTHead(columns)
  const tbody = createTBody(columns, dataSource)

  table.innerHTML = `<table>${thead}${tbody}</table>`
  return table
}

/**
 * 通过创建表格导出Excel文件
 * @param {Array<Column>} columns 列名数组
 * @param {Array<Record>} dataSource 数据源数组
 * @param {any} options 导出选项对象
 * @returns 无返回值，直接导出Excel文件
 */
export const exportByTable = (columns = [], dataSource = [], options = {}) => {
  const table = createTable(columns, dataSource, options)
  const wb = utils.table_to_book(table)
  writeFile(wb, 'SheetJSTable.xlsx')
}

/**
 * 创建表格的表头
 * @param {Array<Column>} columns 表格的列信息数组，默认为空数组
 * @param {{align: string}} options 表格的选项配置对象，默认为空对象
 * @returns 返回包含表头名称、合并单元格和子节点列表的对象
 */
export const createHeader = (columns = [], options = {}) => {
  const newColumns = transformColumns(columns)

  // 所有的child
  const childList = getOnlyChild(columns)

  /**
   * 生成表格表头配置
   * @param {Array<Column>} cols 表格列配置数组
   * @returns 返回一个包含表头名称和合并单元格信息的对象
   */
  const createHeaderConfig = (cols = []) => {
    const names = [],
      merges = []

    // 表头单元格边框样式
    const thBorderStyle = { style: 'thin', color: { rgb: '666666' } }
    const thBorder = {
      top: thBorderStyle,
      bottom: thBorderStyle,
      left: thBorderStyle,
      right: thBorderStyle,
    }
    // 表头单元格样式
    const th = {
      border: thBorder,
      fill: { fgColor: { rgb: 'cccccc' } },
      alignment: {
        vertical: 'center',
        horizontal: options.align ?? 'left',
      },
    }

    const _setHeader = (list = [], maxChildLength) => {
      const res = [],
        children = []

      list.forEach(item => {
        const { colSpan, startCol, rowSpan, startRow } = item

        res[startCol] = { v: item.title, s: th }

        // 行合并
        if (rowSpan > 1) {
          merges.push({
            s: { r: startRow, c: startCol },
            e: { r: startRow + rowSpan - 1, c: startCol + colSpan - 1 },
          })
        }

        // 列合并
        if (colSpan > 1) {
          merges.push({
            s: { r: startRow, c: startCol },
            e: { r: startRow + rowSpan - 1, c: startCol + colSpan - 1 },
          })

          // 列合并后，补空单元格
          res.push(...Array(colSpan - 1).fill({ v: null, s: th }))
        }

        if (item.children?.length) children.push(...item.children)
      })

      if (res.length) {
        // 补全空单元格至childList长度
        if (res.length < maxChildLength) res[maxChildLength - 1] = null

        names.push([...res].map(t => t ?? { v: null, s: th }))
      }
      if (children.length) _setHeader(children, maxChildLength)
    }

    // 设置表头
    _setHeader(cols, childList.length)

    return { names, merges }
  }

  const { names, merges } = createHeaderConfig(newColumns)

  return { headerNames: names, merges, childList }
}

/**
 * 创建数据表格
 * @param {Array<Column>} columns 列配置数组，默认为空数组
 * @param data 数据数组，默认为空数组
 * @param {{dataIndex: string, align: string, cellCb?: (v, row, column) => string}} options 配置对象，默认为空对象
 * @returns 返回二维数组，包含表格数据
 */
export const createData = (columns = [], data = [], options = {}) => {
  const { dataIndex = 'key', align = 'left', cellCb } = options
  const td = {
    t: 's',
    s: {
      alignment: {
        vertical: 'center',
        horizontal: align,
      },
    },
  }

  return data.map(row =>
    columns.map(col => {
      const val = row[col[dataIndex]] || ''

      if (typeof cellCb === 'function') {
        const cell = cellCb(val, row, col)

        return { ...td, ...cell }
      } else {
        return { ...td, v: val }
      }
    })
  )
}

/**
 * 导出数据为Excel文件
 * @param {Array<Column>} columns 列配置数组，默认为空数组
 * @param {Array<Record>} dataSource 数据源数组，默认为空数组
 * @param {} options 导出选项，默认为空对象
 * @returns 无返回值
 */

export const exportByData = (columns = [], dataSource = [], options = {}) => {
  return new Promise((resolve, reject) => {
    // 创建表头名，表头合并，子节点数据
    const { headerNames, merges, childList } = createHeader(columns, options)

    // 创建数据
    const dataList = createData(childList, dataSource, options)

    /* 生成 workbook and worksheet  */
    const wb = utils.book_new()
    const ws = utils.aoa_to_sheet([...headerNames, ...dataList], {
      skipHeader: true,
    })

    // 合并
    ws['!merges'] = merges

    // 工作表插入sheet
    utils.book_append_sheet(wb, ws, 'Sheet1')

    // 设置列宽
    if (options.tableLayout) {
      const childWidths = childList.map((child, ci) => {
        const titleWidth = getPtWidth(child.title)
        const colDataWidth = dataList.map(row => {
          const cell = row[ci]
          const v = typeof cell === 'object' ? cell.v : cell
          return getPtWidth(v)
        })
        return Math.max(titleWidth, ...colDataWidth)
      })

      // 等宽
      if (options.tableLayout === 'fixed') {
        const maxChildWidth = Math.max(...childWidths)
        ws['!cols'] = childWidths.map(() => ({ wch: maxChildWidth }))
      } else {
        ws['!cols'] = childWidths.map(width => ({ wch: width }))
      }
    }

    // 设置导出文件名
    let { filename = Date.now() + '.xlsx', bookType = 'xlsx' } = options
    if (!filename.endsWith(bookType)) filename += '.' + bookType

    // 只有在主线程才可以导出 (webwork 无法导出文件 https://docs.sheetjs.com/docs/demos/bigdata/worker#creating-a-local-file )
    if (typeof window !== 'undefined' && typeof document !== 'undefined') {
      // writeFileXLSX 无法写入样式??
      const blobUrl = writeFile(wb, filename, {
        bookType,
        cellFormula: true,
        cellStyles: true,
      })

      resolve(blobUrl)
    } else {
      /* 生成 xlsx 数据 (Uint8Array) */
      const u8 = write(wb, { bookType, type: 'buffer' })
      /* 创建 blobURL */
      const url = URL.createObjectURL(new Blob([u8]))

      resolve({ url, filename })
      cb && cb({ url, filename })
    }
  })
}
