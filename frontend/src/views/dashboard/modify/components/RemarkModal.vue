<template>
  <a-modal
    className="dashboard-label--modal"
    destroyOnClose
    :open="open"
    :title="title"
    @cancel="onCancel"
    @ok="onOk">
    <input
      ref="titleRef"
      class="label-title"
      v-model="modelValue.title"
      placeholder="请输入标题" />

    <RichInput
      ref="richInputRef"
      placeholder="请输入内容"
      v-model:value="modelValue.content"
      @tag-click="onTagClick">
      <template #footer>
        <a-popover
          placement="topLeft"
          trigger="click"
          :title="insertTitle"
          :open="insertOpen">
          <a style="font-size: 12px" @click="addLinkPop">插入链接</a>
          <template #content>
            <InsertLinkModal
              :init-data="insertInfo"
              @cancel="onPopCancel"
              @ok="onPopConfirm" />
          </template>
        </a-popover>
      </template>
    </RichInput>
  </a-modal>
</template>

<script setup>
import { computed, nextTick, reactive, ref, toRaw, watch } from 'vue'
import { message } from 'ant-design-vue'
import RichInput from 'common/components/RichInput'
import InsertLinkModal from './InsertLinkModal.vue'

const emits = defineEmits(['update:open', 'ok'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  id: {
    type: [String, Number],
  },
  initialValue: {
    type: Object,
    default: () => ({}),
  },
  placeholder: {
    tppe: String,
    default: '请输入',
  },
})

const title = ref('新增便签')
const modelValue = ref({})

const richInputRef = ref(null) // 富文本输入框
const insertOpen = ref(false)
const insertTitle = ref('插入链接')
const insertInfo = ref({})

const isLinkEdit = computed(() => {
  return Object.keys(toRaw(insertInfo.value)).length > 0
})

watch(
  () => props.open,
  open => {
    if (open) {
      init()
    } else {
      insertOpen.value = false
    }
  }
)

const init = () => {
  const initialValue = props.initialValue

  title.value = Object.keys(initialValue).length > 0 ? '编辑便签' : '新增便签'
  modelValue.value = { ...initialValue }
}

const onCancel = () => {
  emits('update:open', false)
  modelValue.value = {}
}

const titleRef = ref(null)
const onOk = () => {
  if (!modelValue.value.title || !modelValue.value.title.trim().length) {
    titleRef.value.focus()

    return message.warning('便签标题不能为空')
  }

  emits('ok', { ...modelValue.value })
  onCancel()
}
const addLinkPop = () => {
  insertTitle.value = '插入链接'
  insertInfo.value = {}
  insertOpen.value = true
}
const onPopCancel = () => {
  insertOpen.value = false
}
const onPopConfirm = values => {
  const { label, link } = values

  if (!isLinkEdit.value) {
    nextTick(() => {
      insert(label, link)
    })
  } else {
    const { label: _label, link: _link, target = {} } = insertInfo.value
    const reg = new RegExp(`<input[^>]*data-id=['"](${target.dataset.id})['"][^<]*>`, 'g')
    const tag = modelValue.value.content.match(reg)?.[0] || ''
    const str = tag
      .replace(`value="${_label}"`, `value="${label}"`)
      .replace(`data-label="${_label}"`, `data-label="${label}"`)
      .replace(`data-href="${_link}"`, `data-href="${link}"`)

    modelValue.value.content = modelValue.value.content.replace(tag, str)
    target.setAttribute('value', label)
    target.setAttribute('data-label', label)
    target.setAttribute('data-href', link)
  }

  insertOpen.value = false
}
const insert = (label, link) => {
  richInputRef.value.insert({
    tag: 'input',
    label: label || link,
    attrs: {
      'data-label': label,
      'data-href': link,
    },
  })
}
const onTagClick = target => {
  const {
    dataset: { label, href: link },
  } = target

  insertTitle.value = '编辑链接'
  insertInfo.value = { target, label, link }
  insertOpen.value = true
}
</script>

<style scoped lang="scss">
.label-title {
  border: none;
  outline: none;
  width: 100%;
  min-height: 40px !important;
  font-size: 16px;
}
</style>
