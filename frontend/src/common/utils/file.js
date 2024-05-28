/**
 * // 下载文件
 * @param {blob} blobBit 文件流（二进制）
 * @param {string} fileName 保存的文件名
 * @returns
 */
export function downloadWithBlob(blobBit, fileName) {
  return new Promise((resolve, reject) => {
    try {
      const blob = new Blob([blobBit]) //处理文档流
      let elink = document.createElement('a')

      elink.download = fileName
      elink.style.display = 'none'
      elink.href = URL.createObjectURL(blob)

      document.body.appendChild(elink)
      elink.click()

      URL.revokeObjectURL(elink.href) // 释放URL 对象
      document.body.removeChild(elink)
      elink = null

      resolve()
    } catch (error) {
      reject(error)
    }
  })
}

/**
 * 导出文件
 * @param {string|File|Blob} content 文件内容(字符串)、文件、blob对象
 * @param {{name: string, type: string, suffix: string}} {name, type, suffix} 配置参数
 */
export const exportFile = (
  content = '',
  { name = Date.now(), type = 'text/plain', suffix = '.xlsx' } = {}
) => {
  const download = url => {
    const a = document.createElement('a')
    a.style = 'display: none'
    a.download = name + suffix
    a.target = '_blank'
    a.href = url

    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  }

  if (typeof content === 'string') {
    const prefix = 'data:application/text;base64,'
    const base64Url = prefix + window.btoa(content)

    download(base64Url)
  } else {
    //处理文档流
    const blob = new Blob([content])
    const bUrl = URL.createObjectURL(blob)

    download(base64Url)
    URL.revokeObjectURL(bUrl) // 释放URL 对象
  }
}

/**
 * 通过 URL 下载文件
 * @param url 文件 URL
 * @param filename 文件名
 */
export const downloadByUrl = (url, filename) => {
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}
