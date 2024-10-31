<template>
  <SettingSectionLayout title="图表设置">
    <a-radio-group
      class="setting-tabs"
      style="width: 100%; margin-bottom: 12px"
      v-model:value="settingTabKey"
    >
      <a-radio-button value="style" @click="scrollTo('style')">
        样式
      </a-radio-button>
      <a-radio-button value="data" @click="scrollTo('data')">
        数据
      </a-radio-button>
      <!-- <a-radio-button value="query" @click="scrollTo('query')">
        查询
      </a-radio-button> -->
    </a-radio-group>

    <a-collapse expand-icon-position="end" v-model:activeKey="activeKey">
      <a-collapse-panel
        v-if="renderType === 'bar'"
        key="barType"
        header="基础样式"
      >
        <BasicStyle v-model="modelValue.style" />
      </a-collapse-panel>

      <a-collapse-panel
        v-if="renderType !== 'pie'"
        key="labelShow"
        id="setting-chart-section--style"
        class="collapse-panel-onlyHeader"
      >
        <!-- 不生效??? -->
        <!-- <template #expandIcon="{ isActive }">{{ isActive }}</template> -->

        <template #header>
          <span>数据标签</span>
          <a-checkbox
            style="float: right"
            v-model:checked="modelValue.labelShow"
            @click.stop
            >显示
          </a-checkbox>
        </template>
      </a-collapse-panel>

      <a-collapse-panel
        key="legend"
        :collapsible="!modelValue.legend ? 'disabled' : ''"
      >
        <template #header>
          <span>图例</span>
          <a-checkbox
            style="float: right"
            :checked="!!modelValue.legend"
            @change="e => onCollapseChange(e, 'legend', { position: 'top' })"
            @click.stop
            >显示
          </a-checkbox>
        </template>

        <div class="chart-legend" v-if="modelValue.legend">
          <a-radio-group
            size="small"
            button-style="solid"
            option-type="button"
            name="legend-position"
            :options="positionTop"
            v-model:value="modelValue.legend.position"
          />
          <a-radio-group
            style="margin-top: 8px"
            size="small"
            button-style="solid"
            option-type="button"
            name="legend-position"
            :options="positionBottom"
            v-model:value="modelValue.legend.position"
          />
        </div>
      </a-collapse-panel>

      <template v-if="renderType === 'bar' || renderType === 'line'">
        <a-collapse-panel key="splited" class="collapse-panel-onlyHeader">
          <template #header>
            指标拆分
            <a-checkbox
              style="float: right"
              v-model:checked="modelValue.splited"
              @click.stop
              >开启
            </a-checkbox>
          </template>
        </a-collapse-panel>

        <a-collapse-panel
          key="xAxis"
          :collapsible="!modelValue.xAxis.show ? 'disabled' : ''"
        >
          <template #header>
            X轴
            <a-checkbox
              style="float: right"
              v-model:checked="modelValue.xAxis.show"
              @change="
                e => onCollapseChange(e, 'xAxis', defaultChartOptions.xAxis)
              "
              @click.stop
              >显示
            </a-checkbox>
          </template>

          <section v-if="modelValue.xAxis.show" class="setting-panel-section">
            <p class="setting-panel-item">
              轴名称
              <a-switch
                size="small"
                style="margin-left: 4px"
                v-model:checked="modelValue.xAxis.nameShow"
              />
            </p>

            <div class="setting-panel-item">轴标签数量</div>
            <a-radio-group
              class="setting-panel-content"
              v-model:value="modelValue.xAxis.labelCount"
            >
              <a-radio value="auto">自动</a-radio>
              <a-radio value="thin">稀疏展示</a-radio>
              <a-radio value="max">最多展示</a-radio>
            </a-radio-group>
          </section>
        </a-collapse-panel>

        <a-collapse-panel
          key="yAxis"
          :collapsible="!modelValue.yAxis.show ? 'disabled' : ''"
        >
          <template #header>
            Y轴
            <a-checkbox
              style="float: right"
              v-model:checked="modelValue.yAxis.show"
              @change="
                e =>
                  onCollapseChange(e, 'yAxis', {
                    ...defaultChartOptions.yAxis,
                    axis: modelValue.yAxis.axis
                  })
              "
              @click.stop
              >显示
            </a-checkbox>
          </template>

          <YAxisSetting
            v-if="modelValue.yAxis.show"
            :renderType="renderType"
            v-model="modelValue.yAxis"
          />
        </a-collapse-panel>
      </template>

      <a-collapse-panel
        key="compare"
        header="同环比"
        id="setting-chart-section--data"
        :collapsible="compareDisabled ? 'disabled' : ''"
      >
        <BasisRatio v-model:value="contrastModelValue" />
      </a-collapse-panel>

      <a-collapse-panel
        v-if="renderType === 'line'"
        key="special"
        header="特殊值"
      >
        <p class="setting-panel-item">折线特殊值处理</p>
        <a-radio-group v-model:value="modelValue.lineEmptyWith">
          <a-radio value="connect">折线连接</a-radio>
          <a-radio value="0">置为0</a-radio>
          <a-radio value="break">线条断开</a-radio>
        </a-radio-group>
      </a-collapse-panel>

      <a-collapse-panel v-if="renderType === 'pie'" key="pie" header="饼图样式">
        <a-radio-group
          :options="pieTypeOptions"
          v-model:value="modelValue.pieStyle"
        >
        </a-radio-group>
      </a-collapse-panel>
    </a-collapse>
  </SettingSectionLayout>
</template>

<script setup>
import { computed, shallowReactive, watch, inject, nextTick } from 'vue'
import SettingSectionLayout from './SectionLayout.vue'
import BasisRatio from './BasisRatio.vue'
import { chartLayoutPositionOptions, pieTypeOptions } from './config'
import { useVModel } from 'common/hooks/useVModel'
import BasicStyle from './BasicStyle.vue'
import YAxisSetting from './YAxisSetting.vue'
import { defaultChartOptions } from '@/views/analysis/defaultOptions'
import { deepClone } from '@/common/utils/help'

const props = defineProps({
  renderType: {
    type: String
  },
  options: {
    type: Object,
    default: () => ({})
  },
  compare: {
    type: Object,
    default: () => ({})
  }
})

const indexInject = inject('index')

const compareDisabled = computed(() => {
  const compare = indexInject.compare.get() || {}

  return compare.measures?.length ? false : true
})

const positionTop = shallowReactive(chartLayoutPositionOptions.slice(0, 3))
const positionBottom = shallowReactive(chartLayoutPositionOptions.slice(3))

const emits = defineEmits(['update:options', 'update:compare'])
const modelValue = useVModel(props, 'options', emits)
watch(
  modelValue,
  val => {
    emits('update:options', val)
  },
  { deep: true }
)
const contrastModelValue = useVModel(props, 'compare', emits)
watch(
  contrastModelValue,
  val => {
    emits('update:compare', val)
  },
  { deep: true }
)

const settingTabKey = ref('style')
const scrollTo = domId => {
  const dom = document.querySelector(`#setting-chart-section--${domId}`)
  if (dom) dom.scrollIntoView()
}

const activeKey = ref([])

const onCollapseChange = (e, key, defaultObj) => {
  // 轴
  const isAxis = key === 'xAxis' || key === 'yAxis'

  const checked = e.target.checked
  if (checked) {
    modelValue.value[key] = deepClone(defaultObj)

    if (!activeKey.value.includes(key)) activeKey.value.push(key)
  } else {
    const index = activeKey.value.indexOf(key)
    if (index > -1) activeKey.value.splice(index, 1)

    if (!isAxis) {
      // 避免组件动态卡帧
      nextTick(() => {
        modelValue.value[key] = undefined
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.chart-legend {
  text-align: center;
  .ant-radio-group {
    display: block;
  }
  :deep(.ant-radio-button-wrapper) {
    min-width: 45px;
  }
}
.setting-tabs {
  display: flex;
  :deep(.ant-radio-button-wrapper) {
    flex: 1;
    text-align: center;
  }
}
.collapse-panel-onlyHeader {
  :deep(.ant-collapse-expand-icon) {
    visibility: hidden;
  }
  :deep(.ant-collapse-content) {
    display: none;
  }
}
</style>
