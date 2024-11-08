<template>
  <div class="chart-view">
    <!-- 动态维度过滤区域 -->
    <div v-if="enableDynamicFilter" class="dynamic-filter-wrapper">
      <Teleport :to="teleportTo" :disabled="!teleportTo">
        <slot
          name="dynamicFilters"
          v-bind="{ dynamicDimensions, dynamicFilterChange }"
        >
          <DynamicFilter
            :dataSource="dynamicDimensions"
            v-model="dynamicFiltersValue"
            @change="dynamicFilterChange"
          />
        </slot>
      </Teleport>
    </div>

    <div class="chart-content">
      <keep-alive>
        <RTable
          v-if="isRenderTable(renderType)"
          ref="tableRef"
          :renderType="renderType"
          :columns="renderColumns"
          :data-source="list"
          :summaryRows="summaryRows"
          :summaryMap="summaryMap"
          :options="tableConfig"
          :sorters="sortersConfig"
          :formatters="formatterConfig"
          @updateTableSorter="onTableSorterUpdate"
          @updateTableColumnsWidth="onTableColumnsWidthUpdate"
        />

        <Statistic
          v-else-if="renderType === 'statistic'"
          :fields="statisticOption.fields"
          :field="statisticOption.field"
          :value="statisticOption.value"
          :row="statisticOption.row"
          :options="statisticConfig"
          :formatters="formatterConfig"
          :compare="compare"
        />

        <RChart
          v-else
          ref="chartRef"
          :options="chartOptions"
          @chart-resized="onChartResized"
        />
      </keep-alive>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  computed,
  nextTick,
  watch,
  onBeforeUnmount,
  toRaw
} from 'vue'
import RTable from './Table/index.vue'
import RChart from 'common/components/Charts/index.vue'
import Statistic from 'common/components/Charts/Statistic.vue'
import createTable from './utils/createTable'
import createChart from './utils/createChart'
import createPieChart from './utils/createPieChart'
import createStatistic from './utils/createStatistic'
import createGroupTable from './utils/createGroupTable'
import createIntersectionTable from './utils/createIntersectionTable'
import { CATEGORY } from '@/CONST.dict'
import { isRenderTable } from '@/views/analysis/utils'
import { deepClone, debounce } from '@/common/utils/help'
import transformExport from './Table/exportUtil'
import DynamicFilter from './DynamicFilter.vue'
import { isDtField } from '@/views/dataset/utils'
import dayjs from 'dayjs'
import isBetween from 'dayjs/plugin/isBetween'
dayjs.extend(isBetween)

defineOptions({
  name: 'Chart'
})

const props = defineProps({
  from: {
    type: String,
    default: 'report',
    validator: s => ['dashboard', 'report'].includes(s)
  },
  loading: {
    type: Boolean,
    default: false
  },
  dataset: {
    type: Object,
    default: () => ({})
  },
  // 字段列
  columns: {
    type: Array,
    default: () => []
  },
  // 数据
  dataSource: {
    type: Array,
    default: () => []
  },
  summaryRows: {
    type: Array,
    default: () => []
  },
  // 选中的
  choosed: {
    type: Object,
    default: () => ({})
  },
  // 同环比
  compare: {
    type: Object
  },
  // 配置项
  options: {
    type: Object,
    default: () => ({})
  },
  // 额外参数
  extraChartOptions: {
    type: Object,
    default: () => ({})
  },
  // 是否显示动态过滤
  showDynamicFilters: {
    type: Boolean,
    default: true
  },
  // 动态过滤配置
  dynamicFilterConfig: {
    type: Object,
    default: () => ({
      size: 'middle'
    })
  },
  // 设置默认动态过滤值
  setDynamicFilterDefault: {
    type: Function
  },
  // 动态过滤区域传送的参数
  teleportTo: {
    type: [String, Object]
  }
})

// 表格ref
const tableRef = ref(null)
// 图表ref
const chartRef = ref(null)

// 渲染类型
const renderType = computed(() => props.options.renderType)

// 排序配置
const sortersConfig = computed(() => {
  if (!props.options.sorters) return []

  return props.options.sorters[renderType.value]
})

// 格式化配置
const formatterConfig = computed(() => {
  if (typeof props.options.formatters === 'undefined') {
    return props.options.table.formatter
  } else {
    return props.options.formatters
  }
})

// 表格配置
const tableConfig = computed(() => {
  const { renderType, table, compare } = props.options

  let isGroupTable = false

  if (renderType === 'groupTable') {
    // 分组类型且分组(维度字段)数量大于1
    isGroupTable =
      props.columns.filter(t => t.category === CATEGORY.PROPERTY).length > 1
  } else if (renderType === 'intersectionTable') {
    // 交叉表格类型，行分组数量大于1
    isGroupTable =
      props.columns.filter(
        t =>
          t.category === CATEGORY.PROPERTY &&
          (typeof t._group === 'undefined' || t._group === 'row')
      ).length > 1
  } else {
    isGroupTable = false
  }

  return {
    ...table,
    renderType,
    dataset: props.dataset,
    compare,
    isGroupTable
  }
})

// 图表配置
const chartConfig = computed(() => {
  const { chart, renderType, compare } = props.options
  return {
    ...chart,
    renderType,
    dataset: props.dataset,
    compare: { ...compare, merge: false }
  }
})

const statisticConfig = computed(() => {
  const { indexCard, compare } = props.options

  return {
    ...indexCard,
    dataset: props.dataset,
    compare: { ...compare, merge: false }
  }
})

// 渲染列
const renderColumns = ref([])
// 渲染行数据
const list = ref([])
// 图表实例
const ChartInstance = ref(null)
// 图表配置
const chartOptions = ref({})
// 指标卡配置
const statisticOption = reactive({})
// 汇总数据
const summaryMap = ref()

const _renderTable = res => {
  const { dataSource, columns: originColumns } = props

  return createTable({
    originFields: originColumns,
    originData: enableDynamicFilter.value
      ? dataSource.filter(onDynamicFilter)
      : dataSource,
    summaryRows: props.summaryRows,
    compare: props.compare,
    choosed: props.choosed,
    sorters: sortersConfig.value,
    config: tableConfig.value,
    datasetFields: props.dataset.fields,
    ...res
  })
}

// 生成表格
const initTable = debounce(() => {
  if (!isRenderTable(renderType.value)) return

  const { dataSource, columns: originColumns } = props

  const tableRenderMap = {
    table: () => _renderTable(),
    groupTable: createGroupTable,
    intersectionTable: createIntersectionTable
  }

  const createFn = tableRenderMap[renderType.value]

  const {
    columns,
    list: tList,
    summaryMap: summary
  } = createFn({
    originFields: originColumns,
    originData: enableDynamicFilter.value
      ? dataSource.filter(onDynamicFilter)
      : dataSource,
    summaryRows: props.summaryRows,
    compare: props.compare,
    choosed: props.choosed,
    config: tableConfig.value,
    datasetFields: props.dataset.fields
  })

  renderColumns.value = columns
  list.value = tList
  summaryMap.value = summary
}, 50)

// 生成指标卡
const initStatistic = () => {
  const {
    field,
    fields,
    value,
    row = []
  } = createStatistic({
    originFields: props.columns,
    originData: props.dataSource
  })

  statisticOption.field = field
  statisticOption.value = value
  statisticOption.row = row
  statisticOption.fields = fields
}

// 生成图表
const initChart = debounce(() => {
  const { dataSource, columns } = props

  chartOptions.value = renderChart({
    instance: ChartInstance.value,
    originFields: deepClone(columns).map(t => (delete t._group, t)),
    originData: enableDynamicFilter.value
      ? dataSource.filter(onDynamicFilter)
      : dataSource,
    datasetFields: props.dataset.fields,
    choosed: props.choosed,
    compare: props.compare,
    config: chartConfig.value,
    extraChartOptions: props.extraChartOptions,
    sorters: sortersConfig.value,
    formatters: formatterConfig.value,
    summaryRows: props.summaryRows
  })
}, 50)

const renderChart = options => {
  const renderMap = {
    bar: e => createChart({ ...e, chartType: 'bar' }),
    line: e => createChart({ ...e, chartType: 'line' }),
    pie: createPieChart
  }

  return renderMap[renderType.value]?.(options)
}

/**
 * 渲染表格、图表
 * @param {Boolean} initFilters  是否初始化动态过滤
 */
const render = (initFilters = true) => {
  console.info('--------  Components/Chart init  --------')

  const t = renderType.value

  initFilters && initDynamicFilters()

  if (['bar', 'line', 'pie'].includes(t)) {
    if (!ChartInstance.value) {
      ChartInstance.value = chartRef.value?.getInstance()
    }

    initChart()
  } else if (t === 'statistic') {
    initStatistic()
  } else {
    initTable()
  }
}

const onTableSorterUpdate = e => {
  props.options.table.sorter = [...e]
}

// 源数据改变，重新绘制
watch([() => props.dataSource, () => props.columns], () => nextTick(render), {
  immediate: true,
  deep: true
})

// 格式化变化和排序变化，图表需要重新绘制
watch(
  [sortersConfig, formatterConfig],
  () => {
    if (isRenderTable(renderType.value)) return

    initChart()
  },
  { deep: true }
)

// 表格行列转置下，排序设置变化需要重新构建表格数据
watch(
  sortersConfig,
  () => {
    if (renderType.value === 'table' && tableConfig.value.reverse) {
      initTable()
    }
  },
  { deep: true }
)

watch(renderType, (type, oldType) => {
  if (!type) return

  const charts = ['bar', 'line', 'pie'] // 图表类型
  // 防止切换图表时，图表组件未销毁，导致图表闪烁
  if (charts.includes(oldType) && !charts.includes(type)) {
    ChartInstance.value?.clear()
  }

  nextTick(render)
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

// 同环比配置变化可更细致化处理（表格内部更新列宽， 图表重新渲染）
watch(
  () => props.options.compare,
  () => {
    if (isRenderTable(renderType.value)) render()
  },
  { deep: true }
)

onBeforeUnmount(() => {
  ChartInstance.value?.dispose()
})

/**
 * 下载文件
 * @param {string} filename 文件名
 */
const download = filename => {
  let data = [],
    columns = [],
    summary = {}

  if (isRenderTable(renderType.value) && !tableConfig.value.reverse) {
    data = deepClone(list.value)
    columns = deepClone(renderColumns.value)
    summary = deepClone(summaryMap.value)
  } else {
    // 行列反转的下载当做正常明细表格下载
    const {
      columns: _columns,
      list,
      summaryMap
    } = _renderTable({
      originFields: deepClone(props.columns).map(t => (delete t._group, t)),
      config: {
        ...tableConfig.value,
        sorter: [],
        reverse: false
      }
    })

    data = deepClone(list)
    columns = deepClone(_columns)
    summary = deepClone(summaryMap)
  }

  transformExport({
    data,
    columns: columns.filter(t => t.type !== 'seq'), // 序列号列不导出
    summary,
    dataset: toRaw(props.dataset),
    tableConfig: tableConfig.value,
    sorters: sortersConfig.value,
    formatters: formatterConfig.value,
    filename
  })
}

defineExpose({ download })

// 动态维度配置
const dynamicFilterOption = computed(() => props.options.dynamicFilters || {})

// 配置的动态维度过滤
const dynamicDimensions = computed(() => {
  const { columns = [], dataSource = [], choosed = {} } = props
  const dimensions = choosed[CATEGORY.PROPERTY] || []

  return dimensions.reduce((acc, item) => {
    if (item.name in dynamicFilterOption.value) {
      const colIndex = columns.findIndex(t => t.name === item.name)
      // 动态维度字段在请求的字段中
      if (colIndex > -1) {
        const _dynamicOptions = [
          ...new Set(dataSource.map(row => row[colIndex]))
        ]
        const newItem = { ...item, _dynamicIndex: colIndex, _dynamicOptions }

        if (props.from === 'dashboard' && isDtField(item)) {
          acc.unshift(newItem)
        } else {
          acc.push(newItem)
        }
      }
    }

    return acc
  }, [])
})

const enableDynamicFilter = computed(
  () => props.showDynamicFilters && dynamicDimensions.value.length
)

const isDynamicFilterInit = ref(false)
// 动态过滤项
const dynamicFiltersValue = ref({})
// 初始化动态过滤
const initDynamicFilters = () => {
  if (!props.showDynamicFilters) return

  isDynamicFilterInit.value = true

  let initialVal = deepClone(props.options.dynamicFilters || {})

  initialVal = Object.keys(initialVal).reduce((acc, name) => {
    if (!dynamicDimensions.value.find(t => t.name === name)) return acc

    acc[name] = initialVal[name]
    return acc
  }, {})

  if (typeof props.setDynamicFilterDefault === 'function') {
    const dfs = props.setDynamicFilterDefault()
    Object.keys(dfs).forEach(name => {
      if (name in initialVal) {
        initialVal[name] = dfs[name]
      }
    })
  }

  props.options.dynamicFilters = dynamicFiltersValue.value = initialVal
}

// 动态过滤change
const dynamicFilterChange = (field, value) => {
  if (!props.options.dynamicFilters) {
    props.options.dynamicFilters = {}
  }

  dynamicFiltersValue.value[field.name] = value
  props.options.dynamicFilters[field.name] = value
}

const isDate = field => field.dataType?.includes('TIME')
// 对源数据的过滤规则
const onDynamicFilter = row => {
  const keys = Object.keys(dynamicFiltersValue.value)
  if (!keys.length) return true

  return keys.every(name => {
    const filterValues = dynamicFiltersValue.value[name]
    if (!filterValues?.length) return true

    const dimension = dynamicDimensions.value.find(t => t.name === name)
    if (!dimension) return true

    const col = row[dimension._dynamicIndex]
    if (isDate(dimension)) {
      const [s, e] = filterValues
      return dayjs(col).isBetween(dayjs(s), dayjs(e), null, '[]')
    } else {
      return filterValues.includes(col)
    }
  })
}

watch(
  dynamicFilterOption,
  () => {
    if (isDynamicFilterInit.value) {
      isDynamicFilterInit.value = false
    } else {
      render(false)
    }
  },
  { deep: true, immediate: false }
)

const onTableColumnsWidthUpdate = e => {
  props.options.table.columnsWidth = [...e]
}

// 列汇总
watch(
  [
    () => tableConfig.value.summary?.column?.enable,
    () => tableConfig.value.summary?.column?.position
  ],
  (n, o) => {
    if (!n[0] && !o[0]) return

    if (props.renderType === 'table') {
      if (!tableConfig.value.reverse) {
        initTable()
      }
    } else {
      initTable()
    }
  }
)

// 行汇总是否开启
watch(
  () => tableConfig.value.summary?.row?.enable,
  () => {
    tableConfig.value.reverse && initTable()
  }
)

// 序号
watch(() => tableConfig.value.orderSeq, initTable)

// 行列反转
watch(() => tableConfig.value.reverse, initTable)

// 特殊值 - 拖动列宽会触发？
watch(
  [
    () => tableConfig.value.special?.dimension,
    () => tableConfig.value.special?.measure
  ],
  (n, o) => {
    if (n[0] !== o[0] || n[1] !== o[1]) initTable()
  },
  { deep: true }
)

const onChartResized = () => {
  initChart()
}
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
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: auto;
}
.chart-content {
  flex: 1;
  overflow: auto;
}

.dynamic-filter-wrapper {
  width: 100%;
  // margin-bottom: 8px;
  overflow: auto;
  &:not(:empty) {
    & + .chart-content {
      margin-top: 8px;
    }
  }
}
</style>

<style lang="scss">
.dynamic-filter-datepicker {
  .ant-picker-range-separator {
    padding: 0 6px 0 0;
  }
  .ant-picker-suffix {
    margin-left: 0;
  }
}
</style>
