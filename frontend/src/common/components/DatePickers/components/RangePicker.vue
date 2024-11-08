<template>
  <div class="custom-picker">
    <div class="panel left" ref="startPanel">
      <div v-show="!pickerOnly" class="panel-title">
        <span class="title-label">起始日期:</span>
        <a-date-picker
          open
          size="small"
          inputReadOnly
          placement="bottomRight"
          class="date-picker-trigger"
          popup-class-name="custom-date-picker-dropdown small"
          :valueFormat="valueFormat"
          :bordered="false"
          :getPopupContainer="e => onGetPopupContainer(e, startPanel)"
          v-model:value="start"
          @change="onStartDateChange">
          <template #dateRender="{ current }">
            <div
              class="ant-picker-cell-inner"
              :class="{ [rangeCls]: isInRange(current) }"
              :data-date="current.date()">
              {{ current.date() }}
            </div>
          </template>
        </a-date-picker>
      </div>
      <a-time-picker
        v-if="showTime"
        class="time-picker start-time"
        inputReadOnly
        placeholder="开始时间"
        valueFormat="HH:mm:ss"
        :value="hmsValue[0]"
        @change="onStartTimeChange" />
    </div>
    <div class="panel right" ref="endPanel">
      <div v-show="!pickerOnly" class="panel-title">
        <span class="title-label">结束日期:</span>
        <a-date-picker
          open
          size="small"
          inputReadOnly
          placement="bottomRight"
          class="date-picker-trigger"
          popup-class-name="custom-date-picker-dropdown small"
          :valueFormat="valueFormat"
          :bordered="false"
          :getPopupContainer="e => onGetPopupContainer(e, endPanel)"
          :disabled-date="endDisabled"
          v-model:value="end"
          @change="onEndDateChange">
          <template #dateRender="{ current }">
            <div
              class="ant-picker-cell-inner"
              :class="{ [rangeCls]: isInRange(current) }"
              :data-date="current.date()">
              {{ current.date() }}
            </div>
          </template>
        </a-date-picker>
      </div>
      <a-time-picker
        v-if="showTime"
        class="time-picker end-time"
        inputReadOnly
        placeholder="结束时间"
        valueFormat="HH:mm:ss"
        :value="hmsValue[1]"
        @change="onEndTimeChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import { getUtcDate } from '../utils'
import dayjs from 'dayjs'

const emits = defineEmits(['update:value', 'update:hms', 'change'])
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

  pickerOnly: Boolean,
})

const rangeCls = 'ant-picker-cell-in-range'

const start = ref()
const end = ref()
// 初始化
watchEffect(() => {
  const [s, e] = props.value

  if (dayjs(e).isBefore(s, 'day')) {
    start.value = e
    end.value = s
  } else {
    start.value = s
    end.value = e
  }
})
// 触发数据更新
watch([start, end], vs => {
  emits('update:value', vs.filter(Boolean))
})
// 监听结束时间，处理自某日至*逻辑
watch(end, e => {
  if (!props.extra.until) return

  const offset = dayjs().startOf('day').diff(e, 'day')
  if (offset !== 0 && offset !== 1) {
    props.extra.until = undefined
  }
})

// 时分秒
const hmsValue = ref([])
watchEffect(() => {
  hmsValue.value = props.hms
})
watch(hmsValue, hms => {
  emits('update:hms', hms)
})

// 单元格是否isInRange
const isInRange = current => {
  if (start.value === undefined || end.value === undefined) return false

  return (
    (current.isSame(start.value, 'day') || current.isAfter(start.value, 'day')) &&
    (current.isBefore(end.value, 'day') || current.isSame(end.value, 'day'))
  )
}

// 开始日期不可选
const startDisabled = current => {
  return current && current > getUtcDate(props.utcOffset).endOf('day')
}

// 结束日期不可选
const endDisabled = current => {
  if (!start.value) return true //startDisabled(current)

  return current <= dayjs(start.value).subtract(1, 'day').endOf('day')
}

const onStartDateChange = e => {
  emits('change', { start: e, end: end.value })
  // 没有结束时间，选中为开始时间
  if (!end.value) end.value = e
  // 开始时间大于结束时间时，结束时间等于开始时间
  if (dayjs(e).isAfter(end.value)) {
    end.value = e
  }
}

const onEndDateChange = e => {
  emits('change', { start: start.value, end: e })
}

const startPanel = ref(null)
const endPanel = ref(null)
const onGetPopupContainer = (e, panel) => {
  if (!e) return

  return panel
}

const createDisableRange = start => {
  return Array.from({ length: start }, (_, i) => i + 1)
}

// TODO 时分秒的不可选
const endTimeDisabled = current => {
  if (!hmsValue.value[0]) return true

  if (dayjs(end.value).isAfter(start.value)) {
    return {
      disabledHours: [],
      disabledMinutes: [],
      disabledSeconds: [],
    }
  } else {
    if (end.value === start.value) {
      const [[sH = 0, sM = 0, sS = 0], [eH = 0, eM = 0, eS = 0]] =
        hmsValue.value.map(item => item?.split(':') || [])

      if (eH > sH) {
        return {}
      } else {
        if (sM > eM) {
          return {
            disabledHours: createDisableRange(sH),
          }
        } else {
          if (eS > eS) {
            return {
              disabledHours: createDisableRange(sH),
              disabledMinutes: createDisableRange(sM),
            }
          }
        }
      }
    }
  }
}

const isHmsBefore = (s = '', e = '') => {
  return parseInt(s?.replace(':', '')) > parseInt(e?.replace(':', ''))
}

const onStartTimeChange = e => {
  if (!e) {
    hmsValue.value[0] = '00:00:00'
    return
  }

  if (!hmsValue.value[1]) {
    hmsValue.value[1] = '23:59:59'
  }

  // 开始时间大于结束时间
  if (isHmsBefore(e, hmsValue.value[1])) {
    message.info('开始时间不能大于结束时间')
    return
  }

  hmsValue.value[0] = e
}
const onEndTimeChange = e => {
  if (!e) {
    hmsValue.value[1] = '23:59:59'
    return
  }

  if (!hmsValue.value[0]) {
    hmsValue.value[0] = '00:00:00'
  }

  if (isHmsBefore(hmsValue.value[0], e)) {
    // 结束时间大于开始时间
    message.info('结束时间不能小于开始时间')
    return
  }

  hmsValue.value[1] = e
}
</script>
