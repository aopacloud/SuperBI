<template>
  <section class="box" :id="`box_${reportId}`" :data-reportId="reportId">
    <header class="header graggable-handler">
      <div class="title">
        <div class="name">
          <a class="report-name" target="_blank" @click="toChartPage">
            {{ report.name }}
          </a>
        </div>
        <a-space class="tools">
          <a-tooltip
            :title="`数据更新至: ${
              report.dataset ? report.dataset.latestPartitionValue : '-'
            }`">
            <FieldTimeOutlined
              class="flex-center"
              style="
                width: 24px;
                height: 24px;
                font-size: 20px;
                color: rgba(0, 0, 0, 0.88);
              " />
          </a-tooltip>

          <a-button
            type="text"
            size="small"
            :icon="h(ReloadOutlined)"
            @click="reload" />

          <a-dropdown trigger="click">
            <a-button type="text" size="small" :icon="h(MoreOutlined)" />
            <template #overlay>
              <a-menu @click="onMoreMenuClick">
                <a-menu-item key="sql">查看SQL</a-menu-item>
                <a-menu-item key="download">下载</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </div>
      <div class="sub-title">{{ subTitle }}</div>
    </header>
    <main class="content">
      <a-spin class="box-spin" :spinning="loading">
        <!-- 初始化 -->
        <a-empty v-if="isInit" class="box-empty" />

        <!-- 数据集无权限 -->
        <BoxUnaccess
          v-else-if="
            report.dataset.permission !== 'WRITE' &&
            report.dataset.permission !== 'READ'
          "
          :dataset="report.dataset"
          @apply="emits('dataset-apply', report.dataset)" />

        <!-- 查询异常 -->
        <div v-else-if="!isQuerySuccess" style="text-align: center">
          <img src="@/assets/svg/chartBox_error.svg" style="width: 200px" />
          <p style="color: #999">查询异常</p>
        </div>

        <!-- 无数据 -->
        <div
          v-else-if="
            requestResponse.layout.renderType !== 'table' && !dataSource.length
          "
          style="text-align: center">
          <img src="@/assets/svg/chartBox_empty.svg" style="width: 200px" />
          <p style="color: #999">当前查询条件下暂无数据</p>
        </div>

        <Chart
          v-else
          :choosed="choosed"
          :columns="columns"
          :dataSource="dataSource"
          :compare="requestResponse.request.compare"
          :dataset="report.dataset"
          :options="requestResponse.layout"
          :extraChartOptions="{ grid: { left: 50, right: 50, bottom: 40 } }" />
      </a-spin>
    </main>
  </section>
</template>

<script setup>
import { h, ref, reactive, computed, watch, shallowRef, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  FieldTimeOutlined,
  MoreOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'
import Chart from '@/components/Chart/index.vue'
import { postAnalysisQuery } from '@/apis/analysis'
import { getDetailById } from '@/apis/report'
import { CATEGORY } from '@/CONST.dict'
import { transformColumns } from '@/views/analysis/utils'
import { deepClone } from 'common/utils/help'
import { storagePrefix } from '@/settings'
import { DASHBORD_TO_REPORT_NAME } from '../config'
import BoxUnaccess from './BoxUnaccess.vue'
import emittor from 'common/plugins/emittor'
import { versionJs } from '@/versions'
import { getStartDateStr, getEndDateStr } from 'common/components/DatePickers/utils'
import dayjs from 'dayjs'

const router = useRouter()

const emits = defineEmits(['sql', 'download', 'dataset-apply', 'loaded', 'reload'])
const props = defineProps({
  detail: {
    type: Object,
    default: () => ({}),
  },
  reportId: {
    type: [String, Number],
  },
  globalDateConfig: {
    type: Object,
    default: () => ({}),
  },
  shouldLoad: {
    type: Boolean,
  },
  item: {
    type: Object,
    default: () => ({}),
  },
  // 全局筛选过滤器
  filters: {
    type: [Array, Object],
    default: () => [],
  },
  observer: {
    type: Object,
    default: () => ({}),
  },
  timeOffset: {
    type: String,
    default: '+8',
  },
})

const onMoreMenuClick = ({ key }) => {
  if (key === 'sql') {
    emits('sql', requestResponse.response)
  } else if (key === 'download') {
    emits('download', downloadQueryParams.value)
  }
}

// 获取看板图表详情地址
const getReportDetailHref = () => {
  if (!props.reportId) return

  const routeRes = router.resolve({
    name: 'ReportDetail',
    params: { id: props.reportId },
  })
  if (!routeRes) return

  return routeRes.href
}

const toChartPage = () => {
  const href = getReportDetailHref()
  if (!href) return

  window.open(href, DASHBORD_TO_REPORT_NAME)

  try {
    // 使用 LocalStorage 存贮作为新打开页面的初始化缓存筛选条件

    const payload = {
      dashboard: props.detail,
      filters: mergedFilters.value,
    }

    window.localStorage.setItem(
      storagePrefix + DASHBORD_TO_REPORT_NAME,
      JSON.stringify(payload)
    )
  } catch (error) {
    throw error
  }
}

const loading = ref(false)
const requestResponse = reactive({
  request: {},
  response: {},
  layout: {},
})

// 是否请求成功过
const isQuerySuccess = computed(() => {
  return requestResponse.response.status === 'SUCCESS'
})
// 初始化
const isInit = computed(() => {
  return Object.keys(requestResponse.response).length === 0
})

const _n = category => category.toLowerCase() + 's'
// 从数据集字段中恢复category
const _addCategoryFromDatasetFields = (t, category) => {
  const item =
    (report.value.dataset?.fields || []).find(f => f.name === t.name) || {}

  // t.displayName !== item.displayName 兼容旧数据的重命名
  const displayName =
    t.displayName !== item.displayName
      ? t.displayName
      : t._modifyDisplayName ?? item.displayName

  return {
    ...t,
    displayName: displayName + '',
    category,
  }
}

// 选中的字段
const choosed = computed(() => {
  const requestRes = requestResponse.request

  return {
    [CATEGORY.PROPERTY]: (requestRes[_n(CATEGORY.PROPERTY)] ?? [])
      .filter(filterFields)
      .map(t => _addCategoryFromDatasetFields(t, CATEGORY.PROPERTY)),
    [CATEGORY.INDEX]: (requestRes[_n(CATEGORY.INDEX)] ?? [])
      .filter(filterFields)
      .map(t => _addCategoryFromDatasetFields(t, CATEGORY.INDEX)),
    [CATEGORY.FILTER]: (requestRes[_n(CATEGORY.PROPERTY)] ?? [])
      .filter(filterFields)
      .map(_addCategoryFromDatasetFields),
  }
})

// 过滤掉非数据集中的字段
const filterFields = item => {
  const fields = report.value.dataset?.fields || []

  return fields.filter(t => t.status !== 'HIDE').some(t => t.name === item.name)
}

// 字段列
const columns = computed(() => {
  // 使用查询成功的请求字段
  const fields = [
    ...choosed.value[CATEGORY.PROPERTY],
    ...choosed.value[CATEGORY.INDEX],
  ]

  return transformColumns({
    fields,
    fieldNames: requestResponse.response.fieldNames,
  })
})
const dataSource = computed(() => requestResponse.response.rows || [])
// 副标题
const subTitle = computed(() => {
  return versionJs.ViewsDashboard.displaySubTitle(
    queryFilters.value,
    report.value.dataset,
    props.timeOffset
  )
})

// 图表详情
const report = ref({})
const fetchReportDetail = async () => {
  try {
    const {
      layout,
      queryParam = '{}',
      ...rest
    } = await getDetailById(props.reportId)

    report.value = { ...rest }
    requestResponse.request = {
      ...JSON.parse(queryParam),
      fromSource: 'dashboard',
    }
    requestResponse.layout = JSON.parse(layout)
    props.item.report = { ...rest }

    emits('reload', rest)
  } catch (error) {
    console.error('获取数据集错误', error)
  }
}

// 合并后的过滤条件
const mergedFilters = shallowRef([])
// 查询过滤条件
const queryFilters = shallowRef([])
// 全局筛选的值转换为图表筛选条件
const toFilterItem = item => {
  const { filterType, filterMethod, fieldName, value } = item

  if (filterType === 'TIME') {
    // 日期
    const { date = [], mode = 0, offset = [], extra = {}, hms } = value
    return {
      name: fieldName,
      logical: 'AND',
      conditions: [
        {
          useLatestPartitionValue: !!extra.dt,
          functionalOperator: 'BETWEEN',
          timeType: mode === 0 ? 'RELATIVE' : 'EXACT',
          args: mode === 0 ? [...offset] : [...date],
          timeParts: hms,
        },
      ],
    }
  } else if (filterType === 'TEXT' || filterType === 'NUMBER') {
    // 文本、数值
    return {
      name: fieldName,
      logical: ['AND', 'ALL'].includes(filterMethod) ? filterMethod : 'AND',
      conditions: value.map(val => {
        return {
          functionalOperator: val.operator,
          args: [val.value],
        }
      }),
    }
  } else if (filterType === 'CUSTOM') {
    // 自定义
    return {
      name: fieldName,
      logical: 'AND',
      conditions: [
        {
          functionalOperator: 'IN',
          args: value,
        },
      ],
    }
  } else {
    // 枚举
    return {
      name: fieldName,
      logical: 'AND',
      conditions: [
        {
          functionalOperator: 'IN',
          args: Array.isArray(value) ? value : [value],
        },
      ],
    }
  }
}

// 更新日期筛选项
const updateFilterItem = filter => {
  if (!filter.dataType.includes('TIME')) {
    return filter
  } else {
    const cond = filter.conditions[0]
    const { timeType = 'RELATIVE', _this, _until } = cond

    if (!!_until) {
      const endDate = getEndDateStr(
        { type: 'day', offset: _until.split('_')[1] },
        props.timeOffset
      )
      cond.args[1] = endDate
      cond.timeType = 'EXACT'

      return {
        ...filter,
        conditions: [cond],
      }
    } else if (timeType === 'RELATIVE' && _this) {
      // 相对时间的当月在查询时重新计算
      const [tp, of = 0] = _this.split('_')
      const s = getStartDateStr(
        { type: tp.toLowerCase(), offset: +of },
        props.timeOffset
      )
      const e = getEndDateStr(
        { type: tp.toLowerCase(), offset: +of },
        props.timeOffset
      )
      const sDiff = dayjs().startOf('day').diff(s, 'day')
      const eDiff = dayjs().endOf('day').diff(e, 'day')

      return {
        ...filter,
        conditions: [
          {
            ...cond,
            args: [sDiff, eDiff],
          },
        ],
      }
    } else {
      return filter
    }
  }
}

const queryFiltersHandler = () => {
  const resFilters = deepClone(requestResponse.request.filters || [])

  return versionJs.ViewsDashboard.queryFilters(
    resFilters.map(updateFilterItem),
    mergedFilters.value
  )
}

const reload = async () => {
  await fetchReportDetail()

  runQuery()
}

// 下载的查询参数
const downloadQueryParams = ref({})

const runQuery = async () => {
  try {
    loading.value = true

    //  处理全局筛选
    mergedFilters.value = versionJs.ViewsDashboard.mergeBoxFilters(
      props.filters,
      props.globalDateConfig,
      toFilterItem,
      props.timeOffset
    )

    queryFilters.value = queryFiltersHandler()

    const p = _n(CATEGORY.PROPERTY),
      i = _n(CATEGORY.INDEX)

    const requestParams = {
      ...requestResponse.request,
      [p]: requestResponse.request[p].filter(filterFields),
      [i]: requestResponse.request[i].filter(filterFields),
      filters: queryFilters.value.filter(filterFields),
    }

    const res = await postAnalysisQuery(requestParams)

    downloadQueryParams.value = requestParams // 下载的请求参数，不可直接赋值给reqeust，因为reqeust的filters会使用queryFiltersHandler
    requestResponse.response = res
  } catch (error) {
    console.error('看板项查询错误', error)
  } finally {
    loading.value = false
  }
}

// 监听全局日期过滤
watch(() => props.globalDateConfig, runQuery, { deep: true })
// 监听全局筛选
watch(() => props.filters, runQuery, { deep: true })
// 监听刷新
watch(
  () => props.shouldLoad,
  async b => {
    if (b) {
      fetchReportDetail()
        .then(runQuery)
        .then(() => {
          props.item._load = true
          emits('loaded', props.reportId)
        })
    }
  },
  { immediate: true }
)

// 数据集申请成功回调
const onDatasetApplying = e => {
  report.value.dataset.applying = true
  if (e.id === report.value.dataset.id) {
    report.value.dataset.applying = e.applying
  }
}

onMounted(() => {
  emittor.on('dataset-applying', onDatasetApplying)
})
</script>

<style lang="scss" scoped>
.box {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: 0.15s;

  &:hover {
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
    .title .tools {
      opacity: 1;
    }
  }
}

.header {
  padding: 16px 12px 8px;
}
.title {
  display: flex;
  align-items: center;
  .name {
    flex: 1;
    font-size: 16px;
    @extend .ellipsis;
  }
  .tools {
    margin-left: 20px;
    transition: 0.1s;
    opacity: 0;
  }
  .report-name {
    color: inherit;
    &:hover {
      text-decoration: underline;
    }
  }
}
.sub-title {
  margin-top: 6px;
  color: #999;
}
.content {
  flex: 1;
  overflow: auto;
  padding: 8px 12px;
  :deep(.ant-spin-nested-loading) {
    height: 100%;
    .ant-spin-container {
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }
  }
}
.box-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  :deep(.ant-empty-image) {
    overflow: hidden;
  }
}
</style>
