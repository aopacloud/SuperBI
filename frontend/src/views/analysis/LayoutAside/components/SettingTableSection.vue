<template>
  <SettingSectionLayout class="table-section" title="表格设置">
    <a-collapse expand-icon-position="end">
      <a-collapse-panel header="列宽" key="layout">
        <a-radio-group v-model:value="modelValue.layout" :options="tableLayoutOptions">
        </a-radio-group>
      </a-collapse-panel>

      <a-collapse-panel header="文字位置" key="align">
        <a-radio-group v-model:value="modelValue.align" :options="tableAlignOptions" />
      </a-collapse-panel>

      <a-collapse-panel header="冻结" key="fixed">
        <p class="panel-title">行冻结</p>
        <a-radio disabled checked>行冻结</a-radio>
        <p class="panel-title">列冻结</p>
        <a-radio-group
          v-model:value="modelValue.fixed.columnMode"
          @change="onFixedModeChange">
          <a-radio :value="0">无</a-radio>
          <a-radio :value="1">首列冻结</a-radio>
          <a-radio :value="2">自定义</a-radio>
        </a-radio-group>
        <p
          class="panel-title"
          v-if="modelValue.fixed.columnMode === 2"
          style="display: flex">
          前
          <a-input-number
            size="small"
            :min="0"
            :precision="0"
            v-model:value="modelValue.fixed.columnSpan[0]"
            style="width: 65px; margin: 0 6px" />列，后
          <a-input-number
            size="small"
            :min="0"
            :precision="0"
            v-model:value="modelValue.fixed.columnSpan[1]"
            style="width: 65px; margin: 0 6px" />列
        </p>
      </a-collapse-panel>

      <a-collapse-panel header="汇总行">
        <a-checkbox v-model:checked="modelValue.showSummary">显示</a-checkbox>
      </a-collapse-panel>

      <a-collapse-panel
        header="同环比"
        key="compare"
        :collapsible="compareDisabled ? 'disabled' : ''">
        <BasisRatio
          chartType="table"
          :disabled="compareDisabled"
          v-model:value="contrastModelValue" />
      </a-collapse-panel>
    </a-collapse>
  </SettingSectionLayout>
</template>
<script setup>
import { computed, watch, inject } from 'vue'
import SettingSectionLayout from './SettingSectionLayout.vue'
import BasisRatio from './BasisRatio.vue'
import { tableLayoutOptions, tableAlignOptions } from '../config.js'
import { useVModel } from 'common/hooks/useVModel'

const props = defineProps({
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

// 列冻结模式改变
const onFixedModeChange = e => {
  const value = e.target.value

  if (value === 0) {
    modelValue.value.fixed.columnSpan = [0, 0]
  } else if (value === 1) {
    modelValue.value.fixed.columnSpan = [1, 0]
  } else if (value === 2) {
    modelValue.value.fixed.columnSpan = [0, 0]
  }
}

const emits = defineEmits(['update:options', 'update:compare'])
const modelValue = useVModel(props, 'options', emits)
const contrastModelValue = useVModel(props, 'compare', emits)

watch(
  modelValue,
  val => {
    emits('update:options', val)
  },
  { deep: true }
)

watch(
  contrastModelValue,
  val => {
    emits('update:compare', val)
  },
  { deep: true }
)
</script>

<style lang="scss" scoped>
.panel-title {
  margin-bottom: 6px;
  &:first-child {
    margin: 0 0 6px;
  }
}
</style>
