import { onMounted, onUnmounted, ref, unref } from 'vue'

const startX = ref(0) // 开始位置X
const startY = ref(0) // 开始位置Y
const x = ref(0) // 当前位置X
const y = ref(0) // 当前位置Y
const disX = ref(0) // 移动距离X
const disY = ref(0) // 移动距离Y
export default function usePointer({ el, onEnd } = {}) {
  if (!el) {
    el = document.body
  } else {
    el = typeof el === 'string' ? document.querySelector(el) : el
  }

  const onMousedown = e => {
    e.preventDefault()
    startX.value = x.value = e.clientX
    startY.value = y.value = e.clientY

    window.addEventListener('mousemove', onMousemove)
  }

  const onMousemove = e => {
    x.value = e.clientX
    y.value = e.clientY

    disX.value = e.clientX - startX.value
    disY.value = e.clientY - startY.value

    e.preventDefault()
  }

  const onMouseup = () => {
    window.removeEventListener('mousemove', onMousemove)

    startX.value = 0
    startY.value = 0

    onEnd &&
      onEnd({ x: unref(x), y: unref(y), disX: unref(disX), disY: unref(disY) })

    disX.value = 0
    disY.value = 0
  }

  const addEvent = () => {
    el.addEventListener('mousedown', onMousedown)
    el.addEventListener('mouseup', onMouseup)
  }
  const removeEvent = () => {
    el.removeEventListener('mousedown', onMousedown)
    el.removeEventListener('mouseup', onMouseup)
  }

  onMounted(() => {
    addEvent()
  })

  onUnmounted(() => {
    removeEvent()
  })

  return {
    startX,
    startY,
    disX,
    disY,
    x,
    y
  }
}
