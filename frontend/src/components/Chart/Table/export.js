import { utils, write, writeFileXLSX } from 'xlsx-js-style'
import { getWordWidth } from 'common/utils/help'

const getPtWidth = str => getWordWidth(str, 1.75, 2.25)

const getOnlyChild = (l = []) => {
  return l.reduce(
    (a, t) => a.concat(t.children?.length ? getOnlyChild(t.children) : t),
    []
  )
}

export default function exportExcel(payload = {}, cb) {
  try {
    return new Promise((resolve, reject) => {
      const { data = [], columns = [], tableLayout, options = {} } = payload
      // 创建工作表
      const wb = utils.book_new()

      /**
       * 将嵌套层级数组转换成表头格式，并返回最大层级、转换后的表头数组、表头名称数组和合并单元格信息数组
       * @param list 嵌套层级数组
       * @returns 包含最大层级、表头数组、表头名称数组和合并单元格信息数组的对象
       */
      const transformHeader = list => {
        // 获取最大深度
        const getMaxDepth = (list, init = 1) =>
          list.reduce((a, b) => {
            if (b.children?.length) {
              return getMaxDepth(b.children, init + 1)
            } else {
              return Math.max(init, a)
            }
          }, init)

        const maxLevel = getMaxDepth(list)

        // 获取合计colSpan
        const getColSpanSum = l => l.reduce((a, b) => a + b.colSpan, 0)

        /**
         * 将层级数组转换成表头格式
         * @param l 层级数组
         * @param initRow 初始行号
         * @param initCol 初始列号
         * @returns 转换后的表头数组
         */
        const toHeaders = (l = [], initRow = 0, initCol = 0) => {
          let startCol = initCol
          const res = l.map(t => {
            let startRow = initRow

            if (t.children?.length) {
              let oldStart = startCol

              const res = toHeaders(t.children, startRow + 1, startCol)
              const colSpan = getColSpanSum(res)
              startCol += colSpan

              return {
                title: t.title,
                colSpan,
                rowSpan: 1,
                children: res,
                startRow,
                startCol: oldStart
              }
            } else {
              let oldStart = startCol

              startCol += 1

              return {
                title: t.title,
                rowSpan: maxLevel - startRow,
                colSpan: 1,
                startRow,
                startCol: oldStart
              }
            }
          })

          return res
        }

        /**
         * 生成表格表头配置
         * @param {Array<Column>} cols 表格列配置数组
         * @returns 返回一个包含表头名称和合并单元格信息的对象
         */
        const toNamesAndMerges = (cols = []) => {
          const names = [],
            merges = []

          // 表头单元格边框样式
          const thBorderStyle = { style: 'thin', color: { rgb: '666666' } }
          const thBorder = {
            top: thBorderStyle,
            bottom: thBorderStyle,
            left: thBorderStyle,
            right: thBorderStyle
          }
          // 表头单元格样式
          const th = {
            border: thBorder,
            fill: { fgColor: { rgb: 'cccccc' } },
            alignment: {
              vertical: 'center',
              horizontal: options.alignment ?? 'left'
            }
          }

          const _transformHeaders = (list = [], maxChildLength) => {
            const res = [],
              children = []

            list.forEach(item => {
              const { colSpan, startCol, rowSpan, startRow } = item

              res[startCol] = { v: item.title, s: th }

              // 行合并
              if (rowSpan > 1) {
                merges.push({
                  s: { r: startRow, c: startCol },
                  e: { r: startRow + rowSpan - 1, c: startCol + colSpan - 1 }
                })
              }

              // 列合并
              if (colSpan > 1) {
                merges.push({
                  s: { r: startRow, c: startCol },
                  e: { r: startRow + rowSpan - 1, c: startCol + colSpan - 1 }
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
            if (children.length) _transformHeaders(children, maxChildLength)
          }

          // 设置表头
          _transformHeaders(cols, getOnlyChild(cols).length)

          return { names, merges }
        }

        const headers = toHeaders(list)
        const { names, merges } = toNamesAndMerges(headers)

        return { maxLevel, headers, names, merges }
      }
      const { names, merges } = transformHeader(columns)

      const ws = utils.aoa_to_sheet([...names, ...data], { skipHeader: true })

      // 添加合并单元格
      ws['!merges'] = merges

      // 工作表插入sheet
      utils.book_append_sheet(wb, ws, 'Sheet1')

      // 设置列宽
      if (tableLayout) {
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

        const expandChildrenColumns = expandChildren(columns)

        const columnsWidth = expandChildrenColumns.map((col, colIndex) => {
          const titleWidth = getPtWidth(col.title)
          const colWidth = data.map(row => {
            const c = row[colIndex]
            const v = typeof c === 'object' ? c.v : c

            return getPtWidth(v)
          })

          return Math.max(titleWidth, ...colWidth)
        })

        // 自动
        if (tableLayout === 'auto') {
          ws['!cols'] = columnsWidth.map(w => ({ wch: w }))
        } else if (tableLayout === 'fixed') {
          // 等宽
          const maxWidth = Math.max(...columnsWidth)

          ws['!cols'] = expandChildrenColumns.map(t => ({ wch: maxWidth }))
        }
      }

      // freeze 冻结（开源版本不支持）
      // ws['!freeze'] = {
      //   xSplit: 1, // 第一列
      //   ySplit: maxLevel, // headerRows是表头有几行
      //   topLeftCell: 'B2',
      //   activePane: 'bottomRight',
      //   state: 'frozen',
      // }

      let { filename = Date.now() + '.xlsx', bookType = 'xlsx' } = options
      if (!filename.endsWith(bookType)) {
        filename += '.' + bookType
      }

      // 只有在主线程才可以导出 (webwork 无法导出文件 https://docs.sheetjs.com/docs/demos/bigdata/worker#creating-a-local-file )
      if (typeof window !== 'undefined' && typeof document !== 'undefined') {
        const blobUrl = writeFileXLSX(wb, filename, {
          bookType,
          cellFormula: true,
          cellStyles: true
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
  } catch (error) {
    reject(error)
    cb && cb({ error })
    throw Error(error)
  }
}
