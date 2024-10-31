<template>
  <a-space-compact block size="small" class="summary-item">
    <ExtendASelect
      style="flex: 1; margin-right: -1px"
      mode="multiple"
      showArrow
      allSelectedLabel="所有指标"
      placeholder="请选择指标"
      showTooltip
      :options="fields"
      v-model:value="item.name"
      @change="onSummaryChange"
      @dropdownVisibleChange="onDropdownVisibleChange"
    >
    </ExtendASelect>

    <AggregatorSelector
      style="width: 95px"
      v-model:value="item.aggregator"
      v-model:customValue="item._aggregator"
    />

    <slot name="action"> </slot>
  </a-space-compact>
</template>

<script setup>
import { message } from 'ant-design-vue'
import { ExtendASelect } from 'common/components/ExtendSelect'
import AggregatorSelector from '@/components/AggregatorSelector/index.vue'

const props = defineProps({
  item: {
    type: Object,
    default: () => ({})
  },
  fields: {
    type: Array,
    default: () => []
  },
  // 已经选择的指标名
  selectedNames: {
    type: Array,
    default: () => []
  },
  onlyOne: Boolean // 仅有一个
})

const onSummaryChange = e => {
  props.item._all = e.length && e.length === props.fields.length
}

const onDropdownVisibleChange = e => {
  if (e) return

  const names = props.selectedNames
    .filter(Boolean)
    .reduce((acc, cur) => acc.concat(cur), [])
  const uniqueNames = [...new Set(names)]

  if (names.length !== uniqueNames.length) {
    message.warn('同一指标不能有多个汇总配置')
  }
}
</script>

<style lang="scss" scoped></style>
