<template>
  <section class="content">
    <!-- 字段拖入容器 -->
    <ContentHeader :dimensions="dimensions" :indexes="indexes" :filters="filters" />

    <!-- 看板过滤项 -->
    <DashboardFtilers style="margin-bottom: 10px" />

    <!-- 工具栏 -->
    <Tools
      ref="toolsRef"
      style="margin-bottom: 10px; border-bottom: 1px solid #eee"
      @reset="onReset"
      @topN="onTopN"
      @download="onDownload"
      @basisRatio="onBasisRatio"
      @toggle-history="handleToggleHistory"
      @run-loading="onRunLoading"
      @querySuccess="onQuerySuccess" />

    <main class="main">
      <a-spin :spinning="runLoading">
        <keep-alive>
          <RenderView
            v-if="viewComponent === 'render'"
            :dataset="dataset"
            :choosed="choosed"
            :compare="compare"
            :options="options" />

          <HistoryView v-else @revert="onHistoryQuery" />
        </keep-alive>
      </a-spin>
    </main>
  </section>

  <!-- TopN -->
  <TopNModal
    v-model:open="topNModalOpen"
    :data-source="indexes"
    :value="topN"
    @ok="onTopNOk"
    @close="onTopNClose" />

  <!-- 同环比 -->
  <BasisRatioModal
    v-model:open="basisRatioModalOpen"
    :data-source="indexes"
    v-model:type="compare.type"
    :target="dimensions.find(t => t.name === compare.timeField || toContrastFiled(t))"
    :value="compare.measures"
    @ok="onBasisRatioOk"
    @close="onBasisRatioClose" />

  <!-- 下载 -->
  <DownloadModal
    v-model:open="downloadModalOpen"
    :filename="chart.id ? chart.name : dataset.name"
    :initParams="requestResponse.request" />
</template>

<script setup>
import { ref, computed, inject, nextTick } from 'vue'
import ContentHeader from './ContentHeader.vue'
import DashboardFtilers from './DashboardFtilers.vue'
import Tools from './Tools.vue'
import RenderView from './RenderView.vue'
import HistoryView from './History.vue'
import TopNModal from '@/views/analysis/components/TopNModal.vue'
import BasisRatioModal from '@/views/analysis/components/BasisRatioModal.vue'
import DownloadModal from '@/components/DownloadModal/index.vue'
import { toContrastFiled } from '@/views/analysis/config'

const props = defineProps({
  chart: {
    type: Object,
    default: () => ({}),
  },
  dataset: {
    type: Object,
    default: () => ({}),
  },
  dimensions: {
    type: Array,
    default: () => [],
  },
  indexes: {
    type: Array,
    default: () => [],
  },
  filters: {
    type: Array,
    default: () => [],
  },
  options: {
    type: Object,
    default: () => ({}),
  },
})

const {
  compare: indexCompare,
  choosed: indexChoosed,
  topN: indexTopN,
  requestResponse: indexRequestResponse,
  recovert: indexRecovert,
  reset: indexReset,
} = inject('index', {})
// 选中的
const choosed = computed(() => indexChoosed.get())
// 查询响应
const requestResponse = computed(() => indexRequestResponse.get())

// 查询loading
const runLoading = ref(false)
const onRunLoading = running => {
  runLoading.value = running
}

const onQuerySuccess = e => {
  indexRequestResponse.set({ ...e })
}

// topN
const topNModalOpen = ref(false)
const topN = computed(() => indexTopN.get() || {})
const onTopN = () => {
  topNModalOpen.value = true
}
const onTopNOk = e => {
  indexTopN.set(e)
  toolsRef.value.run()
}
const onTopNClose = () => {
  indexTopN.set()
  toolsRef.value.run()
}

// 同环比
const compare = computed(() => indexCompare.get() || {})
const basisRatioModalOpen = ref(false)
const onBasisRatio = () => {
  basisRatioModalOpen.value = true
}
const onBasisRatioOk = ({ target = {}, fields = [], type } = {}) => {
  const paylaod = {
    type,
    timeField: target.name,
    measures: fields,
  }

  indexCompare.set(paylaod)
  toolsRef.value.run()
}
const onBasisRatioClose = () => {
  indexCompare.set()
  toolsRef.value.run()
}

// 清空
const onReset = () => {
  indexReset()
}

// 下载
const downloadModalOpen = ref(false)
const onDownload = () => {
  downloadModalOpen.value = true
}

// 历史记录
const viewComponent = ref('render')
const handleToggleHistory = mode => {
  viewComponent.value = mode ? 'history' : 'render'
}
const toolsRef = ref(null)
const onHistoryQuery = row => {
  indexRecovert(row.queryParam)

  nextTick(() => {
    toolsRef.value?.run(row._from)
  })
}

const toolsRun = () => {
  toolsRef.value?.run()
}

defineExpose({
  toolsRun,
})
</script>

<style lang="scss" scoped>
.content {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 100%;
  padding-right: 7px;
  height: 100%;

  .main {
    flex: 1;
    padding: 10px;
    overflow: hidden;
  }
}
.main {
  :deep(.ant-spin-nested-loading) {
    height: 100%;
    .ant-spin-container {
      height: inherit;
    }
  }
}
</style>
