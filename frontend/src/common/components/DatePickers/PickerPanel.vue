<template>
  <section class="custom-picker-section">
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
        :single="single"
        :options="options"
        :id="id"
        :showTime="showTime"
        :utcOffset="currentUtc"
        :value="modelValue"
        :extra="extraValue"
        @click="onPresetClick" />

      <div
        class="content"
        :style="{
          paddingBottom: showTime && !single ? '40px' : '',
        }">
        <component
          ref="pickerRef"
          :is="pickerComponent"
          :format="format"
          :valueFormat="valueFormat"
          :extra="extraValue"
          :utcOffset="currentUtc"
          :showTime="showTime"
          v-model:value="modelValue"
          v-model:hms="hmsValue"
          @change="onPickerChange"
          @ok="ok" />
      </div>
    </main>

    <footer
      class="footer"
      :style="{ margin: single && showTime ? '-12px 10px 0px' : '' }">
      <div class="left flex-inline">
        <a-tooltip arrowPointAtCenter placement="topLeft" :title="utcOffsetTooltip">
          <InfoCircleOutlined
            style="position: relative; font-size: 16px"
            :style="{ top: single && showTime ? '-10px' : '' }" />
        </a-tooltip>
        <div
          v-if="!single"
          :id="`pick-custom-placeholder-${id}`"
          style="margin-left: 16px"></div>
      </div>

      <a-space v-if="!single || !showTime">
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
import RangePicker from './components/RangePicker.vue'
import SinglePicker from './components/SinglePicker.vue'
import { getStartDateStr, getEndDateStr, getUtcDate } from './utils'
import { InfoCircleOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'

defineOptions({
  inheritAttrs: false,
})

const emits = defineEmits([
  'update:moda',
  'update:offset',
  'update:value',
  'update:hms',
  'update:extra',
  'ok',
  'cancel',
])
const props = defineProps({
  // 单个面板
  single: { type: Boolean },
  // mode 会与外层 dropdown 的mode属性冲突， 什么原因？？？
  // 0 动态时间，1 静态时间 },
  moda: { type: Number, default: 0 },

  // 只显示静态或动态时间
  modeOnly: { type: Number },

  // 值
  value: { type: Array, default: () => [] },

  // 格式化
  format: { type: String, default: 'YYYY-MM-DD' },

  // 值的格式化
  valueFormat: { type: String, default: 'YYYY-MM-DD' },

  // 显示时分秒
  showTime: { type: Boolean },

  // 时分秒的值
  hms: { type: Array },

  placeholder: { type: String, default: '请选择日期' },

  // 偏移量值
  offset: { type: Array },

  extra: { type: Object, default: () => ({}) },

  utcOffset: { type: String, default: '+8' },

  // 一些配置参数
  options: { type: Object, default: () => ({}) },
})

// 是否显示时分秒
const showTime = computed(() => props.showTime)

// 当前时区偏移量
const currentUtc = computed(() => +props.utcOffset)

const utcOffsetTooltip = computed(() => {
  return `当前时区为: UTC${props.utcOffset}:00`
})

const pickerComponent = computed(() => {
  return props.single ? SinglePicker : RangePicker
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

const hmsValue = ref([])

// 根据当前日期获取偏移量
const getOffsets = (dates = []) => {
  if (!dates.length) {
    return []
  } else {
    const [s, e] = dates

    const sDiff = getUtcDate(currentUtc.value).startOf('day').diff(s, 'day')
    const eDiff = getUtcDate(currentUtc.value).startOf('day').diff(e, 'day')

    return [sDiff, eDiff]
  }
}

// 根据偏移量获取默认日期
const getByOffset = (offsets = []) => {
  const [s = 1, e = 1] = offsets // 默认昨天
  const start = getUtcDate(currentUtc.value)
    .endOf('day')
    .subtract(s, 'day')
    .format(props.format)
  const end = getUtcDate(currentUtc.value)
    .endOf('day')
    .subtract(e, 'day')
    .format(props.format)

  return [start, end]
}

const pickerRef = ref(null)
// 初始化
const init = () => {
  pickerRef.value?.open?.()

  const { moda, modeOnly, offset, value, hms = [], extra = {} } = props

  modeKey.value = modeOnly ?? moda
  offsetValue.value = offset ?? offsetValue.value
  hmsValue.value = [...hms]
  extraValue.value = { ...extra }

  if (extra.dt) {
    modelValue.value = []
  } else if (!!extra.until) {
    // 自某日至昨日、今, [静态时间，动态时间] => ['2024-03-19', 0] => [静态时间, 静态时间] => ['2024-03-19', '2024-03-20']
    // 使用 extra.until until_-1 | until_0 去处理
    const endDate = getEndDateStr(
      { type: 'day', offset: extra.until.split('_')[1] },
      currentUtc.value
    )

    modelValue.value = [value[0], endDate]
  } else {
    // 静态
    if (moda === 1) {
      modelValue.value = value.length === 1 ? [value[0], value[0]] : [...value]
      extraValue.value.current = undefined
      extraValue.value.isCustom = undefined
    } else {
      const { current, isCustom } = extra

      extraValue.value.isCustom = isCustom

      if (current) {
        const [tp, of = 0] = current.split('_')
        const s = getStartDateStr(
          { type: tp.toLowerCase(), offset: +of },
          currentUtc.value
        )
        const e = getEndDateStr(
          { type: tp.toLowerCase(), offset: +of },
          currentUtc.value
        )

        modelValue.value = [s, e]
      } else {
        extraValue.value.current = undefined
        if (Array.isArray(offset)) {
          modelValue.value = getByOffset(
            offset.length === 1 ? [offset[0], offset[0]] : [...offset]
          )
        } else {
          // 默认昨天
          const yDay = dayjs().subtract(1, 'day')
          const yDayStr = getUtcDate(currentUtc.value, yDay).format('YYYY-MM-DD')

          modelValue.value = [yDayStr, yDayStr]
        }
      }
    }
  }
}

/**
 * 主面板点击
 * 清空 extra.current、isCustom、dt
 */
const onPickerChange = () => {
  extraValue.value.current = undefined
  extraValue.value.dt = undefined
  extraValue.value.isCustom = undefined
}

// 重置面板
const resetOpen = () => {
  pickerRef.value?.close?.()
}

defineExpose({ init, resetOpen })

watch(() => props.utcOffset, init)

const onReset = () => {
  modelValue.value = []
  offsetValue.value = []
  hmsValue.value = []
  extraValue.value.dt = undefined
  extraValue.value.current = undefined
  extraValue.value.isCustom = undefined
  extraValue.value.until = undefined
}

// 预设快捷
const onPresetClick = item => {
  const { offset, type, custom } = item

  extraValue.value.dt = type === 'dt'
  extraValue.value.isCustom = !!custom

  if (type === 'dt') {
    modelValue.value = []
    modelValue.offset = [0, 0]
    extraValue.value.current = undefined
    extraValue.value.until = undefined
  } else {
    // 自某日至今、昨日
    if (type === 'until') {
      extraValue.value.current = undefined
      extraValue.value.isCustom = undefined
      extraValue.value.until = type + '_' + offset

      if (!modelValue.value[0]) {
        modelValue.value[0] = getUtcDate(currentUtc.value)
          .endOf('day')
          .subtract(1, 'day')
          .format(props.format)
      }

      modelValue.value[1] = getUtcDate(currentUtc.value)
        .endOf('day')
        .subtract(Math.abs(offset), 'day')
        .format(props.format)

      return
    }

    extraValue.value.until = undefined

    // 自定义
    if (custom) {
      extraValue.value.current = type.toUpperCase() + '_' + offset
    } else {
      // 动态
      if (props.moda === 0) {
        if (type === 'week') {
          extraValue.value.current = 'WEEK_' + offset
        } else if (type === 'month') {
          extraValue.value.current = 'MONTH_' + offset
        } else {
          extraValue.value.current = undefined
        }
      } else {
        // 静态
        extraValue.value.current = undefined
      }
    }

    const s = getStartDateStr({ type, offset }, currentUtc.value)
    const e = getEndDateStr({ type, offset }, currentUtc.value)

    modelValue.value = [s, e]
  }
}

// 描述文字
const desc = computed(() => {
  if (extraValue.value.dt) return '有数的一天'

  if (!modelValue.value || !modelValue.value.length) return props.placeholder

  const [start, end] = modelValue.value
  const [sTime = '', eTime = ''] = hmsValue.value

  if (!start) {
    return props.placeholder
  } else {
    const startLabel = start + (props.showTime && !!sTime ? ' ' + sTime : '')

    if (props.single) {
      return startLabel
    } else {
      if (!end) return props.placeholder

      const endLabel = end + (props.showTime && !!eTime ? ' ' + eTime : '')
      return startLabel + ' ~ ' + endLabel
    }
  }
})

const ok = () => {
  const offsets = getOffsets(modelValue.value)
  const values = [...modelValue.value]
  const moda = +!!extraValue.value.until || modeKey.value

  offsetValue.value = offsets

  if (props.single) {
    const value = modelValue.value.length ? [modelValue.value[0]] : undefined
    const offset = offsets.length ? [offsets[0]] : undefined
    const hms = [...hmsValue.value]

    emits('update:moda', moda)
    emits('update:offset', offset)
    emits('update:value', value)
    emits('update:hms', hms)
    emits('update:extra', extraValue.value)

    emits('ok', {
      moda: modeKey.value,
      value: value,
      offset: offset,
      hms: hms,
      extra: {
        ...extraValue.value,
        current: undefined,
        isCustom: undefined,
        until: undefined,
      },
    })

    return
  }

  // 自某日至昨日、今, [静态时间，动态时间] => ['2024-03-19', 0]
  if (!!extraValue.value.until) {
    const endOffset = extraValue.value.until.split('_')[1]

    values[1] = +endOffset
    offsets[0] = values[0]
  }

  emits('update:moda', moda)
  emits('update:offset', offsets)
  emits('update:value', values)
  emits('update:hms', hmsValue.value)
  emits('update:extra', extraValue.value)
  emits('ok', {
    moda: modeKey.value,
    offset: offsets,
    value: values,
    hms: hmsValue.value,
    extra: extraValue.value,
  })
}

const onCancel = () => {
  emits('cancel')
}
</script>

<style lang="scss" scoped>
.custom-picker-section {
  display: inline-flex;
  flex-direction: column;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12),
    0 9px 28px 8px rgba(0, 0, 0, 0.05);
}

.main,
.content {
  flex: 1;
}
.main {
  position: relative;
  display: flex;
  padding: 10px 10px 0;
}
.content {
  margin-left: 10px;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 12px 10px 0;
}
</style>

<style lang="scss">
@import './picker.scss';
</style>
