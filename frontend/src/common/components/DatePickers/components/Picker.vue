<template>
  <div class="picker">
    <div class="panel left" ref="startPanel">
      <div class="panel-title">
        <span class="title-label">起始日期:</span>
        <a-date-picker
          open
          size="small"
          inputReadOnly
          placement="bottomRight"
          class="date-picker-trigger"
          popup-class-name="date-picker-dropdown small"
          :bordered="false"
          :getPopupContainer="e => onGetPopupContainer(e, startPanel)"
          v-model:value="start"
          @change="onStartchange">
          <template #dateRender="{ current }">
            <div
              class="ant-picker-cell-inner"
              :class="{ [rangeCls]: isInRange(current) }"
              :data-date="current.date()"
              @click="emits('click')">
              {{ current.date() }}
            </div>
          </template>
        </a-date-picker>
      </div>
    </div>
    <div class="panel right" ref="endPanel">
      <div class="panel-title">
        <span class="title-label">结束日期:</span>
        <a-date-picker
          open
          size="small"
          inputReadOnly
          placement="bottomRight"
          class="date-picker-trigger"
          popup-class-name="date-picker-dropdown small"
          :bordered="false"
          :getPopupContainer="e => onGetPopupContainer(e, endPanel)"
          :disabled-date="endDisabled"
          v-model:value="end">
          <template #dateRender="{ current }">
            <div
              class="ant-picker-cell-inner"
              :class="{ [rangeCls]: isInRange(current) }"
              :data-date="current.date()"
              @click="emits('click')">
              {{ current.date() }}
            </div>
          </template>
        </a-date-picker>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, watchEffect } from 'vue'
import { getUtcDate } from '../utils'
import dayjs from 'dayjs'

const emits = defineEmits(['update:value', 'click'])
const props = defineProps({
  format: {
    type: String,
    default: 'YYYY-MM-DD',
  },
  value: {
    type: Array,
    default: () => [],
  },
  utcOffset: {
    type: Number,
    default: 8,
    validator: str => {
      return typeof str === 'number'
    },
  },
})

const rangeCls = 'ant-picker-cell-in-range'

const start = ref()
const end = ref()
// 触发数据更新
watch([start, end], vals => {
  emits('update:value', vals.filter(Boolean))
})

// 单元格是否isInRange
const isInRange = current => {
  if (start.value === undefined || end.value === undefined) return false

  return (
    (current.isSame(start.value, 'day') || current.isAfter(start.value, 'day')) &&
    (current.isBefore(end.value, 'day') || current.isSame(end.value, 'day'))
  )
}
// 初始化
watchEffect(() => {
  const [s, e] = props.value

  start.value = typeof s === 'string' ? dayjs(s) : s
  end.value = typeof e === 'string' ? dayjs(e) : e
})

// 开始日期不可选
const startDisabled = current => {
  return current && current > getUtcDate(props.utcOffset).endOf('day')
}

// 结束日期不可选
const endDisabled = current => {
  if (!start.value) return true //startDisabled(current)

  return current <= start.value.subtract(1, 'day').endOf('day')
}

const onStartchange = e => {
  // 没有结束时间，选中为开始时间
  if (!end.value) end.value = e
  // 开始时间大于结束时间时，结束时间等于开始时间
  if (e?.isAfter(end.value)) end.value = e
}

const startPanel = ref(null)
const endPanel = ref(null)
const onGetPopupContainer = (e, panel) => {
  if (!e) return

  return panel
}
</script>

<style lang="scss" scoped>
.picker {
  display: flex;
  min-width: 490px;
  margin-top: 10px;
}
.panel {
  flex: 1;
  display: flex;
  flex-direction: column;

  &.right {
    margin-left: 15px;
  }
}

.panel-title {
  display: flex;
  align-items: center;
  width: 234px;
  padding-left: 10px;
}
.title-label {
  padding: 0 10px;
  font-size: 14px;
  color: #aaa;
}
.date-picker-trigger {
  flex: 1;
  padding-right: 20px;
  :deep(.ant-picker-input) > input {
    color: #1677ff;
  }
}
</style>
<style lang="scss">
.date-picker-dropdown {
  position: static !important;
  margin-top: 10px !important;
  .ant-picker-panel-container {
    box-shadow: none !important;
    border: 1px solid #e9e9e9;
  }
  .ant-picker-cell-selected {
    .ant-picker-cell-in-range::after {
      background-color: transparent;
    }
  }

  .ant-picker-cell {
    .ant-picker-cell-inner {
      position: static !important;
    }
    &:not(.ant-picker-cell-today) {
      .ant-picker-cell-inner {
        &::before {
          content: '';
          position: absolute;
          inset: 0;
          width: 100%;
          height: 100% !important;
          z-index: 9;
        }
      }
    }

    &:not(.ant-picker-cell-selected) {
      .ant-picker-cell-in-range {
        &::before {
          content: '';
          position: absolute;
          left: 0;
          right: 0;
          height: 24px;
          background-color: #e6f4ff;
          transition: none;
          z-index: -1;
        }
        &::after {
          content: attr(data-date);
          position: absolute;
          left: 0;
          right: 0;
          top: 50%;
          height: 24px;
          background-color: #e6f4ff;
          transform: translateY(-50%);
          transition: all 0.3s;
          z-index: 1;
        }
      }
    }
  }

  .ant-picker-footer {
    display: none;
  }
}
</style>
