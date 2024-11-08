<template>
  <!-- 合并显示 -->
  <span class="target" v-if="compare.merge">{{ displayTarget }}</span>

  <!-- 非合并显示 -->
  <span>
    <span v-if="compare.merge"> ( </span>
    <span class="origin" :style="valStyle">{{ displayValue }}</span>
    <span v-if="compare.merge"> ) </span>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { formatFieldDisplay } from '@/components/Chart/utils/index.js'
import { COMPARE } from '@/CONST.dict'
import { getCompareDisplay } from '../utils'

const props = defineProps({
  field: {
    type: Object,
    default: () => ({})
  },
  dataset: {
    type: Object,
    default: () => () => ({})
  },
  origin: {
    type: [Number, String],
    default: 0
  },
  target: {
    type: [Number, String],
    default: 0
  },
  dTarget: {
    type: [Number, String]
  },
  compare: {
    type: Object,
    default: () => ({
      mode: 1, // 显示模式, 0 默认，1 差值， 2 差值百分比
      merge: true // 合并显示, false 单独显示， true 合并显示
    })
  },
  config: {
    type: Object,
    default: () => ({})
  },
  isSummary: Boolean, // 是否汇总行的同环比， 是 则不显示数值的颜色
  displayed: String // 直接显示的值，不参与组件内的逻辑计算
})

const displayTarget = computed(() => {
  return (
    props.dTarget ||
    formatFieldDisplay(props.target, props.field, props.dataset.fields)
  )
})

// 显示值
const displayValue = computed(() => {
  if (props.displayed) return props.displayed

  const { origin, target } = props

  const s = getCompareDisplay({
    origin,
    target,
    mode: props.compare.mode,
    config: props.config
  })(props.field, props.dataset.fields)

  return s
})

const valStyle = computed(() => {
  if (props.isSummary || props.displayed) return

  const { origin, target } = props
  let color = ''

  if (target === origin) {
    color = ''
  } else {
    if (target / origin === Infinity) {
      color = props.compare.mode === COMPARE.MODE.DIFF_PERSENT ? '' : 'green'
    } else {
      color = target > origin ? 'green' : 'red'
    }
  }

  return { color }
})
</script>

<style lang="scss" scoped></style>
