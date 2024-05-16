import { onMounted, onUnmounted } from 'vue'

export default function useVisibilitychange({
  onShow: showHandler = () => {},
  onHide: hideHandler = () => {},
}) {
  const handler = () => {
    if (document.visibilityState === 'visible') {
      onHidden && onHidden()
    } else {
      onShow && onShow()
    }
  }

  onMounted(() => {
    document.addEventListener('visibilitychange', handler)
  })

  onUnmounted(() => {
    document.removeEventListener('visibilitychange', handler)
  })

  return {
    onShow: showHandler,
    onHide: hideHandler,
  }
}
