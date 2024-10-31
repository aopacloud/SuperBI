<template>
  <a-dropdown
    trigger="click"
    placement="bottomLeft"
    v-model:open="open"
    :disabled="disabled"
    @openChange="onOpenChange"
  >
    <div tabindex="-1" style="display: inline-block">
      <slot v-bind="{ open, clear, displayValue }">
        <a-input
          readonly
          placeholder="请选择日期"
          :size="size"
          :disabled="disabled"
          :style="styleObj"
          :value="displayValue"
        >
          <template #prefix>
            <CalendarOutlined style="margin-right: 2px" />
          </template>

          <template #suffix>
            <CloseOutlined
              v-if="
                !disabled && (modelValue.value?.length || modelValue.length)
              "
              style="
                font-size: 10px;
                color: rgba(0, 0, 0, 0.45);
                cursor: pointer;
              "
              @click.stop="clear"
            />
          </template>
        </a-input>
      </slot>
    </div>

    <template #overlay>
      <PickerPanel
        ref="panelRef"
        v-bind="
          Object.assign(
            { ...$props, ...$attrs },
            pickerOnly ? { value: modelValue } : modelValue
          )
        "
        @cancel="onCancel"
        @ok="onOk"
      />
    </template>
  </a-dropdown>
</template>

<script setup>
import { computed, ref, useAttrs } from 'vue'
import { CalendarOutlined, CloseOutlined } from '@ant-design/icons-vue'
import PickerPanel from './PickerPanel.vue'
import { displayDateFormat } from './utils'
import { watchEffect } from 'vue'
import { deepClone } from '@/common/utils/help'
import { nextTick } from 'vue'

const attrs = useAttrs()

const props = defineProps({
  disabled: Boolean,
  pickerOnly: Boolean,
  showTime: Boolean,
  size: String
})

const emits = defineEmits(['update:value', 'cancel', 'ok', 'clear'])

const open = ref(false)
const panelRef = ref(null)

const onOpenChange = e => {
  if (e) {
    nextTick(() => {
      panelRef.value?.init()
    })
  } else {
    panelRef.value?.resetOpen()
    emits('cancel')
  }
}
const onCancel = () => {
  open.value = false

  emits('cancel')
}
const onOk = e => {
  open.value = false

  modelValue.value = deepClone(e)
  emits('update:value', props.pickerOnly ? deepClone(e) : deepClone(e.value))
  emits('ok', e)
}

const modelValue = ref()

watchEffect(() => {
  if (props.pickerOnly) {
    modelValue.value = attrs.value ? [...attrs.value] : []
  } else {
    modelValue.value = { ...attrs }
  }
})

const styleObj = computed(() => {
  return {
    width: props.showTime ? '365px' : '240px',
    ...attrs.style
  }
})

const displayValue = computed(() => {
  const {
    moda,
    value = [],
    extra = {},
    offset = [],
    hms,
    utcOffset
  } = modelValue.value

  if (props.pickerOnly) {
    return modelValue.value.join(' ~ ')
  }

  return displayDateFormat({
    mode: moda,
    date: value,
    offset,
    hms,
    extra,
    utcOffset,
    format: 'YYYY/MM/DD'
  }).join(' ~ ')
})

const clear = () => {
  if (props.pickerOnly) {
    modelValue.value = []
    emits('update:value', [])
  } else {
    formState.partitionRanges[0].conditions = [{}]
    modelValue.value[0].conditions = [{}]
    emits('update:value', modelValue.value)
  }
  emits('clear')
}
</script>
