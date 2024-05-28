import { ref } from 'vue'
import { fetchErrorAnalysis, fetchErrorReason } from '@/apis/log/error'

const contentReg = /({.*})/

export default function useError(cb) {
  const doneCb = cb
  const loading = ref(false)

  const readerDecode = async (reader, cb) => {
    let decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()

      if (done) {
        loading.value = false
        doneCb && doneCb()
        decoder = null

        break
      }

      const text = decoder.decode(value)

      const str = text
        .split('\n') // 去除空格
        .map(t => t.match(contentReg)) // 取出EventStream数据内容
        .filter(Boolean)
        .map(t => JSON.parse(t[1]).content)
        .join('')

      cb(str)
    }
  }

  const reason = ref('')
  const reasonLoading = ref(false)
  const fetchReason = async queryId => {
    if (!queryId) return

    try {
      reasonLoading.value = true
      reason.value = ''

      const { summary } = await fetchErrorReason({ queryId })

      reason.value = summary
    } catch (error) {
      console.error('获取错误原因失败', error)
    } finally {
      reasonLoading.value = false
    }
  }

  const content = ref('')
  const fetchError = async queryId => {
    if (!queryId) return

    try {
      loading.value = true
      content.value = ''

      const response = await fetchErrorAnalysis({ queryId })
      const reader = response.body.getReader()

      readerDecode(reader, str => {
        content.value += str
      })
    } catch (error) {
      console.error('获取错误分析失败', error)
    }
  }

  return {
    loading,
    reasonLoading,
    reason,
    fetchReason,
    content,
    fetchError,
  }
}
