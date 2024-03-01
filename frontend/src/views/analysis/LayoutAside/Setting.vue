<template>
  <aside class="aside-right">
    <div class="setting-arrow-icon" @click="handleSettingCollapse">
      <DoubleRightOutlined v-if="settingOpen" />
      <DoubleLeftOutlined v-else />
    </div>

    <div v-show="settingOpen" class="setting" :class="{ disabled: !hasDatasetAnalysis }">
      <SettingTypeSection v-model:type="renderType" @change="onRenderTypeChange" />

      <Divider style="margin: 10px 0" />

      <template v-if="renderType !== 'statistic'">
        <keep-alive>
          <SettingTableSection
            v-if="renderType === 'table'"
            v-model:options="tableOption"
            v-model:compare="compareOption" />
          <SettingChartSection
            v-else
            :type="renderType"
            v-model:options="chartOption"
            v-model:compare="compareOption" />
        </keep-alive>
      </template>
    </div>
  </aside>
</template>

<script setup>
import { ref, watch, watchEffect } from 'vue'
import { Divider } from 'ant-design-vue'
import { DoubleRightOutlined, DoubleLeftOutlined } from '@ant-design/icons-vue'
import SettingTypeSection from './components/SettingTypeSection.vue'
import SettingTableSection from './components/SettingTableSection.vue'
import SettingChartSection from './components/SettingChartSection.vue'
import {
  defaultRenderType,
  defaultTableOptions,
  defaultQueryTotal,
  defaultChartOptions,
  defaultCompareOptions,
} from '../defaultOptions.js'

const emits = defineEmits(['update:options'])
const props = defineProps({
  options: {
    type: Object,
    default: () => ({}),
  },
  hasDatasetAnalysis: Boolean,
})

// 设置侧边栏显示
const settingOpen = ref(true)
const handleSettingCollapse = () => {
  const oepn = settingOpen.value

  settingOpen.value = !oepn
}

// 展示类型
const renderType = ref('table')
// 表格配置
const tableOption = ref({})
// 图表配置
const chartOption = ref({})
// 同环比配置
const compareOption = ref({})

watchEffect(() => {
  const {
    renderType: type = defaultRenderType,
    table = defaultTableOptions,
    chart = defaultChartOptions,
    compare = defaultCompareOptions,
  } = props.options

  renderType.value = type
  compareOption.value = compare
  tableOption.value = table
  chartOption.value = chart
})

const onRenderTypeChange = e => {
  if (e === 'bar' || e === 'line') {
    ;(chartOption.value.axis || []).forEach(item => {
      item.chartType = e
    })
  }
}

watch(
  [renderType, compareOption, tableOption, chartOption],
  ([type, compare, table, chart]) => {
    emits('update:options', {
      renderType: type,
      compare,
      table,
      chart,
    })
  }
)
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
