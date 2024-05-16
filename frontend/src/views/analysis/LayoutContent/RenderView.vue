<template>
  <div class="render-view">
    <!-- 使用 keepAlive 会导致图表渲染闪烁 -->

    <!-- loading -->
    <a-spin _comment_="loading" v-if="loading" class="flex-column flex-center" />

    <!-- 初始胡 -->
    <a-empty _comment_="初始胡" v-else-if="!queryStarted" class="flex-column flex-center">
      <template #description>
        <div style="color: #999">请拖入/更改分析字段</div>
      </template>
    </a-empty>

    <!-- 无权限 -->
    <UnAccess _comment_="无权限" v-else-if="!hasDatasetAnalysis" :dataset="dataset" />

    <!-- 查询异常 -->
    <div _comment_="查询异常" v-else-if="queryStatus === 'FAILED'" class="flex-column flex-center">
      <img src="@/assets/svg/chartBox_error.svg" style="width: 200px" alt="查询异常" />
      <p style="color: #999;text-align: center;">
        查询异常 <span v-if="resError">( {{ resError }} )</span>
        <a-spin v-if="reasonLoading"  size="small" style="margin-left: 6px" />
        <div v-if="errorReason" style="margin-top: 4px;">AI分析: {{ errorReason }}</div>
      </p>
    </div>

    <!-- 无数据 -->
    <div _comment_="无数据"
      v-else-if="options.renderType !== 'table' && !rows.length"
      class="flex flex-column flex-center">
      <img src="@/assets/svg/chartBox_empty.svg" style="width: 200px" alt="无数据" />
      <p style="color: #999">当前查询条件下暂无数据</p>
    </div>

    <!-- 查询成功 -->
    <Chart _comment_="查询成功"
      v-else
      :choosed="choosed"
      :columns="columns"
      :dataSource="rows"
      :summaryRows="summaryRows"
      :compare="compare"
      :dataset="dataset"
      :options="options" />
  </div>
</template>

<script setup>
import { ref, computed, watch, inject } from 'vue'
import Chart from '@/components/Chart/index.vue'
import { transformColumns } from '@/views/analysis/utils'
import { CATEGORY } from '@/CONST.dict'
import UnAccess from '../components/UnAccess.vue'
import useError from '@/hooks/useError'

const { fetchReason, reason: errorReason, reasonLoading } = useError()

const props = defineProps({
  loading: { type: Boolean },
  dataset: {
    type: Object,
    default: () => ({}),
  },
  choosed: {
    type: Object,
    default: () => ({}),
  },
  compare: {
    type: Object,
  },
  options: {
    type: Object,
    default: () => ({}),
  },
})

// 查询开始
const queryStarted = ref(false)

const { requestResponse: indexRequestResponse, permissions: indexPermissions } =
  inject('index', {})

const hasDatasetAnalysis = computed(() => indexPermissions.dataset.hasAnalysis())

// 查询请求
const requestRes = computed(() => indexRequestResponse.get('request'))
watch(
  requestRes,
  r => {
    if (Object.keys(r).length === 0) return

    createColumns()
  },
  { deep: true }
)

const columns = ref([])
const createColumns = () => {
  const { fieldNames = [] } = responseRes.value
  // 使用选中的字段渲染表头（查询成功后）
  const pFields = requestRes.value[CATEGORY.PROPERTY.toLowerCase() + 's'].map(
    t => ((t.category = CATEGORY.PROPERTY), t)
  )
  const iFields = requestRes.value[CATEGORY.INDEX.toLowerCase() + 's'].map(
    t => ((t.category = CATEGORY.INDEX), t)
  )

  columns.value = transformColumns({
    fields: [...pFields, ...iFields],
    fieldNames,
  })
}

// 查询响应
const responseRes = computed(() => indexRequestResponse.get('response'))
const rows = computed(() => responseRes.value.rows || [])
const summaryRows = computed(() => responseRes.value.summaryRows || [])
// 查询错误原因（从errorLog 中提取中文信息）
const resError = computed(() => {
  const log = responseRes.value.errorLog || ''
  const chMsg = log.match(/^\[(.*?)\]/)

  return chMsg ? chMsg[1] || log : log
})

const queryStatus = ref('')
watch(
  responseRes,
  r => {
    queryStarted.value = !!Object.keys(r).length
    queryStatus.value = r.status

    if(r.status !== 'SUCCESS') {
      fetchReason(r.queryId)
    }
  },
  { deep: true }
)

</script>

<style lang="scss" scoped>
.render-view {
  width: 100%;
  height: 100%;
  overflow: auto;
  // min-width: 800px;
}
</style>
