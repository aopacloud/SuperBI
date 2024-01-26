<template>
  <a-modal
    :title="title"
    :open="open"
    :confirmLoading="loading"
    @cancel="cancel"
    @ok="ok">
    <SelectList
      ref="selectListRef"
      v-bind="{ ...$attrs, value: modelValue }"
      @update:value="e => (modelValue = e)" />
  </a-modal>
</template>

<script setup>
import { ref, watch } from 'vue'
import SelectList from './SelectList.vue'

const emits = defineEmits(['update:open', 'ok', 'cancel'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '标题',
  },
  value: {
    type: [String, Number, Array],
  },
  confirmFn: {
    type: Function,
  },
})

const modelValue = ref()
const selectListRef = ref(null)

watch(
  () => props.open,
  op => {
    if (op) {
      const { value } = props

      modelValue.value =
        typeof value === 'undefined' ? [] : Array.isArray(value) ? [...value] : [value]

      selectListRef.value?.clearSearch()
    }
  }
)

const cancel = () => {
  emits('update:open', false)
  emits('cancel')
}

const loading = ref(false)
const ok = async () => {
  try {
    loading.value = true

    if (typeof props.confirmFn === 'function') {
      await props.confirmFn(modelValue.value)
    }

    emits('ok', modelValue.value)
    cancel()
  } catch (error) {
    console.error('添加错误', error)
  } finally {
    loading.value = false
  }
}
</script>
