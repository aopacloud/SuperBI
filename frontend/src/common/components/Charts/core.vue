<template>
  <div ref="rootEl" style="width: 100%; height: 100%; overflow-x: hidden"></div>
</template>

<script setup>
import { ref, shallowRef, reactive, watch, onMounted, onBeforeUnmount } from 'vue'
import echarts from './utils/install.js'
import { gridCommon, legendCommon, colors, CHART_GRID_HEIGHT } from './utils/default.js'

const props = defineProps({
  options: {
    type: Object,
  },
})
// 图表实例
const chart = shallowRef(null)
// 根元素
const rootEl = ref(null)

// 更新图表尺寸
const updateChartSize = () => {
  const options = chart.value.getOption()
  if (!options) return
  // 图表容器高度
  const domHeight = chart.value.getDom().offsetHeight

  const gridsHeight = (options.grid || []).reduce((acc, cur) => {
    acc += CHART_GRID_HEIGHT + 10
    return acc
  }, 0)

  chart.value.resize({
    width: 'auto',
    height: gridsHeight > domHeight ? gridsHeight : 'auto',
  })
}

// 创建一个resize监听器，响应父级尺寸大小, 重置chart尺寸
const ob = new ResizeObserver(entries => {
  updateChartSize()
})

// 默认配置项
const defaultOptions = reactive({
  grid: gridCommon,
  color: colors,
  legend: legendCommon,
})
// 初始化图表
const initChart = async () => {
  chart.value = echarts.init(rootEl.value, null, {
    width: 'auto',
    height: 'auto',
  })
}

// 渲染图表
const render = options => {
  chart.value.setOption(
    {
      ...defaultOptions,
      ...options,
    },
    {
      notMerge: true,
      replaceMerge: ['legend', 'xAxis', 'yAxis', 'series'],
      lazyUpdate: true,
    }
  )
}

// 配置项改变，重新渲染
watch(
  () => props.options,
  options => {
    if (!chart.value || !options) return

    render(options)
  },
  {
    immediate: true,
    deep: true,
  }
)

onMounted(() => {
  initChart()

  ob.observe(rootEl.value.parentElement)
})

defineExpose({
  getInstance: () => chart.value,
  setOption: render,
})

onBeforeUnmount(() => {
  // 销毁图表实例
  chart.value.dispose()
  // 取消监听
  ob.disconnect()
})
</script>
