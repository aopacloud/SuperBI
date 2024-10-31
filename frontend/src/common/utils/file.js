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

/**
 * 将文件转换为 ArrayBuffer
 * @param file
 * @returns {Promise<unknown>}
 */
export function file2Buffer(file) {
  return new Promise((resolve, reject) => {
    // 创建一个文件读取器
    const reader = new FileReader()
    // 使用FileReader的readAsArrayBuffer方法异步读取文件内容
    reader.readAsArrayBuffer(file)
    reader.onload = function (e) {
      const buffer = e.target.result

      resolve(buffer)
    }

    reader.onerror = function (err) {
      reject(err)
    }
  })
}

/**
 * 将文件分割为指定大小的文件块
 * @param file
 * @param chunkSize
 * @returns {Promise<unknown[]>}
 */
export const fileToChunks = (file, chunkSize = 1024) => {
  if (!file) {
    throw new Error('file is required')
  }

  if (!(file instanceof File)) {
    throw new Error('file must be a File instance')
  }

  const { size: fileSize } = file

  const chunks = []
  // 分片数量
  const chunkLength = Math.ceil(fileSize / chunkSize)
  for (let i = 0; i < chunkLength; i++) {
    const start = i * chunkSize
    const end = (i + 1) * chunkSize
    // 文件分片处理
    const chunkFile = file.slice(start, end)

    chunks.push(chunkFile)
  }

  return chunks
}
