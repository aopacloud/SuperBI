<template>
  <a-dropdown
    trigger="click"
    placement="bottomLeft"
    v-model:open="open"
    @openChange="onOpenChange">
    <slot v-bind="{ open }">
      <a>{{ open }}</a>
    </slot>

    <template #overlay>
      <PickerPanel ref="panelRef" v-bind="$attrs" @cancel="onCancel" @ok="onOk" />
    </template>
  </a-dropdown>
</template>

<script setup>
import { ref } from 'vue'
import PickerPanel from './PickerPanel.vue'

const emits = defineEmits(['cancel', 'ok'])

const open = ref(false)
const panelRef = ref(null)

const onOpenChange = e => {
  if (e) {
    setTimeout(() => panelRef.value?.init())
  }
}
const onCancel = () => {
  open.value = false

  emits('cancel')
}
const onOk = e => {
  open.value = false

  emits('ok', e)
}
</script>
