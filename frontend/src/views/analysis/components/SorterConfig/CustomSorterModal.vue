<template>
  <a-modal
    title="自定义排序"
    :width="450"
    :open="open"
    @cancel="onCancel"
    @ok="onOk">
    <p>编辑字段值及其顺序，未设定排序的值会自动排在列表后面</p>

    <a-textarea
      placeholder="回车换行分隔"
      :rows="6"
      v-model:value="value"></a-textarea>
  </a-modal>
</template>

<script setup>
import { watch } from 'vue'

const emits = defineEmits(['update:open', 'ok'])
const props = defineProps({
  modelValue: { type: String, default: '' },
  open: { type: Boolean, default: false },
})

const value = ref()

watch(
  () => props.open,
  e => {
    if (e) value.value = props.modelValue
  }
)

const onCancel = () => {
  emits('update:open', false)
}
const onOk = () => {
  emits('update:open', false)
  const v = value.value
    .split(/\n/)
    .map(t => t.trim())
    .filter(Boolean)
    .join('\n')
  emits('ok', v)
}
</script>

<style lang="scss" scoped></style>
