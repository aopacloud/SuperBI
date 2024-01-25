import { ref, onBeforeUnmount } from 'vue'

export function useDefer(max = 100) {
  const frameCount = ref(0)

  let rafId

  function updateFrameCount() {
    rafId = requestAnimationFrame(() => {
      frameCount.value++

      if (frameCount.value >= max) return

      updateFrameCount()
    })
  }

  updateFrameCount()

  onBeforeUnmount(() => {
    cancelAnimationFrame(rafId)
    frameCount.value = null
  })

  return function (n) {
    return frameCount.value >= n
  }
}

export default useDefer
