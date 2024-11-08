<template>
  <aside class="aside-right setting" :class="{ disabled: !hasDatasetAnalysis }">
    <SettingTypeSection
      style="min-height: 120px"
      :type="renderType"
      @change="onRenderTypeChange"
    />

    <Divider style="margin: 10px 0" />

    <keep-alive>
      <SettingTableSection
        v-if="isRenderTable(renderType)"
        :renderType="renderType"
        v-model:options="options.table"
        v-model:compare="options.compare"
      />

      <SettingIndexCardSection
        v-else-if="renderType === 'statistic'"
        v-model:options="options.indexCard"
        v-model:compare="options.compare"
      />

      <SettingChartSection
        v-else
        :renderType="renderType"
        v-model:options="options.chart"
        v-model:compare="options.compare"
      />
    </keep-alive>
  </aside>
</template>

<script setup>
import { inject, computed } from 'vue'
import { Divider } from 'ant-design-vue'
import SettingTypeSection from './components/SettingSections/TypeSection.vue'
import SettingTableSection from './components/SettingSections/TableSection.vue'
import SettingChartSection from './components/SettingSections/ChartSection.vue'
import SettingIndexCardSection from './components/SettingSections/IndexCardSection.vue'
import { isRenderTable } from '../utils'
import { CATEGORY } from '@/CONST.dict'

const emits = defineEmits(['update:options'])
const props = defineProps({
  options: {
    type: Object,
    default: () => ({})
  },
  hasDatasetAnalysis: Boolean
})

const renderType = computed(() => props.options.renderType)

const { choosed: indexChoosed, autoRun, requestResponse } = inject('index')

// 查询请求
const request = computed(() => requestResponse.get('request') || {})

// 查询结果
const response = computed(() => requestResponse.get('response') || {})

// 汇总处理
const summaryHandler = e => {
  // 上一次查询结果未请求成功
  if (response.value.status !== 'SUCCESS') return

  // 上一次查询请求是否汇总
  const prevRequestIsSummery = request.value.summary
  // 上一次查询详情汇总
  const prevRequestIsDetailSummary = request.value.detailSummary
  // 当前配置是否显示汇总行
  const currentIsSummary = !!props.options.table.summary

  const oldE = props.options.renderType

  // 交叉表会有数据个数的变化，切换至别的需重新查询
  if (oldE === 'intersectionTable') {
    autoRun()
    return
  }

  // 上一次查询不查询汇总行和明细汇总
  if (!prevRequestIsSummery && !prevRequestIsDetailSummary) {
    if (oldE === 'table') {
      autoRun()
    } else {
      // 设置中没有汇总行
      if (!currentIsSummary) return
      autoRun()
    }
  } else {
    if (oldE !== 'table') return
    autoRun()
  }
}

// 更新维度
const updateDimensions = e => {
  const list = indexChoosed.get(CATEGORY.PROPERTY)

  indexChoosed.set(
    CATEGORY.PROPERTY,
    list.map(t => (delete t._group, t))
  )
}

const onRenderTypeChange = e => {
  updateDimensions(e)

  if (isRenderTable(e) || e === 'statistic') {
    updateDimensions(e)
    // summaryHandler(e)
    autoRun()
  } else {
    if (e === 'bar' || e === 'line') {
      ;(props.options.chart.yAxis.axis || []).forEach(item => {
        item.chartType = e
      })
    }
  }

  props.options.renderType = e
}
</script>

<style lang="scss" scoped>
.aside-right {
  position: relative;
  border-left: 1px solid #e8e8e8;
}
.setting {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: auto;
  &.disabled {
    background-color: rgba(211, 211, 211, 0.3);
    cursor: not-allowed;
    & > * {
      pointer-events: none;
    }
  }
  :deep(.ant-collapse-item) {
    .ant-collapse-header {
      padding: 9px 12px;
    }
    .ant-collapse-content-box {
      padding: 12px;
    }
  }
}
</style>

<style lang="scss">
$marginBottom: 4px;

.setting-panel-section {
  margin-bottom: 0;
  & ~ & {
    margin-top: $marginBottom * 6;
  }

  .setting-panel-item {
    display: flex;
    align-items: center;
    margin: 0 0 $marginBottom * 2;
    &:last-child {
      margin: 0;
    }
    & > .setting-panel-content,
    & + .setting-panel-content {
      margin-left: $marginBottom * 5;
    }
  }
}
</style>
