<template>
  <Chart :options="options" ref="chartRef" />
</template>

<script setup>
import Chart from './core.vue'
import echarts, { BarChart } from './utils/install.js'
echarts.use([BarChart])

const props = defineProps({
  options: {
    type: Object,
  },
})
</script>
