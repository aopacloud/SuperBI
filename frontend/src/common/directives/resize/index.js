/**
 * 使用 ResizeObserver 封装的 v-resize 指令
 * 监听元素的尺寸变化
 */

const map = new WeakMap()

const ob = new ResizeObserver(entries => {
  for (const entry of entries) {
    const mapValue = map.get(entry.target)

    mapValue && mapValue(entry)
  }
})

export default {
  created(el, binding) {
    const handler = binding.value

    if (typeof handler === 'undefined' || handler === null) return

    ob.observe(el)
    map.set(el, handler)
  },
  beforeUnmount(el) {
    ob.unobserve(el)
    map.delete(el)
  },
}
