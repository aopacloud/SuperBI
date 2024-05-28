<template>
  <section class="wrapper">
    <header>
      <LayoutTitle
        :chart="chartDetail"
        :dataset="datasetDetail"
        @update-dataset="onDatasetUpdate"
        @update-chart="onChartUpdate"
        @timeoffset-change="e => (timeOffset = e)" />
    </header>
    <main class="main">
      <Splitpanes class="default-theme main-splitpanes">
        <Pane style="min-width: 240px" :size="20" :max-size="55">
          <aside class="aside">
            <AsideDatasetInfo
              style="border-bottom: 1px solid #eee"
              :chart="chartDetail"
              :dataset="datasetDetail"
              @dataset-toggled="onDatasetToggled" />
            <Splitpanes horizontal style="flex: 1; overflow: auto">
              <Pane class="field-list-spin" style="min-height: 36px">
                <a-spin :spinning="datasetDetailLoading">
                  <AsideFieldList
                    title="维度"
                    :category="CATEGORY.PROPERTY"
                    :data-source="dimensionList"
                    :hasDatasetAnalysis="hasDatasetAnalysisPermission"
                    @dbclick="e => onFieldListDbclick(e, CATEGORY.PROPERTY)" />
                </a-spin>
              </Pane>
              <Pane class="field-list-spin" style="min-height: 36px">
                <a-spin :spinning="datasetDetailLoading">
                  <AsideFieldList
                    title="指标"
                    :category="CATEGORY.INDEX"
                    :data-source="indexList"
                    :hasDatasetAnalysis="hasDatasetAnalysisPermission"
                    @dbclick="e => onFieldListDbclick(e, CATEGORY.INDEX)" />
                </a-spin>
              </Pane>
            </Splitpanes>
          </aside>
        </Pane>
        <Pane>
          <LayoutContent
            ref="layoutContentRef"
            :chart="chartDetail"
            :dataset="datasetDetail"
            :dimensions="chooseDimensions"
            :indexes="chooseIndexes"
            :filters="chooseFilters"
            :options="settingOptions" />
        </Pane>
      </Splitpanes>

      <AsideSetting
        :hasDatasetAnalysis="hasDatasetAnalysisPermission"
        v-model:options="settingOptions" />
    </main>
  </section>

  <!-- <BasisRatioModal v-model:open="basisRatioModalOpen" /> -->
</template>

<script setup>
import {
  ref,
  reactive,
  computed,
  provide,
  onBeforeMount,
  onMounted,
  onBeforeUnmount,
  watch,
} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import LayoutTitle from './LayoutTitle.vue'
import AsideDatasetInfo from './LayoutAside/DatasetInfo.vue'
import AsideFieldList from './LayoutAside/FieldList.vue'
import LayoutContent from './LayoutContent/index.vue'
import AsideSetting from './LayoutAside/Setting.vue'
import { getDetailById as getDatasetDetail } from '@/apis/dataset'
import { getDetailById as getReportDetail } from '@/apis/report'
import { CATEGORY } from '@/CONST.dict.js'
import {
  defaultRenderType,
  defaultQueryTotal,
  defaultCompareOptions,
  defaultTableOptions,
  defaultChartOptions,
} from './defaultOptions'
import useUserStore from '@/store/modules/user'
import { getByIncludesKeys, deepClone } from 'common/utils/help'
import { getRandomKey } from 'common/utils/string'
import { storagePrefix } from '@/settings'
import { DASHBORD_TO_REPORT_NAME } from '@/views/dashboard/modify/config'
import useAppStore from '@/store/modules/app'
import { versionJs } from '@/versions'
import { getNameByJoinAggregator } from './utils'
import { uninitWorker } from '@/components/Chart/Table/exportUtil'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 时区偏移
const timeOffset = computed(() => appStore.activeTimeOffset)

// 当前空间ID
const currentWorkspaceId = computed(() => appStore.workspaceId)

// 数据集分析权限
const hasDatasetAnalysisPermission = computed(() => {
  if (userStore.hasPermission('DATASET:ANALYSIS:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('DATASET:ANALYSIS:HAS:PRIVILEGE')) {
    return (
      datasetDetail.value.permission === 'READ' ||
      datasetDetail.value.permission === 'WRITE'
    )
  } else {
    return false
  }
})

// 数据集管理权限
const hasDatasetManagePermission = computed(() => {
  if (userStore.hasPermission('DATASET:MANAGE:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('DATASET:MANAGE:HAS:PRIVILEGE')) {
    return (
      datasetDetail.value.permission === 'READ' ||
      datasetDetail.value.permission === 'WRITE'
    )
  } else {
    return false
  }
})

// 图表查看权限
const hasReadPermission = computed(() => {
  if (userStore.hasPermission('REPORT:READ:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('REPORT:READ:HAS:PRIVILEGE')) {
    return (
      chartDetail.value.permission === 'READ' ||
      chartDetail.value.permission === 'WRITE'
    )
  } else {
    return false
  }
})

// 图表编辑权限
const hasWritePermission = computed(() => {
  if (userStore.hasPermission('REPORT:WRITE:ALL:WORKSPACE')) {
    return true
  } else if (!chartDetail.value.id) {
    return userStore.hasPermission('REPORT:VIEW:CREATE')
  } else if (userStore.hasPermission('REPORT:WRITE:CREATE')) {
    return chartDetail.value.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('REPORT:WRITE:HAS:PRIVILEGE')) {
    return chartDetail.value.permission === 'WRITE'
  } else {
    return false
  }
})

// 图表管理权限（发布，移动，下线，删除，共享，推送）
const hasManagePermission = computed(() => {
  if (userStore.hasPermission('REPORT:MANAGE:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('REPORT:MANAGE:CREATE')) {
    return chartDetail.value.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('REPORT:MANAGE:HAS:PRIVILEGE')) {
    return chartDetail.value.permission === 'WRITE'
  } else {
    return false
  }
})

// 图表详情
const loading = ref(false)
const chartDetail = ref({
  name: '查询',
})

const init = async () => {
  const {
    name,
    params: { id },
    query = {},
  } = route

  // 旧版本路由记录
  if (name === 'DatasetAnalysisDetail') {
    await fetchDatasetDetail(id)

    if (query.chartId) {
      fetchChartDetail(query.chartId)
    }
  } else if (name === 'ReportDetail') {
    fetchChartDetail(id)
  } else {
    fetchDatasetDetail(id)
  }
}

// 获取图表详情
const fetchChartDetail = async id => {
  try {
    loading.value = true

    const res = await getReportDetail(id)
    chartDetail.value = { ...res }

    await fetchDatasetDetail(res.dataset.id)

    setOptions(undefined, JSON.parse(res.layout))
    setRecovert(res.queryParam)

    autoRun()
  } catch (error) {
    console.error('获取图表详情错误', error)
  } finally {
    loading.value = false
  }
}
const layoutContentRef = ref(null)
const autoRun = () => {
  layoutContentRef.value?.toolsRun()
}
// 数据集更新
const onDatasetUpdate = e => {
  datasetDetail.value = { ...datasetDetail.value, ...e }
}

// 是否应该刷新页面
const shouldReloadPage = ref(true)
// 保存后图表更新
const onChartUpdate = e => {
  if (chartDetail.value.id !== e.id) {
    const { query } = route

    shouldReloadPage.value = false
    router.replace({ name: 'ReportDetail', params: { id: e.id }, query })
  }

  chartDetail.value = { ...chartDetail.value, ...e }
}

// 初始化过滤条件
const initChooseFilters = () => {
  const { force = true, filters } = datasetDetail.value.extraConfig

  if (!force) {
    chooseFilters.value = []
  } else {
    const allFields = [...dimensionList.value, ...indexList.value]

    if (typeof filters === 'undefined') {
      // 初始化 dt
      const dtField = allFields.find(versionJs.ViewsDatasetModify.isDt)
      if (!dtField) {
        chooseFilters.value = []
      } else {
        chooseFilters.value = [
          {
            ...dtField,
            _forced: true,
            _latestPartitionValue: datasetDetail.value.latestPartitionValue,
          },
        ]
      }
    } else {
      chooseFilters.value = filters.map(t => {
        const field = allFields.find(f => f.name === t.name)
        return {
          ...t,
          displayName: field?.displayName,
          _forced: true,
        }
      })
    }
  }
}

// ------ 所有数据集详情 start ------ //
const datasetDetailLoading = ref(false)
const datasetDetail = ref({})
const fetchDatasetDetail = async id => {
  try {
    datasetDetailLoading.value = true

    const res = await getDatasetDetail(id)

    if (res.workspaceId && res.workspaceId !== currentWorkspaceId.value) {
      await appStore.setWorkspaceId(res.workspaceId, true)
    }

    datasetDetail.value = {
      ...res,
      extraConfig:
        typeof res.extraConfig === 'string' ? JSON.parse(res.extraConfig) : {},
    }

    initChooseFilters()
  } catch (error) {
    console.error('获取数据集详情错误', error)
  } finally {
    datasetDetailLoading.value = false
  }
}

// 更新数据集，防止查询时数据集被修改
const fetchDatasetLastest = async datasetId => {
  try {
    const res = await getDatasetDetail(datasetId)

    // 当前版本与最新版本不一致，更新数据集
    if (res.version !== datasetDetail.value.version) {
      datasetDetail.value = res
      updateChoosed()
    } else {
      // 查询时更新数据集时间时更新筛选项日期时间
      updateFilters()
    }
  } catch (error) {
    console.error('获取最新数据集失败', error)
  }
}

// 更新筛选项日期时间
const updateFilters = () => {
  chooseFilters.value = [...chooseFilters.value]
}

// 更新选中的字段
const updateChoosed = () => {
  const fields = [...dimensionList.value, ...indexList.value]

  chooseDimensions.value = chooseDimensions.value
    .filter(t => dimensionList.value.some(f => f.name === t.name))
    .map(t => {
      const item = dimensionList.value.find(p => p.name === t.name)

      return {
        ...t,
        expression: item.expression,
        displayName: item.displayName,
      }
    })

  chooseIndexes.value = chooseIndexes.value
    .filter(t => fields.some(f => f.name === t.name))
    .map(t => {
      const item = fields.find(i => i.name === t.name)

      return {
        ...t,
        expression: item.expression,
        displayName: t._modifyDisplayName || item.displayName,
      }
    })

  chooseFilters.value = chooseFilters.value
    .filter(t => fields.some(f => f.name === t.name))
    .map(t => {
      const item = fields.find(f => f.name === t.name)

      return {
        ...t,
        displayName: item.displayName,
      }
    })
}

// 数据集切换
const onDatasetToggled = e => {
  // 修改地址栏参数（注意看是否有监听路由做的处理）
  router.replace({
    name: 'DatasetAnalysis',
    params: { id: e.id },
    query: route.query,
  })

  resetChoosed()
  setRequestResponse()
  chartDetail.value = {
    name: '查询',
  }
}

// 维度列表
const dimensionList = computed(() => {
  return datasetDetail.value.fields?.filter(
    t => t.status !== 'HIDE' && t.category === CATEGORY.PROPERTY
  )
})
// 指标列表
const indexList = computed(() => {
  return datasetDetail.value.fields?.filter(
    t => t.status !== 'HIDE' && t.category === CATEGORY.INDEX
  )
})
// ------ 所有数据集详情 end ------ //

// 选中的维度
const chooseDimensions = ref([])
// 选中的指标
const chooseIndexes = ref([])
const onFieldListDbclick = (e, category) => {
  if (!hasDatasetAnalysisPermission.value) return

  const prevChoosed = getChoosed(category)

  const preIndex = prevChoosed.findIndex(t => t.id === e.id)
  if (preIndex > -1 && category === CATEGORY.PROPERTY) {
    prevChoosed.splice(preIndex, 1)
  }

  const item = { ...e, _id: getRandomKey() }

  prevChoosed.push(item)
  updateCompareDimensions(item)
}

// 选中的筛选条件
const chooseFilters = ref([])

// 获取选中
const getChoosed = category => {
  const map = {
    [CATEGORY.PROPERTY]: chooseDimensions.value,
    [CATEGORY.INDEX]: chooseIndexes.value,
    [CATEGORY.FILTER]: chooseFilters.value,
  }

  return map[category] || map
}
// 设置选中
const setChoosed = (category, values) => {
  if (typeof values === 'undefined') return

  if (category === CATEGORY.PROPERTY) {
    chooseDimensions.value = values
  } else if (category === CATEGORY.INDEX) {
    chooseIndexes.value = values
  } else if (category === CATEGORY.FILTER) {
    // version
    if (!values.length) {
      chooseFilters.value = chooseFilters.value.filter(t => t._forced) // versionJs.ViewsAnalysis.clearFilters(chooseFilters.value)
    } else {
      chooseFilters.value = values
    }
  }
}
// 移除某一个选中
const removeChoosed = item => {
  const category = item.category

  if (category === CATEGORY.PROPERTY) {
    const index = chooseDimensions.value.findIndex(t => t.name === item.name)
    if (index < 0) return

    chooseDimensions.value.splice(index, 1)
  } else if (category === CATEGORY.INDEX) {
    const index = chooseIndexes.value.findIndex(t => t.name === item.name)
    if (index < 0) return

    chooseIndexes.value.splice(index, 1)
  } else if (category === CATEGORY.FILTER) {
    const index = chooseFilters.value.findIndex(t => t.name === item.name)
    if (index < 0) return

    chooseFilters.value.splice(index, 1)
  }
}
// 重置选中
const resetChoosed = () => {
  chooseDimensions.value = []
  chooseIndexes.value = []
  chooseFilters.value = []
}

// topN
const topN = reactive({
  sortField: undefined,
  sortType: 'desc',
  limit: 50,
})
const setTopN = ({ sortField, limit = 50 } = {}) => {
  topN.sortField = sortField
  topN.limit = limit
}

// 对比
const compare = ref()
const setCompare = e => {
  compare.value = e
}

// 查询分页
const paging = ref({
  limit: defaultQueryTotal,
})
const setPaging = (val = { limit: defaultQueryTotal }, key) => {
  if (typeof key === 'undefined') {
    paging.value = val
  } else {
    paging.value[key] = val
  }
}

// 拖拽的字段
const draggingField = ref()
// 配置项
const settingOptions = ref({
  // 渲染类型
  renderType: defaultRenderType,
  // 表格配置
  table: {
    ...defaultTableOptions,
  },
  // 图表配置
  chart: { ...defaultChartOptions },
  compare: { ...defaultCompareOptions },
})
const getOptions = key =>
  typeof key === 'string' ? settingOptions.value[key] : settingOptions.value
const setOptions = (key, value) => {
  if (typeof key === 'undefined') {
    settingOptions.value = value
  } else {
    settingOptions.value[key] = value
  }
}

// 选中字段更新配置项
const updateByChoosedIndex = (list = []) => {
  updateTopN(list)
  updateAxisOptions(list)
  updateComparOptions(list)
}

// 更新默认排序
const updateDefaultSorter = list => {
  const { sorter } = settingOptions.value.table

  if (!list.length) {
    sorter.field = undefined

    return
  }

  if (!sorter.field) {
    const first = list[0]
    sorter.field = first.name + '.' + first.aggregator
    sorter.order = topN.sortType
  }
}

const updateTopN = list => {
  const { sortField } = topN

  if (typeof sortField === 'undefined') return
  if (list.some(item => getNameByJoinAggregator(item) === sortField)) return

  const item = list.find(item => getNameByJoinAggregator(item) === sortField)
  if (!item) {
    topN.sortField = undefined
  }
}

// 更新坐标轴设置
const updateAxisOptions = list => {
  const axis = settingOptions.value.chart?.axis ?? []

  settingOptions.value.chart.axis = list.map(t => {
    const item = axis.find(a => a.name === t.name)
    return {
      ...t,
      chartType: item?.chartType ?? settingOptions.value.renderType,
      yAxisPosition: item?.yAxisPosition ?? 'left',
    }
  })
}

// 更新同环比配置
const updateComparOptions = list => {
  // 更新同环比
  if (!list.length) {
    setCompare()
  } else {
    if (!compare.value) return

    const { measures = [] } = compare.value

    compare.value.measures = measures.filter(t =>
      chooseIndexes.value.some(i => i.name === t.name)
    )
  }
}

// 更新同环比对比维度
const updateCompareDimensions = async list => {
  await nextTick()

  const old = compare.value?.dimensions

  // 历史数据无对比维度字段
  if (typeof old === 'undefined') return

  list = Array.isArray(list) ? list : [list]

  const newList = list
    .map(item => getByIncludesKeys(item, ['name', 'dataType']))
    .filter(t => old.every(i => i.name !== t.name))

  compare.value.dimensions = [...old, ...newList]
}

const updateCompareDimensionsByChoosed = (list = []) => {
  if (!compare.value) return

  const { timeField, dimensions = [] } = compare.value

  if (typeof compare.value.dimensions === 'undefined') return

  compare.value.dimensions = dimensions.filter(t =>
    list.some(i => i.name === t.name)
  )

  // 同环比配置的对比字段不在维度中，重新配置
  if (list.every(t => t.name !== timeField)) {
    compare.value.timeField = undefined
    compare.value.measures = []
  }
}

// 更新维度
const updateByChoosedDimension = (ls = []) => {
  updateCompareDimensionsByChoosed(ls)
}

watch(chooseDimensions, updateByChoosedDimension, { deep: true })

watch(chooseIndexes, updateByChoosedIndex, { immediate: true, deep: true })

// 查询成功响应
const requestResponse = ref({})
const setRequestResponse = (e = {}) => {
  requestResponse.value = deepClone(e)
}

const requestResponseUpdate = e => {
  updateDefaultSorter(chooseIndexes.value)
}

watch(() => requestResponse.value.response, requestResponseUpdate)

// 从queryParam中恢复字段信息
const setRecovert = paylaod => {
  if (typeof paylaod === 'undefined') return

  const params = typeof paylaod === 'string' ? JSON.parse(paylaod) : paylaod
  const {
    dimensions = [],
    measures = [],
    filters = [],
    sorts = [],
    compare,
    paging,
  } = params

  updateChoosedFromChart({ dimensions, measures, filters })

  // setChoosed(CATEGORY.PROPERTY, dimensions)
  // setChoosed(CATEGORY.INDEX, measures)
  // setChoosed(CATEGORY.FILTER, filters)
  setCompare(compare ?? undefined)
  setPaging(paging)
  setTopN(sorts[0])
}

// 从数据集中更新选中的字段
const updateChoosedFromChart = ({
  dimensions = [],
  measures = [],
  filters = [],
} = {}) => {
  const fields = [...dimensionList.value, ...indexList.value]
  // 维度
  chooseDimensions.value = dimensions
    .filter(t => fields.some(f => f.name === t.name))
    .map(t => {
      const item = fields.find(it => it.name === t.name)

      return {
        ...t,
        _id: getRandomKey(),
        category: item.category,
        displayName: item.displayName,
      }
    })

  // 指标
  chooseIndexes.value = measures
    .filter(t => fields.some(f => f.name === t.name))
    .map(t => {
      const item = fields.find(it => it.name === t.name)

      // t.displayName !== item.displayName 兼容历史数据
      const displayName =
        t.displayName !== item.displayName
          ? t.displayName
          : t._modifyDisplayName ?? item.displayName

      return {
        ...t,
        _id: getRandomKey(),
        category: item.category,
        displayName,
      }
    })

  const { force, filters: forcedFilters = [] } = datasetDetail.value.extraConfig

  // 过滤条件
  chooseFilters.value = filters
    .filter(t => fields.some(f => f.name === t.name))
    .map(t => {
      const item = fields.find(it => it.name === t.name)

      // 数据集中配置的强制过滤
      const forceItem = force ? forcedFilters.find(ft => ft.name === t.name) : null

      return {
        ...t,
        ...item,
        _id: getRandomKey(),
        _forced: !!forceItem,
        filterMode: forceItem?.filterMode,
        dataType: item.dataType,
        category: item.category,
        displayName: item.displayName,
      }
    })
}

provide('index', {
  autoRun,
  timeOffset,
  permissions: {
    dataset: {
      hasAnalysis: () => hasDatasetAnalysisPermission.value,
      hasManage: () => hasDatasetManagePermission.value,
    },
    chart: {
      hasRead: () => hasReadPermission.value,
      hasWrite: () => hasWritePermission.value,
      hasManage: () => hasManagePermission.value,
    },
  },
  // 数据集
  dataset: {
    get: () => datasetDetail.value,
    update: async () => fetchDatasetLastest(datasetDetail.value.id),
  },
  // 拖拽的字段
  draggingField: {
    get: () => draggingField.value,
    set: e => {
      draggingField.value = e
    },
  },
  // 选中
  choosed: {
    get: getChoosed,
    set: setChoosed,
    remove: removeChoosed,
    clear: category => setChoosed(category, []),
    reset: resetChoosed,
  },
  // topN
  topN: {
    get: () => topN,
    set: setTopN,
  },
  // 同环比
  compare: {
    get: () => compare.value,
    set: setCompare,
    update: {
      dimensions: updateCompareDimensions,
    },
  },
  paging: {
    get: key => {
      return typeof key === 'undefined' ? paging.value : paging.value[key]
    },
    set: (key, value) => setPaging(value, key),
  },
  // 配置
  options: {
    get: getOptions,
    set: setOptions,
    clear: key => (setOptions(key, undefined), undefined),
    reset: () => (settingOptions.value = undefined),
  },
  // 请求响应
  requestResponse: {
    get: key => requestResponse.value[key] || requestResponse.value,
    set: setRequestResponse,
  },
  // 恢复
  recovert: setRecovert,
  // 重置
  reset: () => {
    resetChoosed()
    // 初始化过滤条件
    initChooseFilters()
    // 清空请求响应
    setRequestResponse()
    // 清空同环比
    setCompare()
    // 清空topN
    setTopN()
  },

  dashboardFilters: {
    get: () => dashboardFilters.value,
    reset() {
      dashboardFilters.value = {}

      layoutContentRef.value?.toolsRun()
    },
  },
})

// 从看板过来的查询条件
const dashboardFilters = ref({})

watch(
  route,
  newRoute => {
    // fix 跳转到别的路由下的错误请求
    if (
      !['DatasetAnalysisDetail', 'ReportDetail', 'DatasetAnalysis'].includes(
        newRoute.name
      )
    )
      return

    if (!shouldReloadPage.value) {
      shouldReloadPage.value = true

      return
    }

    init()
  },
  { immediate: true, deep: true }
)
onMounted(() => {
  try {
    // 从看板页面新打开的标签且有缓存，使用缓存作为初始化查询参数
    const localParameters = window.localStorage.getItem(
      storagePrefix + DASHBORD_TO_REPORT_NAME
    )

    if (
      window.name === DASHBORD_TO_REPORT_NAME &&
      typeof localParameters === 'string'
    ) {
      dashboardFilters.value = JSON.parse(localParameters)
    }

    // init()
  } catch (error) {
    throw error
  } finally {
    // 使用完即移除
    setTimeout(() => {
      window.name = '' // 重置window.name，让每次window.open都是新标签(貌似无法清除???)
      window.localStorage.removeItem(storagePrefix + DASHBORD_TO_REPORT_NAME)
    }, 10)
  }
})

onBeforeMount(() => {
  appStore.toggleSideBarHide(true)
})
onBeforeUnmount(() => {
  appStore.toggleSideBarHide(false)
  uninitWorker()
})
</script>

<style scoped lang="scss">
$border-color: #eee;

.wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  .splitpanes.default-theme .splitpanes__pane {
    background-color: #fff;
  }
}

.main {
  flex: 1;
  display: flex;
  position: relative;
  overflow: auto;
  .main-splitpanes {
    flex: 1;
    overflow: hidden;
  }
  .main-content {
    overflow: auto;
  }
}
.aside {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #fff;
  .field-list-spin {
    & > :deep(.ant-spin-nested-loading) {
      height: 100%;
      .ant-spin-container {
        height: 100%;
      }
    }
  }
}
</style>
