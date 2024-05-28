import { onMounted, ref, watch } from 'vue'
import { notification, Spin, message } from 'ant-design-vue'
import { getRandomKey } from 'common/utils/string'
import useError from '@/hooks/useError.js'
import hljs from 'highlight.js/lib/core'
import sql from 'highlight.js/lib/languages/sql'
import 'highlight.js/styles/atom-one-dark.min.css'

// Then register the languages you need
hljs.registerLanguage('sql', sql)

const REASON_PREFIX = '错误原因：'
const REASON_SUFFIX = '修改建议：'

// 关键字
const keywords = [
  { text: REASON_PREFIX, color: '#1677ff' },
  { text: REASON_SUFFIX, color: '#1677ff' },
]
const reasonReg = new RegExp(`${REASON_PREFIX}(.*)?`)

// 请求完超时多久关闭提示
const TIMEOUT = 5000

export default function useErrorSuggestion() {
  // 唯一键
  const notificationKey = getRandomKey()

  // 错误原因
  const reason = ref('')

  // 显示的文本
  const displayHtml = ref('')

  // 复制的文本
  const copyText = ref('')

  const { loading, content, fetchError: fetchFn } = useError()

  watch(content, c => {
    highlightSql(colored(c))
  })

  // 关键字着色
  const colored = str => {
    const a = str.match(reasonReg)
    if (a) {
      reason.value = a[1]
    }

    keywords.forEach(item => {
      str = str.replaceAll(
        item.text,
        match => `<div style="margin-top: 8px;color:${item.color};">${match}</div>`
      )
    })

    return str
  }

  // 高亮sql
  const highlightSql = str => {
    const regex = /```[\s\S]*?```/g

    displayHtml.value = str.replace(regex, match => {
      match = match.substring(6, match.length - 3).replace(/^\n/, '')
      copyText.value = match

      const highlightedCode = hljs.highlight(match, {
        language: 'sql',
      }).value

      return `<div style="position: relative;">
      <a style="position: absolute;top: -25px;right: 2px;z-index: 1;" onClick="__handleCopy()">复制sql</a><pre style="max-height: 520px;margin: 0;overflow: auto;"><code style="display: block;">${highlightedCode}</code></pre>
      </div>`
    })
  }

  // 复制到剪贴板
  const copyToClipboard = () => {
    const textarea = document.createElement('textarea')

    textarea.value = copyText.value
    document.body.appendChild(textarea)
    textarea.select()

    try {
      const copied = document.execCommand('copy')
      if (copied) {
        document.body.removeChild(textarea)
        message.success('复制成功')
      }
    } catch {
      message.error('复制失败，请检查浏览器兼容')
    }
  }

  const show = async queryId => {
    if (loading.value) return

    notification.error({
      key: notificationKey,
      message: null,
      description: () => (
        <div>
          <span innerHTML={displayHtml.value}></span>
          <Spin
            size='small'
            style='line-height: 1;margin: 0 2px;'
            spinning={loading.value}></Spin>
        </div>
      ),
      duration: null,
    })

    await fetchFn(queryId)
  }

  const fetchError = async queryId => {
    reason.value = ''

    await fetchFn(queryId)
  }

  onMounted(() => {
    window.__handleCopy = copyToClipboard.bind(this)
  })

  onBeforeUnmount(() => {
    notification.close(notificationKey)
    window.__handleCopy = null
  })

  return {
    show,
    reason,
    fetchError,
  }
}
