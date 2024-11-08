<script lang="jsx">
import { ref, toRaw, computed, onBeforeMount } from 'vue'

const directionMap = {
  top: 'y',
  right: 'x',
  bottom: 'y',
  left: 'x',
  'top-right': ['x, y'],
  'bottom-right': ['x, y'],
  'bottom-left': ['x, y'],
  'top-left': ['x, y']
}

export default {
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    directions: {
      type: Array,
      default: () => Object.keys(directionMap)
    },
    minWidth: {
      type: Number,
      default: 0
    },
    maxWidth: {
      type: Number
    },
    minHeight: {
      type: Number,
      default: 0
    },
    maxHeight: {
      type: Number
    },
    // 自动改变尺寸
    autoChange: {
      type: Boolean,
      default: true
    },
    dblclick: Boolean
  },
  emits: ['resizing', 'resized', 'dblclick'],
  // inheritAttrs: false,
  setup(props, ctx) {
    const {
      disabled,
      directions = [],
      minWidth,
      maxWidth,
      minHeight,
      maxHeight,
      dblclick
    } = props

    const elRef = ref(null) // 元素

    const contentRef = ref(null)

    const startEl = ref(null) // 开始元素

    const startX = ref(0) // 开始位置X

    const startY = ref(0) // 开始位置Y

    const startWidth = ref(0) // 开始宽度

    const startHeight = ref(0) // 开始高度

    const startTop = ref(0) // 开始顶部

    const startLeft = ref(0) // 开始左边

    const onlyPos = ref() // 拖拽方向

    const isMousedown = ref() // 鼠标按下

    const isDragging = ref(false) // 拖拽中

    const isHover = ref(false) // hover状态

    const isHoverCls = computed(() => {
      if (isMousedown.value) return true

      if (typeof isMousedown.value === 'undefined') return isHover.value

      return isMousedown.value || isHover.value
    })

    const willStyle = ref({})

    const onMouseOver = () => {
      // 使用一个全局变量来处理共享的状态
      if (window._drag_resize_isDragging_ || disabled) return
      isHover.value = true
    }
    const onMouseLeave = () => {
      isHover.value = false
    }

    const onDblclick = e => {
      const target = e.target
      if (
        !target.classList.contains('resize-handle') ||
        !elRef.value.contains(target)
      )
        return

      const direction = target.dataset.direction
      target.style[
        direction === 'left' || direction === 'right' ? 'left' : 'top'
      ] = 'unset'
      target.style[direction] = 'unset'

      ctx.emit('dblclick', direction)
    }

    const onMousedown = e => {
      const target = e.target
      if (!target.classList.contains('resize-handle')) return
      if (!elRef.value?.contains(target)) return

      if (disabled) return

      e.preventDefault()
      e.stopPropagation()

      startEl.value = target

      isMousedown.value = true
      window._drag_resize_isDragging_ = true

      startX.value = e.clientX
      startY.value = e.clientY

      startWidth.value = elRef.value.offsetWidth
      startHeight.value = elRef.value.offsetHeight
      startTop.value = parseInt(elRef.value.style.top) || 0
      startLeft.value = parseInt(elRef.value.style.left) || 0

      willStyle.value = {
        width: startWidth.value,
        height: startHeight.value,
        top: startTop.value,
        left: startLeft.value
      }

      const pos = target.dataset.pos?.split(',')
      if (pos.length === 1) {
        onlyPos.value = pos[0]
      } else {
        onlyPos.value = undefined
      }

      document.addEventListener('mousemove', onMousemove)
    }

    //
    const updateWidth = disX => {
      const w = startWidth.value + disX
      const willWidth = Math.max(Math.min(w, maxWidth || w), minWidth)

      willStyle.value.width = willWidth
      if (props.autoChange) {
        elRef.value.style.width = willWidth + 'px'
      } else {
        startEl.value.style.left =
          willWidth - startEl.value.offsetWidth / 2 + 'px'

        contentRef.value.style.position = 'absolute'
        contentRef.value.style.width = willWidth - 2 + 'px'
      }

      return willWidth
    }

    // 更新高度
    const updateHeight = disY => {
      const h = startHeight.value + disY
      const willHeight = Math.max(Math.min(h, maxHeight || h), minHeight)

      willStyle.value.height = willHeight
      if (props.autoChange) {
        elRef.value.style.height = willHeight + 'px'
      } else {
        startEl.value.style.top = willHeight - 2 + 'px'
      }

      return willHeight
    }

    // 更新top
    const updateTop = (wH, disY) => {
      willStyle.value.top = startTop.value + disY
      if (props.autoChange) {
        elRef.value.style.top = startTop.value + disY + 'px'
      } else {
        startEl.value.style.top = wH - 2 + 'px'
      }
    }

    // 更新left
    const updateLeft = (wW, disX) => {
      willStyle.value.left = startLeft.value + disX
      if (props.autoChange) {
        elRef.value.style.left = startLeft.value + disX + 'px'
      } else {
        startEl.value.style.left = wW - startEl.value.offsetWidth / 2 + 'px'
      }
    }

    const onMousemove = e => {
      if (!isMousedown.value) return

      isDragging.value = true

      const disX = e.clientX - startX.value
      const disY = e.clientY - startY.value

      const direction = startEl.value.dataset.direction
      switch (direction) {
        case 'top':
          updateTop(updateHeight(-disY), disY)
          break
        case 'right':
          updateWidth(disX)
          break
        case 'bottom':
          updateHeight(disY)
          break
        case 'left':
          updateLeft(updateWidth(-disX), disX)
          break
        case 'top-right':
          updateTop(updateHeight(-disY), disY)
          updateWidth(disX)
          break
        case 'bottom-right':
          updateHeight(disY)
          updateWidth(disX)
          break
        case 'bottom-left':
          updateHeight(disY)
          updateLeft(updateWidth(-disX), disX)
          break
        case 'top-left':
          updateTop(updateHeight(-disY), disY)
          updateLeft(updateWidth(-disX), disX)
          break
        default:
          break
      }

      ctx.emit('resizing', {
        ...toRaw(willStyle.value),
        disX,
        disY
      })

      e.stopPropagation()
      e.preventDefault()
      return false
    }

    const onMouseup = () => {
      if (!isMousedown.value) return

      isMousedown.value = false
      window._drag_resize_isDragging_ = false
      startEl.value = null

      if (!props.autoChange) {
        contentRef.value.style.position = 'unset'
        contentRef.value.style.width = 'unset'
      }

      ctx.emit('resized', toRaw(willStyle.value))

      startX.value = 0
      startY.value = 0

      document.removeEventListener('mousemove', onMousemove)
    }

    const createCorners = () => {
      const validDirections = directions.filter(t => directionMap[t])

      const createCorner = direction => (
        <span
          data-direction={direction}
          data-pos={directionMap[direction]}
          class={['resize-handle', 'resize-handle-' + direction]}
        ></span>
      )

      window.addEventListener('mousedown', onMousedown)
      window.addEventListener('mouseup', onMouseup)
      if (dblclick) {
        window.addEventListener('dblclick', onDblclick)
      }

      return (
        <div class='resize-handles'>{validDirections.map(createCorner)}</div>
      )
    }

    onBeforeMount(() => {
      window.removeEventListener('mousedown', onMousedown)
      if (dblclick) {
        window.addEventListener('dblclick', onDblclick)
      }
    })

    return () => {
      const defaultSlot = ctx.slots.default?.() ?? <div>default</div>

      return (
        <div
          class={[
            'resize-container',
            {
              'resize--disabled': props.disabled,
              'resize--hovered': isHoverCls.value
            }
          ]}
          ref={e => (elRef.value = e)}
          onmouseover={onMouseOver}
          onmouseleave={onMouseLeave}
        >
          {createCorners()}
          <div class='resize-content' ref={e => (contentRef.value = e)}>
            {defaultSlot}
          </div>
        </div>
      )
    }
  }
}
</script>

<style lang="scss">
.resize-container {
  --triggerSize: 12px;
  --activeSize: calc(var(--triggerSize) * 0.5);

  position: relative;
  display: inline-block;
  border: 1px dashed transparent;

  &.resize--disabled {
    .resize-handle {
      cursor: unset;
    }
  }
  &.resize--hovered {
    border-color: #6363ff;
    .resize-handle::before {
      opacity: 1;
    }
  }
}

.resize-container {
  .resize-content {
    height: 100%;
    z-index: 9999;
  }
  .resize-handle {
    position: absolute;
    z-index: 10;
    position: absolute;
    width: var(--triggerSize);
    height: var(--triggerSize);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9;
    &::before {
      content: '';
      opacity: 0;
      background-color: #6363ff;
      width: 100%;
      height: 100%;
      border-radius: var(--activeSize);
      overflow: hidden;
    }
    &-top,
    &-bottom {
      width: calc(var(--triggerSize) * 2);
      height: var(--triggerSize);
      transform: translateX(-50%);
      &::before {
        width: 100%;
        height: calc(var(--activeSize) * 0.75);
      }
    }
    &-top {
      top: calc(var(--triggerSize) * -0.5);
      left: 50%;
      cursor: ns-resize;
    }
    &-bottom {
      left: 50%;
      bottom: calc(var(--triggerSize) * -0.5);
      cursor: ns-resize;
    }

    &-right,
    &-left {
      width: var(--triggerSize);
      height: calc(var(--triggerSize) * 2);
      transform: translateY(-50%);
      &::before {
        width: calc(var(--activeSize) * 0.75);
        height: 100%;
      }
    }
    &-right {
      top: 50%;
      right: calc(var(--triggerSize) * -0.5);
      cursor: ew-resize;
    }
    &-left {
      top: 50%;
      left: calc(var(--triggerSize) * -0.5);
      cursor: ew-resize;
    }

    &-top-right,
    &-bottom-right,
    &-bottom-left,
    &-top-left {
      width: calc(var(--triggerSize) * 0.75);
      height: calc(var(--triggerSize) * 0.75);
    }

    &-top-right {
      top: calc(var(--triggerSize) * -0.25);
      right: calc(var(--triggerSize) * -0.25);
      cursor: nesw-resize;
    }
    &-bottom-right {
      bottom: calc(var(--triggerSize) * -0.25);
      right: calc(var(--triggerSize) * -0.25);
      cursor: nwse-resize;
    }
    &-bottom-left {
      bottom: calc(var(--triggerSize) * -0.25);
      left: calc(var(--triggerSize) * -0.25);
      cursor: nesw-resize;
    }
    &-top-left {
      top: calc(var(--triggerSize) * -0.25);
      left: calc(var(--triggerSize) * -0.25);
      cursor: nwse-resize;
    }

    &:hover {
      z-index: 99;
    }
  }
}
</style>
