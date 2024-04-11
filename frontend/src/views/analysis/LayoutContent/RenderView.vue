<template>
  <div class="render-view">
    <!-- 初始化 -->
    <div v-if="!dataset.id" class="flex flex-column flex-center">
      <a-spin />
    </div>

    <!-- 无权限 -->
    <UnAccess v-else-if="!hasDatasetAnalysis" :dataset="dataset" />

    <!-- 未查询 -->
    <a-empty v-else-if="!queryStarted" class="flex flex-column flex-center">
      <template #description>
        <div style="color: #999">请拖入/更改分析字段</div>
      </template>
    </a-empty>

    <!-- 查询异常 -->
    <div v-else-if="queryStatus === 'FAILED'" class="flex flex-column flex-center">
      <img src="@/assets/svg/chartBox_error.svg" style="width: 200px" />
      <p style="color: #999">查询异常</p>
    </div>

    <!-- 无数据 -->
    <div
      v-else-if="options.renderType !== 'table' && !rows.length"
      class="flex flex-column flex-center">
      <img src="@/assets/svg/chartBox_empty.svg" style="width: 200px" />
      <p style="color: #999">当前查询条件下暂无数据</p>
    </div>

    <!-- 查询成功 -->
    <Chart
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

const props = defineProps({
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

const queryStatus = ref('')
watch(
  responseRes,
  r => {
    queryStarted.value = !!Object.keys(r).length
    queryStatus.value = r.status
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
