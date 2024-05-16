import { onMounted, onBeforeUnmount, ref } from 'vue'

export default function useTimer(
  callback,
  { immediate = false, frequency = 0 } = {}
) {
  let timer = ref(null)

  let run = () => {
    if (immediate) {
      run = () => {
        Promise.resolve(callback).then(() => {
          timer = setTimeout(run, frequency)
        })
      }
    } else {
      run = () => {
        timer = setTimeout(() => {
          Promise.resolve(callback).then(run)
        }, frequency)
      }
    }

    run()
  }

  const start = () => {
    if (typeof callback !== 'function') {
      throw TypeError('useTimer callback must be a function')
    }

    run()
  }

  const stop = () => {
    clearTimeout(timer)
    timer = null
  }

  const handler = () => {
    if (document.visibilityState === 'hidden') {
      // 页面不可见时
      stop()
    } else {
      start()
    }
  }

  onMounted(() => {
    window.addEventListener('visibilitychange', handler)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('visibilitychange', handler)
  })

  return {
    start,
    stop,
  }
}
