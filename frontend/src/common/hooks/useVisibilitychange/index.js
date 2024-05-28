import { onMounted, onUnmounted } from 'vue'

export default function useVisibilitychange({ onShow, onHide, delay = 0 }) {
  // 页面可见性状态
  const state = ref(document.visibilityState)
  const timer = ref(null)

  // 事件处理
  const handler = () => {
    if (document.visibilityState === 'visible') {
      if (!onShow) return

      timer.value = setTimeout(() => {
        onShow(state.value)
      }, delay)
    } else {
      if (!onHide) return

      timer.value = setTimeout(() => {
        onHide(state.value)
      }, delay)
    }

    state.value = document.visibilityState
  }

  onMounted(() => {
    document.addEventListener('visibilitychange', handler)
  })

  onUnmounted(() => {
    document.removeEventListener('visibilitychange', handler)
    timer.value = null
  })

  return { state }
}
