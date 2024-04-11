<template>
  <aside class="aside-right">
    <div class="setting-arrow-icon" @click="handleSettingCollapse">
      <DoubleRightOutlined v-if="settingOpen" />
      <DoubleLeftOutlined v-else />
    </div>

    <div
      v-show="settingOpen"
      class="setting"
      :class="{ disabled: !hasDatasetAnalysis }">
      <SettingTypeSection :type="options.renderType" @change="onRenderTypeChange" />

      <Divider style="margin: 10px 0" />

      <template v-if="options.renderType !== 'statistic'">
        <keep-alive>
          <SettingTableSection
            v-if="isRenderTable(options.renderType)"
            v-model:options="options.table"
            v-model:compare="options.compare" />
          <SettingChartSection
            v-else
            :type="options.renderType"
            v-model:options="options.chart"
            v-model:compare="options.compare" />
        </keep-alive>
      </template>
    </div>
  </aside>
</template>

<script setup>
import { ref, watch, inject, watchEffect, computed } from 'vue'
import { Divider } from 'ant-design-vue'
import { DoubleRightOutlined, DoubleLeftOutlined } from '@ant-design/icons-vue'
import SettingTypeSection from './components/SettingTypeSection.vue'
import SettingTableSection from './components/SettingTableSection.vue'
import SettingChartSection from './components/SettingChartSection.vue'
import { isRenderTable } from '../utils'

const emits = defineEmits(['update:options'])
const props = defineProps({
  options: {
    type: Object,
    default: () => ({}),
  },
  hasDatasetAnalysis: Boolean,
})

const { autoRun, requestResponse } = inject('index')

// 设置侧边栏显示
const settingOpen = ref(true)
const handleSettingCollapse = () => {
  settingOpen.value = !settingOpen.value
}

// 展示类型
const renderType = ref('table')

// 查询请求
const request = computed(() => requestResponse.get('request'))

// 查询结果
const response = computed(() => requestResponse.get('response'))

const onRenderTypeChange = e => {
  props.options.renderType = e
  if (e === 'bar' || e === 'line') {
    ;(props.options.chart.axis || []).forEach(item => {
      item.chartType = e
    })
  }

  // 上一次查询结果未请求成功
  if (response.value.status !== 'SUCCESS') return

  // 上一次查询请求是否汇总
  const prevRequestIsSummery = request.value.summary
  // 当前配置是否显示汇总行
  const currentIsSummary = props.options.table.showSummary

  if (isRenderTable(e)) {
    if (e === 'table') {
      // 普通表格的不显示汇总行，不重新查询
      if (!currentIsSummary) return
      // 上一次查询了汇总
      if (prevRequestIsSummery) return

      autoRun()
    } else {
      if (prevRequestIsSummery) return

      autoRun()
    }
  }
}
</script>

<style lang="scss" scoped>
.aside-right {
  position: relative;
  border-left: 1px solid #e8e8e8;
}
.setting-arrow-icon {
  position: absolute;
  left: -16px;
  top: 50%;
  transform: translateY(-50%);
  height: 50px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border: 1px solid #e8e8e8;
  border-right-color: #fff;
  border-radius: 4px 0 0 4px;
  cursor: pointer;
  z-index: 99;
  &:hover {
    background-color: #f9f9f9;
    border-right-color: #f9f9f9;
  }
}
.setting {
  width: 300px;
  // height: 100%;
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
