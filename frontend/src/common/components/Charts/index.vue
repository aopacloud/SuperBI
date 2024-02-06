<template>
  <Chart ref="chartRef" :options="options" />
</template>

<script setup>
import { ref } from 'vue'
import Chart from './core.vue'
import echarts, { BarChart, LineChart, PieChart } from './utils/install.js'
echarts.use([BarChart, LineChart, PieChart])

const props = defineProps({
  options: {
    type: Object,
  },
})
const chartRef = ref(null)

defineExpose({
  getInstance: () => chartRef.value.getInstance(),
})
</script>
