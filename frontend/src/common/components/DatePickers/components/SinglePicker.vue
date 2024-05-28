<template>
  <div class="custom-picker" :class="{ showTime }">
    <div class="panel left" ref="startPanel">
      <div class="panel-title" style="margin-top: -35px">
        <a-date-picker
          inputReadOnly
          placement="bottomRight"
          class="date-picker-trigger"
          popup-class-name="custom-date-picker-dropdown small"
          allowClear
          :open="openValue"
          :valueFormat="format"
          :show-time="showTime"
          :getPopupContainer="e => onGetPopupContainer(e, startPanel)"
          v-model:value="modelValue"
          @change="onChange"
          @ok="onOk">
        </a-date-picker>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import { getUtcDate } from '../utils'
import dayjs from 'dayjs'

const SPLIT = ' '

const emits = defineEmits(['update:value', 'update:hms', 'ok', 'cancel'])
const props = defineProps({
  showTime: { type: Boolean },

  valueFormat: { type: String, default: 'YYYY-MM-DD' },

  value: { type: Array, default: () => [] },

  hms: { type: Array, default: () => [] },

  extra: { type: Object, default: () => ({}) },

  utcOffset: {
    type: Number,
    default: 8,
    validator: str => {
      return typeof str === 'number'
    },
  },
})

const format = computed(() => {
  const f = 'HH:mm:ss'

  return props.showTime ? props.valueFormat + SPLIT + f : props.valueFormat
})

const modelValue = ref(null)

const openValue = ref(false)

const open = () => {
  openValue.value = true
}
const close = () => {
  openValue.value = false
}

defineExpose({ open, close })

// 初始化
watchEffect(() => {
  if (!props.value.length) {
    if (props.extra.dt) {
      modelValue.value = dayjs().format(props.valueFormat) + SPLIT + '00:00:00'
    } else {
      modelValue.value = null
    }
    return
  }

  const [d] = props.value,
    [t] = props.hms

  if (!props.showTime) {
    modelValue.value = d
  } else {
    const hms = t || '00:00:00'

    modelValue.value = d + SPLIT + hms
  }
})

const startPanel = ref(null)
const onGetPopupContainer = (e, panel) => {
  if (!e) return

  return panel
}

const onChange = e => {
  if (e === null) {
    emits('update:value', [])
    emits('update:hms', [])
    emits('ok')
  } else {
    const [d, t] = e.split(SPLIT)
    emits('update:value', [d, d])
    emits('update:hms', [t, t])
  }
}

const onOk = e => {
  emits('ok')
}
</script>

<style lang="scss" scoped>
.custom-picker {
  overflow: hidden;
}
.panel-title {
  :deep(.date-picker-trigger) {
    visibility: hidden;
  }
}
</style>
