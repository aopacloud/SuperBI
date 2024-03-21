<template>
  <aside class="aside-right">
    <div class="setting-arrow-icon" @click="handleSettingCollapse">
      <DoubleRightOutlined v-if="settingOpen" />
      <DoubleLeftOutlined v-else />
    </div>

    <div v-show="settingOpen" class="setting" :class="{ disabled: !hasDatasetAnalysis }">
      <SettingTypeSection
        v-model:type="options.renderType"
        @change="onRenderTypeChange" />

      <Divider style="margin: 10px 0" />

      <template v-if="options.renderType !== 'statistic'">
        <keep-alive>
          <SettingTableSection
            v-if="
              ['table', 'groupTable', 'intersectionTable'].includes(options.renderType)
            "
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
import { ref, watch, watchEffect } from 'vue'
import { Divider } from 'ant-design-vue'
import { DoubleRightOutlined, DoubleLeftOutlined } from '@ant-design/icons-vue'
import SettingTypeSection from './components/SettingTypeSection.vue'
import SettingTableSection from './components/SettingTableSection.vue'
import SettingChartSection from './components/SettingChartSection.vue'

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
  settingOpen.value = !settingOpen.value
}

// 展示类型
const renderType = ref('table')

const onRenderTypeChange = e => {
  if (e === 'bar' || e === 'line') {
    ;(props.options.chart.axis || []).forEach(item => {
      item.chartType = e
    })
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
