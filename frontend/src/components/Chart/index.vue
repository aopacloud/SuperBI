﻿<template>
  <div class="chart-view" style="flex: 1; overflow: auto">
    <keep-alive>
      <RTable
        v-if="
          ['table', 'groupTable', 'intersectionTable'].includes(options.renderType)
        "
        ref="tableRef"
        :columns="renderColumns"
        :data-source="list"
        :options="tableConfig" />

      <Statistic
        v-else-if="options.renderType === 'statistic'"
        :title="statisticOption.title"
        :value="statisticOption.value" />

      <RChart v-else ref="chartRef" :options="chartOptions" />
    </keep-alive>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, watch, onBeforeUnmount } from 'vue'
import RTable from './Table/index.vue'
import RChart from 'common/components/Charts/index.vue'
import Statistic from 'common/components/Charts/Statistic.vue'
import createTable from './utils/createTable'
import createChart from './utils/createChart'
import createPieChart from './utils/createPieChart'
import createStatistic from './utils/createStatistic'
import createGroupTable from './utils/createGroupTable'
import { CATEGORY } from '@/CONST.dict'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false,
  },
  dataset: {
    type: Object,
    default: () => ({}),
  },
  // 数据
  dataSource: {
    type: Array,
    default: () => [],
  },
  // 字段列
  columns: {
    type: Array,
    default: () => [],
  },
  // 选中的
  choosed: {
    type: Object,
    default: () => ({}),
  },
  // 同环比
  compare: {
    type: Object,
  },
  // 配置项
  options: {
    type: Object,
    default: () => ({}),
  },
  // 额外参数
  extraChartOptions: {
    type: Object,
    default: () => ({}),
  },
})

// 表格ref
const tableRef = ref(null)
// 图表ref
const chartRef = ref(null)

// 渲染类型
const renderType = computed(() => props.options.renderType)
// 表格配置
const tableConfig = computed(() => {
  const { renderType, table, compare } = props.options

  return {
    ...table,
    dataset: props.dataset,
    compare,
    isGroupTable:
      renderType === 'groupTable' &&
      props.columns.filter(t => t.category === CATEGORY.PROPERTY).length > 1,
  }
})
// 图表配置
const chartConfig = computed(() => {
  return {
    ...props.options.chart,
    dataset: props.dataset,
    compare: { ...props.options.compare, merge: false },
  }
})

// 渲染
const render = () => {
  console.log('--------  Components/Chart init  --------')
  const t = renderType.value

  if (['bar', 'line', 'pie'].includes(t)) {
    if (!ChartInstance.value) {
      ChartInstance.value = chartRef.value?.getInstance()
    }

    setTimeout(() => {
      initChart()
    })
  } else if (t === 'statistic') {
    initStatistic()
  } else {
    initTable()
  }
}

// 源数据改变，重新绘制
watch(
  [() => props.dataSource, () => props.columns],
  () => {
    nextTick(() => {
      render()
    })
  },
  { immediate: true, deep: true }
)

watch(renderType, (type, oldType) => {
  if (!type) return

  const charts = ['bar', 'line', 'pie'] // 图表类型
  // 防止切换图表时，图表组件未销毁，导致图表闪烁
  if (charts.includes(oldType) && !charts.includes(type)) {
    ChartInstance.value?.clear()
  }

  nextTick(() => {
    render()
  })
})

// 图表配置改变
watch(
  () => props.options.chart,
  () => {
    if (!['bar', 'line', 'pie'].includes(props.options.renderType)) return

    if (!ChartInstance.value) return

    initChart()
  },
  { deep: true }
)

// 同环比配置变化需要更新列, 重新渲染表格
watch(
  () => props.options.compare.merge,
  (n, o) => {
    if (!tableRef.value || n === o) return

    initTable()
  },
  { deep: true }
)

const renderColumns = ref([])
const list = ref([])

// 图表实例
const ChartInstance = ref(null)
// 图表配置
const chartOptions = ref({})
// 指标卡配置
const statisticOption = reactive({})

// 生成表格
const initTable = () => {
  const { dataSource, columns: originColumns } = props

  // 分组表格且分组字段列长度为1时，渲染为普通表格
  const createFn = tableConfig.value.isGroupTable ? createGroupTable : createTable

  const { columns, list: tList } = createFn({
    originData: dataSource,
    originFields: originColumns,
    compare: props.compare,
    choosed: props.choosed,
    config: tableConfig.value,
    datasetFields: props.dataset.fields,
  })

  renderColumns.value = columns
  list.value = tList
}

// 生成指标卡
const initStatistic = () => {
  const { field, value } = createStatistic({
    originFields: props.columns,
    originData: props.dataSource,
    datasetFields: props.dataset.fields,
  })

  statisticOption.title = field.displayName
  statisticOption.value = value
}

// 生成图表
const initChart = async () => {
  const { dataSource, columns } = props

  chartOptions.value = renderChart({
    instance: ChartInstance.value,
    originFields: columns,
    originData: dataSource,
    datasetFields: props.dataset.fields,
    choosed: props.choosed,
    compare: props.compare,
    config: chartConfig.value,
    extraChartOptions: props.extraChartOptions,
  })
}

const renderChart = options => {
  const renderMap = {
    bar: e => createChart({ ...e, chartType: 'bar' }),
    line: e => createChart({ ...e, chartType: 'line' }),
    pie: createPieChart,
  }

  return renderMap[renderType.value]?.(options)
}

onBeforeUnmount(() => {
  ChartInstance.value?.dispose()
})
</script>

<style lang="scss" scoped>
.chart-spin {
  width: 100%;
  height: 100%;
  :deep(.ant-spin-container) {
    height: inherit;
  }
  :deep(.ant-spin-spinning) {
    max-height: initial !important;
  }
}
.chart-view {
  width: 100%;
  height: 100%;
  overflow: auto;
  // min-width: 800px;
}
</style>
