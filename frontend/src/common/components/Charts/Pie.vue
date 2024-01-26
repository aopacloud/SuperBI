<template>
  <Chart :options="options" ref="chartRef" />
</template>

<script setup>
import Chart from './core.vue'
import echarts, { PieChart } from './utils/install.js'
echarts.use([PieChart])

const props = defineProps({
  options: {
    type: Object,
  },
})
</script>
