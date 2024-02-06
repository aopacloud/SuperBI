<template>
  <div class="inputarea">
    <div
      ref="inputRef"
      :contenteditable="contenteditable"
      class="inputarea--content"
      :placeholder="placeholder"
      :style="contentStyle"
      @click.stop.prevent="onClick"
      @input="onInput"
      @focus="onFocus"
      @blur="onBlur"
      v-html="modelValue"></div>
    <div v-if="$slots.footer" class="inputarea--footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
/**
 * 生成随机ID
 */
function getGuid() {
  return `r${new Date().getTime()}d${Math.ceil(Math.random() * 1000)}`
}

const emits = defineEmits(['update:value', 'tag-click', 'input', 'focus', 'blur'])

const props = defineProps({
  value: {
    type: String,
  },
  placeholder: {
    type: String,
    default: '请输入',
  },
  max: {
    type: Number,
    default: 0,
  },
  contentStyle: {
    type: Object,
    default: () => ({}),
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  readonly: {
    type: Boolean,
    default: false,
  },
  disableEnter: {
    type: Boolean,
    default: false,
  },
})

const modelValue = ref('')
const selection = ref(null)
const savedRange = ref(null)
const inputRef = ref(null)
const isInputFocus = ref(false)

const contenteditable = computed(() => {
  return !(props.disabled || props.readonly)
})

watch(
  () => props.value,
  value => {
    if (contenteditable.value) return
    modelValue.value = value?.replace(/\n/g, '<br />')
    // }
  },
  { immediate: true, deep: true }
)

let timerId = null
// 初始化
const init = () => {
  timerId = setTimeout(() => {
    modelValue.value = props.value?.replace(/\n/g, '<br />')
  }, 10)
}

onMounted(() => {
  document.addEventListener('selectionchange', onSelectHandler)
  inputRef.value.addEventListener('paste', onPasteHandler)

  init()
})

onBeforeUnmount(() => {
  document.removeEventListener('selectionchange', onSelectHandler)
  inputRef.value.removeEventListener('paste', onPasteHandler)

  timerId = null
})

// 监听选定文本的变动
const onSelectHandler = () => {
  if (!isInputFocus.value) return

  selection.value = window.getSelection()
  savedRange.value = createRange(selection.value)
}

// 处理富文本粘贴问题，删除html标记，将换行符替换为br
const onPasteHandler = e => {
  if (!contenteditable.value) return

  savedRange.value = createRange(window.getSelection())

  // 删掉选中的内容（如有）
  savedRange.value.deleteContents()

  // 剪切板纯文本内容
  const pasteText = (e.clipboardData || window.clipboardData).getData('text')

  // 如果禁用换行，删除换行符，只插入纯文本内容，反之则将文本内容处理为html后插入
  if (props.disableEnter !== undefined) {
    const node = document.createTextNode(pasteText.replace(/\n+/g, ''))
    // 插入节点
    savedRange.value.insertNode(node)
    // 移动光标
    moveCursorAfterNode(node)
  } else {
    // 文本及换行符集合
    const strs = pasteText.match(/([^\n]+|\n+?)/g).filter(str => str)

    strs.forEach(str => {
      const node = /\n+/.test(str)
        ? document.createElement('br')
        : document.createTextNode(str)

      // 插入节点
      savedRange.value.insertNode(node)
      // 移动光标
      moveCursorAfterNode(node)
    })
  }

  updateData()
  e.preventDefault()
}
const createRange = selection => {
  selection.value = selection

  return selection.rangeCount > 0 ? selection.getRangeAt(0) : null
}

// 检查光标选区是否存在，如不存在，或者不在input上，则在input容器中创建选区。
const makesureRange = () => {
  const range = savedRange.value

  if (!range) {
    focus()
    selection.value = window.getSelection()
    savedRange.value = createRange(selection.value)
  }

  moveRangeToEnd()
}

// 移动光标到末尾
const moveRangeToEnd = () => {
  // 选中全部子节点
  // selection.value.selectAllChildren(inputRef.value)
  // 移动光标到尾部
  selection.value.collapseToEnd()
  savedRange.value.collapse(true)
}

/**
 * 根据参考节点移动光标位置
 * @param node - 参考节点
 */
const moveCursorAfterNode = node => {
  savedRange.value.setStartAfter(node)
  selection.value.collapseToEnd()
  savedRange.value.collapse(true)

  // 存在最后光标对象，选定对象清除所有光标并添加最后光标还原之前的状态
  selection.value.removeAllRanges()
  selection.value.addRange(savedRange.value)
}
const focus = () => {
  inputRef.value.focus()
}
const onFocus = e => {
  isInputFocus.value = true
}
const onBlur = e => {
  isInputFocus.value = false
}
const onClick = e => {
  const { target } = e

  if (typeof target.dataset.type === 'undefined') return

  emits('tag-click', target)
}

/**
 * 创建标签节点
 * @param tag 		- 标签名称
 * @param label 	- 显示文字
 * @param attrs 	- 属性
 * @param payload - 额外参数
 * @param targetValue - 目标取值
 *                      如果传递了
 *                      那返回的text文本将使用targetValue输出
 *                      否则使用默认的插入文本
 */
const createTagNode = ({
  tag = 'input',
  label,
  attrs = {},
  payload,
  targetValue,
} = {}) => {
  // 创建模版标签
  const node = document.createElement(tag.toLowerCase() || 'input')
  const id = Date.now()
  node.setAttribute('data-type', 'tag')
  node.setAttribute('unselectable', 'on')
  node.setAttribute('readonly', 'true')
  node.setAttribute('class', `inputarea-tag--${tag}`)
  node.setAttribute('data-id', id)

  if (payload) {
    node.dataset.payload = typeof payload === 'object' ? JSON.stringify(payload) : payload
  }

  // 绑定属性
  if (attrs) {
    Object.keys(attrs).forEach(k => {
      node.setAttribute(k, attrs[k])
    })
  }

  if (targetValue) {
    node.dataset.targetValue = targetValue
  }

  if (node.tagName === 'INPUT') {
    node.type = 'button'
    node.value = label
  }
  node.innerText = label

  return node
}

// 更新数据
const updateData = () => {
  const divEle = document.createElement('div')
  const inputInnerHTML = inputRef.value.innerHTML.replace(/\n/g, '<br />')
  divEle.innerHTML = inputInnerHTML

  const lastChild = divEle.lastChild
  if (lastChild && lastChild.tagName === 'BR') {
    tempContent.removeChild(lastChild)
  }

  emits('update:value', divEle.innerHTML)
}

const onInput = async e => {
  emits('update:value', inputRef.value.innerHTML)
}

/**
 * 插入节点
 * @param  {string|object} payload   参数
 */
const insert = payload => {
  if (typeof payload === 'string') {
    insertText(payload)
  } else if (payload instanceof HTMLElement) {
    if (payload instanceof HTMLImageElement) {
      const width = payload.getAttribute('width') || 30
      const height = payload.getAttribute('width') || 30

      payload.setAttribute('width', width)
      payload.setAttribute('height', height)
    }

    insertNode(payload)
  } else {
    insertNode(createTagNode(payload))
  }
}

/**
 * 插入文本
 * @param {string} text 文本
 */
const insertText = text => {
  const node = document.createTextNode(text)

  insertNode(node)
}

defineExpose({
  insert,
})

/**
 * 插入节点
 * @param {HTMLEelement} node node节点
 */
const insertNode = node => {
  makesureRange()

  // 删掉选中的内容（如有)
  savedRange.value.deleteContents()

  // 插入节点
  savedRange.value.insertNode(node)
  moveCursorAfterNode(node)
  updateData()
}
</script>

<style scoped lang="scss">
$padding: 8px;

.inputarea {
  position: relative;
  display: flex;
  flex-direction: column;
  line-height: initial;
  background: #f9f9f9;
  &--content {
    width: 100%;
    min-height: 100px;
    padding: $padding;
    word-break: break-all;
    border: none;
    outline: none;
    resize: none;
    &:empty:before {
      content: attr(placeholder);
      color: #919191;
    }

    :deep(input) {
      color: #1890ff;
      width: auto;
      max-width: 100%;
      white-space: normal;
      cursor: pointer;
      border: none;
      touch-action: manipulation;
      -webkit-appearance: none;
      outline: none;
      border-radius: 3px;
      margin: 0 2px;
      padding: 0;
      background: none;
      text-align: left;
      &:hover {
        color: lighten(#1890ff, 15%);
      }
    }
  }
  &--footer {
    padding: $padding;
    bottom: $padding;
    left: $padding;
    right: $padding;
    line-height: 1;
  }
}
</style>
