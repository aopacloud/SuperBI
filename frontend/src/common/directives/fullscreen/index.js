import { cssStrToObj, cssObjToStr } from 'common/utils/css'

const fixedCSS = 'position: fixed; top: 0; right: 0; bottom: 0; left: 0;z-index: 9'

const updateCSS = (el, cssStr) => {
  const obj = cssStrToObj(cssStr)
  const str = cssObjToStr(obj)
  el.style = str
}

export default {
  mounted(el) {
    el._style = el.getAttribute('style') || ''
  },
  updated(el, binding) {
    const value = binding.value

    if (typeof value === 'undefined' || value) {
      updateCSS(el, el._style + ';' + fixedCSS)
    } else {
      updateCSS(el, el._style)
    }
  },
  beforeUnmount(el) {},
}
