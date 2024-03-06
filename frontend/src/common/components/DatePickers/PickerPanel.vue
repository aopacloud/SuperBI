<template>
  <section class="section">
    <header class="header">
      <HeaderTabs
        :tabOnly="modeOnly"
        :value="modelValue"
        v-model:tab="modeKey"
        @reset="onReset">
        <template #desc>{{ desc }}</template>
      </HeaderTabs>
    </header>

    <main class="main">
      <AsideQuick
        class="aside"
        style="width: 165px; margin-top: 10px"
        :options="options"
        :id="id"
        :utcOffset="currentUtc"
        :value="modelValue"
        :extra="extraValue"
        @click="onPresetClick" />

      <div class="content" style="margin-left: 10px">
        <MainPicker
          :utcOffset="currentUtc"
          v-model:value="modelValue"
          @click="onPickerClick" />
      </div>
    </main>

    <footer class="footer">
      <div class="left flex-inline">
        <a-tooltip arrowPointAtCenter placement="topLeft" :title="utcOffsetTooltip">
          <InfoCircleOutlined style="margin-right: 16px; font-size: 16px" />
        </a-tooltip>
        <div :id="`pick-custom-placeholder-${id}`"></div>
      </div>
      <a-space>
        <a-button @click="onCancel">取消</a-button>
        <a-button type="primary" @click="ok">确定</a-button>
      </a-space>
    </footer>
  </section>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import HeaderTabs from './components/HeaderTabs.vue'
import AsideQuick from './components/AsideQuick.vue'
import MainPicker from './components/Picker.vue'
import { getStartDate, getEndDate, getUtcDate } from './utils'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import { InfoCircleOutlined } from '@ant-design/icons-vue'
dayjs.extend(utc)

const emits = defineEmits([
  'update:moda',
  'update:offset',
  'update:value',
  'update:extra',
  'ok',
  'cancel',
])
const props = defineProps({
  format: {
    type: String,
    default: 'YYYY-MM-DD',
  },
  moda: {
    // mode 会与外层 dropdown 的mode属性冲突， 什么原因？？？
    type: Number,
    default: 0, // 0 动态时间，1 静态时间
  },
  modeOnly: {
    type: Number,
  },
  value: {
    type: Array,
  },
  placeholder: {
    type: String,
    default: '请选择日期',
  },
  offset: {
    type: Array,
  },
  extra: {
    type: Object,
    default: () => ({}),
  },
  utcOffset: {
    type: String,
    default: '+8',
  },
  // 一些配置参数
  options: {
    type: Object,
    default: () => ({}),
  },
})

// 当前时区偏移量
const currentUtc = computed(() => +props.utcOffset)

const utcOffsetTooltip = computed(() => {
  return `当前时区为: UTC${props.utcOffset}:00`
})

const id = ref(Date.now())
// 模式 1 动态， 0 静态
const modeKey = ref(1)
// 值
const modelValue = ref()
// 跨度
const offsetValue = ref()
// 额外参数
const extraValue = ref({})

// 将字符串转为dayjs格式
const _format2dayjs = (dates = []) => {
  return dates.map(t => dayjs(t))
}
// 将dayjs格式转为字符串
const _format2string = (dates = []) => {
  return dates.map(t => t.format(props.format))
}

// 根据当前日期获取偏移量
const getOffsets = (dates = []) => {
  if (!dates.length) {
    return []
  } else {
    const [s, e] = dates
    const sDiff = getUtcDate(currentUtc.value).diff(s, 'day')
    const eDiff = getUtcDate(currentUtc.value).diff(e, 'day')

    return [sDiff, eDiff]
  }
}

// 根据偏移量获取默认日期
const getByOffset = (offsets = []) => {
  const [s = 0, e = 0] = offsets
  const start = getUtcDate(currentUtc.value).subtract(s, 'day') //.format('YYYY-MM-DD')
  const end = getUtcDate(currentUtc.value).subtract(e, 'day') //.format('YYYY-MM-DD')

  return [start, end]
}

// 初始化
const init = () => {
  const { moda, modeOnly, offset, value, extra = {} } = props

  modeKey.value = modeOnly ?? moda
  offsetValue.value = offset ?? offsetValue.value

  extraValue.value = { ...extra }

  if (extra.dt) {
    modelValue.value = []
  } else {
    // 静态
    if (moda === 1) {
      modelValue.value = _format2dayjs(value.filter(Boolean))

      extraValue.value.current = undefined
      extraValue.value.isCustom = undefined
    } else {
      const { current, isCustom } = extra

      extraValue.value.isCustom = isCustom

      if (current) {
        const [tp, of = 0] = current.split('_')
        const s = getStartDate({ type: tp.toLowerCase(), offset: +of }, currentUtc.value)
        const e = getEndDate({ type: tp.toLowerCase(), offset: +of }, currentUtc.value)

        modelValue.value = [s, e]
      } else {
        extraValue.value.current = undefined
        modelValue.value = Array.isArray(offsetValue.value)
          ? getByOffset(offsetValue.value)
          : []
      }
    }
  }
}

/**
 * 主面板点击
 */
const onPickerClick = () => {
  // 清空 extra.current、isCustom、dt
  extraValue.value.current = undefined
  extraValue.value.dt = undefined
  extraValue.value.isCustom = undefined
}

defineExpose({ init })

watch(() => props.utcOffset, init)

const onReset = () => {
  offsetValue.value = []
  nextTick(() => {
    modelValue.value = []
  })
}

// 预设快捷
const onPresetClick = item => {
  const { offset, type, custom } = item

  extraValue.value.dt = type === 'dt'
  extraValue.value.isCustom = !!custom

  if (type === 'dt') {
    modelValue.value = []
    modelValue.offset = [0, 0]
    extraValue.value.dt = true
  } else {
    // 自定义
    if (custom) {
      extraValue.value.current = type.toUpperCase() + '_' + offset
    } else {
      // 静态
      if (props.moda === 0) {
        if (type === 'week') {
          extraValue.value.current = 'WEEK_' + offset
        } else if (type === 'month') {
          extraValue.value.current = 'MONTH_' + offset
        } else {
          extraValue.value.current = undefined
        }
      } else {
        // 动态
        extraValue.value.current = undefined
      }
    }

    const s = getStartDate({ type, offset }, currentUtc.value)
    const e = getEndDate({ type, offset }, currentUtc.value)

    modelValue.value = [s, e]
  }
}

// 描述文字
const desc = computed(() => {
  if (extraValue.value.dt) return '有数的一天'

  if (!modelValue.value || !modelValue.value.length) return props.placeholder

  const [start, end] = modelValue.value
  if (!start || !end) return props.placeholder

  const startLabel = dayjs(start).format(props.format) ?? '开始时间'
  const endLabel = dayjs(end).format(props.format) ?? '结束时间'

  return startLabel + ' ~ ' + endLabel
})

const ok = () => {
  const offsets = getOffsets(modelValue.value)
  const dates = _format2string(modelValue.value)

  offsetValue.value = offsets

  emits('update:moda', modeKey.value)
  emits('update:offset', offsets)
  emits('update:value', dates)
  emits('update:extra', extraValue.value)
  emits('ok', {
    moda: modeKey.value,
    offset: offsets,
    date: dates,
    extra: extraValue.value,
  })
}

const onCancel = () => {
  emits('cancel')
}
</script>

<style lang="scss" scoped>
section {
  display: inline-flex;
  flex-direction: column;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12),
    0 9px 28px 8px rgba(0, 0, 0, 0.05);
}

main,
.content {
  flex: 1;
}
main {
  display: flex;
  padding: 0 10px;
}

footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 10px 12px;
}
</style>
