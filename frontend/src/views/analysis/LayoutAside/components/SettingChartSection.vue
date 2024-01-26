<template>
  <SettingSectionLayout title="图表设置">
    <a-collapse expand-icon-position="end">
      <a-collapse-panel v-if="type !== 'pie'" key="labelShow" header="数值显示">
        <a-radio-group v-model:value="modelValue.labelShow">
          <a-radio :value="true">是</a-radio>
          <a-radio :value="false">否</a-radio>
        </a-radio-group>
      </a-collapse-panel>

      <a-collapse-panel key="legendPosition" header="图例位置">
        <div class="chart-legend">
          <a-radio-group
            size="small"
            button-style="solid"
            option-type="button"
            name="legend-position"
            :options="positionTop"
            v-model:value="modelValue.legend.position"></a-radio-group>
          <a-radio-group
            style="margin-top: 8px"
            size="small"
            button-style="solid"
            option-type="button"
            name="legend-position"
            :options="positionBottom"
            v-model:value="modelValue.legend.position"></a-radio-group>
        </div>
      </a-collapse-panel>

      <a-collapse-panel
        key="compare"
        header="同环比"
        :collapsible="compareDisabled ? 'disabled' : ''">
        <BasisRatio v-model:value="contrastModelValue" />
      </a-collapse-panel>

      <a-collapse-panel v-if="type === 'pie'" key="pie" header="饼图样式">
        <a-radio-group :options="pieTypeOptions" v-model:value="modelValue.pieStyle">
        </a-radio-group>
      </a-collapse-panel>

      <template v-else>
        <a-collapse-panel key="splited" header="指标聚合">
          <a-radio-group v-model:value="modelValue.splited">
            <a-radio :value="true">拆分</a-radio>
            <a-radio :value="false">聚合</a-radio>
          </a-radio-group>
        </a-collapse-panel>

        <a-collapse-panel key="axis" header="坐标轴设置">
          <ChartAxis :data-source="modelValue.axis" />
        </a-collapse-panel>
      </template>
    </a-collapse>
  </SettingSectionLayout>
</template>

<script setup>
import { computed, shallowReactive, watch, inject } from 'vue'
import SettingSectionLayout from './SettingSectionLayout.vue'
import BasisRatio from './BasisRatio.vue'
import ChartAxis from './ChartAxis.vue'
import { chartLayoutPositionOptions, pieTypeOptions } from '../config'
import { useVModel } from 'common/hooks/useVModel'

const props = defineProps({
  type: {
    type: String,
  },
  options: {
    type: Object,
    default: () => ({}),
  },
  compare: {
    type: Object,
    default: () => ({}),
  },
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
</script>

<style lang="scss" scoped>
.chart-legend {
  text-align: center;
  :deep(.ant-radio-button-wrapper) {
    min-width: 45px;
  }
}
</style>
