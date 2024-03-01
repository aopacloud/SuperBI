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
    default: () => ({}),
  },
  dataset: {
    type: Object,
    default: () => () => ({}),
  },
  origin: {
    type: [Number, String],
    default: 0,
  },
  target: {
    type: [Number, String],
    default: 0,
  },
  compare: {
    type: Object,
    default: () => ({
      mode: 1, // 显示模式, 0 默认，1 差值， 2 差值百分比
      merge: true, // 合并显示, false 单独显示， true 合并显示
    }),
  },
})

const displayTarget = computed(() => {
  return formatFieldDisplay(props.target, props.field, props.dataset.fields)
})

// 显示值
const displayValue = computed(() => {
  const { origin, target, compare } = props
  const { mode } = compare

  return getCompareDisplay(origin, target, mode)(props.field, props.dataset.fields)
})

const valStyle = computed(() => {
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
