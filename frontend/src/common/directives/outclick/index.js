const outClickHandler = (e, el, cb) => {
  if (!el.contains(e.target)) {
    cb()
  }
}

export default {
  created(el, binding) {
    const handler = binding.value

    document.addEventListener('mousedown', e => outClickHandler(e, el, handler))
  },
  beforeUnmount(el) {
    document.body.removeEventListener('mousedown', e => outClickHandler(e, el))
  },
}
