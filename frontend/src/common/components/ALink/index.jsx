import { defineComponent, onMounted, onUnmounted, ref } from 'vue'

export default defineComponent({
  props: {
    href: {
      type: String,
      default: '',
      description: '链接地址',
    },
  },
  setup(props, { attrs, slots }) {
    const isMousedown = ref(false)
    const isSelected = ref(false)
    const startEl = ref(null)
    const aRef = ref(null)

    const onMousedown = e => {
      // 右键
      if (e.which === 3) return

      isMousedown.value = true
      startEl.value = e.target
    }

    const onMousemove = () => {
      if (!isMousedown.value) return

      isSelected.value = true
    }

    const onMouseup = e => {
      if (!startEl.value) return
      if (!e.currentTarget) return
      if (isSelected.value) return

      triggerClick()
    }

    const href = computed(() => props.href.trim())

    const triggerClick = () => {
      if (!href.value) return

      const a = document.createElement('a')
      a.href = href.value
      a.target = attrs.target
      a.click()
      a.remove()
    }

    const onDocumentMouseup = () => {
      isMousedown.value = false
      isSelected.value = false
      startEl.value = null
    }

    const onContextmenu = e => {
      window.getSelection().selectAllChildren(aRef.value)
      e.preventDefault()
    }

    onMounted(() => {
      document.addEventListener('mousedown', onMousedown)
      document.addEventListener('mouseup', onDocumentMouseup)
    })
    onUnmounted(() => {
      document.removeListener('mousedown', onMousedown)
      document.addEventListener('mouseup', onDocumentMouseup)
    })

    return () => {
      const slotsTemplate = Object.keys(slots).map(slot => slots[slot]())

      return (
        <a
          ref={e => (aRef.value = e)}
          onmousemove={onMousemove}
          onmouseup={onMouseup}
        >
          {slotsTemplate}
        </a>
      )
    }
  },
})
