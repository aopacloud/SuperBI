/**
 * TODO
 */

import { ref, onMounted, onUnmounted } from 'vue'

// 按照惯例，组合式函数名以“use”开头
export default function useTimer(interval = 1000, step = 1000) {
  // 被组合式函数封装和管理的状态
  const value = ref(0)
  let timer = null

  const stop = () => {
    clearInterval(timer)
    clearTimeout(timer)
    timer = null
  }

  const reset = () => {
    stop()

    value.value = 0
  }

  const runTimeout = () => {
    if (timer) {
      stop()
    }

    timer = setTimeout(() => {
      value.value += step
      runTimeout()
    }, interval)
  }

  const runInterval = () => {
    timer = setInterval(() => {
      value.value += step
    }, interval)
  }

  onUnmounted(stop)

  // 通过返回值暴露所管理的状态
  return { value, stop, reset, runTimeout, runInterval }
}
