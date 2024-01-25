export const insertText = (el, str, pos = []) => {
  if (!(el instanceof HTMLElement)) return

  el.focus()

  const txt = el.value
  const { selectionStart: start, selectionEnd: end } = el

  el.value = txt.substring(0, start) + str + txt.substring(end)

  if (pos.length) {
    el.setSelectionRange(start + pos[0], start + (pos[1] ?? pos[0]))
  } else {
    if (start !== end) {
      el.setSelectionRange(start, start + str.length)
    } else {
      el.setSelectionRange(start + str.length, end + str.length)
    }
  }

  return el.value
}
