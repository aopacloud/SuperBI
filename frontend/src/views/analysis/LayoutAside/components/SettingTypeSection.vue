﻿<template>
  <SettingSectionLayout title="图表类型">
    <ul class="chart-list">
      <template v-for="(item, key) in chartListMap" :key="key">
        <a-tooltip :title="item.label">
          <li
            class="chart-item"
            :class="{ active: key === cType }"
            @click="handleClick(key)">
            <component :is="item.icon" />
          </li>
        </a-tooltip>
      </template>
    </ul>
  </SettingSectionLayout>
</template>

<script setup>
import { shallowReactive, ref, watchEffect, watch } from 'vue'
import SettingSectionLayout from './SettingSectionLayout.vue'
import { chartTypeMap } from '../config'

const emits = defineEmits(['update:type', 'change'])
const props = defineProps({
  type: {
    type: String,
    default: 'table',
  },
})

// 类型列表
const chartListMap = shallowReactive(chartTypeMap)

// 当前类型
const cType = ref('table')
const handleClick = type => {
  if (cType.value !== type) {
    emits('change', type)
  }
  cType.value = type
}
// 监听 props 中的 type 变化
watchEffect(() => {
  cType.value = props.type
})

watch(cType, t => {
  emits('update:type', t)
})
</script>
<style lang="scss" scoped>
$item-size: 32px;

.chart-item {
  display: inline-flex;
  width: $item-size;
  height: $item-size;
  align-items: center;
  justify-content: center;
  margin: 6px;
  border-radius: 4px;
  text-align: center;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.2s;

  &.active {
    background-color: #1677ff;
    font-weight: 600;
  }
}
</style>
