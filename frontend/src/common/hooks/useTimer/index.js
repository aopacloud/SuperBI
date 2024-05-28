import { onUnmounted, ref } from 'vue'

export default function useTimer(
  callback,
  { immediate = false, interval = 1000 } = {}
) {
  let timer = null // 定时器id
  const times = ref(0) // 执行次数
  const handle = ref(callback) // 执行函数

  /**
   * 执行函数
   * @returns 无返回值
   */
  let execute = () => {
    if (immediate) {
      execute = () => {
        Promise.resolve(handle.value()).then(() => {
          times.value++
          timer = setTimeout(execute, interval)
        })
      }
    } else {
      execute = () => {
        timer = setTimeout(() => {
          times.value++
          Promise.resolve(handle.value()).then(execute)
        }, interval)
      }
    }

    execute()
  }

  /**
   * 运行函数
   * @param e 事件对象
   * @returns 无返回值
   */
  const run = e => {
    handle.value = e

    stop()
    start()
  }

  /**
   * 开始计时器
   * @throws 当 handle.value 不是函数时，抛出 TypeError 异常
   */
  const start = () => {
    if (typeof handle.value !== 'function') {
      throw TypeError('useTimer callback must be a function')
    }

    execute()
  }

  /**
   * 停止定时器
   * @returns 无返回值
   */
  const stop = () => {
    clearTimeout(timer)
    timer = null
  }

  /**
   * 重置函数
   * @returns 无返回值
   */
  const reset = () => {
    times.value = 0
    stop()
    start()
  }

  onUnmounted(stop)

  return {
    times,
    start,
    stop,
    reset,
    run,
  }
}
