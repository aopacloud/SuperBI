<template>
  <Chart :options="options" ref="chartRef" />
</template>

<script setup>
import Chart from './core.vue'
import echarts, { LineChart } from './utils/install.js'
echarts.use([LineChart])

const props = defineProps({
  options: {
    type: Object,
  },
})
</script>
