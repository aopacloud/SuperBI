<template>
  <a-table
    size="small"
    row-key="name"
    :columns="chartAxisColumns"
    :data-source="dataSource"
    :scroll="{ x: 360 }"
    :pagination="false">
    <template #bodyCell="{ text, record, column }">
      <a-select
        v-if="column.dataIndex === 'chartType'"
        size="small"
        :default-value="left"
        v-model:value="record.chartType">
        <a-select-option value="bar">柱状图</a-select-option>
        <a-select-option value="line">折线图</a-select-option>
      </a-select>

      <a-select
        v-if="column.dataIndex === 'yAxisPosition'"
        style="width: 100%"
        size="small"
        default-value="left"
        v-model:value="record.yAxisPosition">
        <a-select-option value="left">主坐标轴</a-select-option>
        <a-select-option value="right">次坐标轴</a-select-option>
      </a-select>
    </template>
  </a-table>
</template>

<script setup>
import { chartAxisColumns } from '../config'
const props = defineProps({
  dataSource: {
    type: Array,
    default: () => [
      { displayName: 'displayName', chartType: 'line', yAxisPosition: 'left' },
    ],
  },
})
</script>
