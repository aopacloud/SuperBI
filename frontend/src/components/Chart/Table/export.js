import { utils, write, writeFileXLSX } from 'xlsx'
import { getWordWidth } from 'common/utils/help'

const getPtWidth = str => getWordWidth(str, 1.25, 2.5)

export default function exportExcel(payload = {}, cb) {
  try {
    return new Promise((resolve, reject) => {
      const { data = [], columns = [], tableLayout, options = {} } = payload
      // 创建工作表
      const wb = utils.book_new()

      // json to sheet
      const headerNames = columns.map(col => col.title)
      const ws = utils.aoa_to_sheet([headerNames, ...data], { cellFormula: true })

      // 设置列宽
      if (tableLayout) {
        const columnsWidth = columns.map((col, colIndex) => {
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

          ws['!cols'] = columns.map(t => ({ wch: maxWidth }))
        }
      }

      // 工作表插入sheet
      utils.book_append_sheet(wb, ws, 'Sheet1')

      let { filename = Date.now() + '.xlsx', bookType = 'xlsx' } = options
      if (!filename.endsWith(bookType)) {
        filename += '.' + bookType
      }

      // 只有在主线程才可以导出 (webwork 无法导出文件 https://docs.sheetjs.com/docs/demos/bigdata/worker#creating-a-local-file )
      if (typeof window !== 'undefined' && typeof document !== 'undefined') {
        const blobUrl = writeFileXLSX(wb, filename, { bookType, cellFormula: true })

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
