<template>
  <a-dropdown
    trigger="click"
    placement="bottomLeft"
    v-model:open="open"
    :disabled="disabled"
    @openChange="onOpenChange">
    <div tabindex="-1" style="display: inline-block">
      <slot v-bind="{ open, clear, displayValue }">
        <a-input
          readonly
          :disabled="disabled"
          placeholder="请选择日期"
          :style="styleObj"
          :value="displayValue">
          <template #prefix>
            <CalendarOutlined style="margin-right: 2px" />
          </template>

          <template #suffix>
            <CloseOutlined
              v-if="!disabled && attrs.value && attrs.value.length"
              style="font-size: 10px; color: rgba(0, 0, 0, 0.45); cursor: pointer"
              @click.stop="clear" />
          </template>
        </a-input>
      </slot>
    </div>

    <template #overlay>
      <PickerPanel ref="panelRef" v-bind="$attrs" @cancel="onCancel" @ok="onOk" />
    </template>
  </a-dropdown>
</template>

<script setup>
import { computed, ref, useAttrs } from 'vue'
import { CalendarOutlined, CloseOutlined } from '@ant-design/icons-vue'
import PickerPanel from './PickerPanel.vue'
import { displayDateFormat } from './utils'

const attrs = useAttrs()

const props = defineProps({
  disabled: { type: Boolean },
})

const emits = defineEmits(['cancel', 'ok', 'clear'])

const open = ref(false)
const panelRef = ref(null)

const onOpenChange = e => {
  if (e) {
    setTimeout(() => panelRef.value?.init())
  } else {
    panelRef.value?.resetOpen()
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

const styleObj = computed(() => attrs.style)

const displayValue = computed(() => {
  const { moda, value = [], extra = {}, offset = [], hms, utcOffset } = attrs

  return displayDateFormat({
    mode: moda,
    date: value,
    offset,
    hms,
    extra,
    utcOffset,
    format: 'YYYY/MM/DD',
  }).join(' ~ ')
})

const clear = e => {
  emits('clear')
}
</script>
