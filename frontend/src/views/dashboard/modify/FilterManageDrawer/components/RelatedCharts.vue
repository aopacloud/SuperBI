<template>
  <section class="section">
    <header class="header">
      <h3 style="margin: 0">关联表及字段</h3>
      <a @click="reset">清空选择字段</a>
    </header>

    <a-empty v-if="typeof item.id === 'undefined'" />

    <template v-else>
      <header class="header sub-header">
        <a-checkbox
          :checked="allChecked"
          :indeterminate="indeterminate"
          @change="onAllcheckedChange"
          >全选</a-checkbox
        >
        <a-checkbox v-model:checked="item.chartIntellect">智能选入</a-checkbox>
      </header>

      <main class="main">
        <ul class="list">
          <li v-for="chart in chartList" class="item">
            <div class="item-name">
              <a-checkbox
                :title="chart.name"
                :checked="selectedChartIds.includes(chart.id)"
                @change="e => onChartChange(e, chart)"
                >{{ chart.name }}
              </a-checkbox>
            </div>

            <a-select
              v-if="selectedChartIds.includes(chart.id)"
              size="small"
              style="width: 120px"
              placeholder="请选择字段"
              allow-clear
              show-search
              :filterOption="filterOption"
              :value="modelValue[chart.id]['fieldName']"
              @change="e => onFieldChange(e, chart)">
              <a-select-option v-for="field in chart.datasetFields" :key="field.name">
                {{ field.displayName }}
              </a-select-option>
            </a-select>
          </li>
        </ul>
      </main>
    </template>
  </section>
</template>

<script setup>
import { computed, watchPostEffect } from 'vue'
import { CATEGORY } from '@/CONST.dict.js'

const props = defineProps({
  item: {
    type: Object,
    default: () => ({}),
  },
  charts: {
    type: Array,
    default: () => [],
  },
})

const chartList = computed(() => {
  return props.charts.map(chart => {
    const { id, name, dataset } = chart
    return {
      id,
      name,
      datasetId: dataset.id,
      datasetFields: dataset.fields.filter(
        t => t.category === CATEGORY.PROPERTY && t.status === 'VIEW'
      ),
    }
  })
})

// const modelValue = computed(() => props.item.charts || {})

const modelValue = ref({})
watchPostEffect(() => {
  modelValue.value = props.item.charts || {}
})

const selectedChartIds = computed(() => {
  return Object.keys(modelValue.value).map(id => +id)
})
const allChecked = computed(() => {
  return (
    chartList.value.length > 0 && chartList.value.length === selectedChartIds.value.length
  )
})
const indeterminate = computed(() => {
  return (
    chartList.value.length > 0 &&
    selectedChartIds.value.length > 0 &&
    chartList.value.length !== selectedChartIds.value.length
  )
})

const filterOption = (input, option) => {
  const label = option.children()[0].children
  const value = option.value

  return (
    label.toLowerCase().indexOf(input.toLowerCase()) >= 0 ||
    value.toLowerCase().indexOf(input.toLowerCase()) >= 0
  )
}

const reset = () => {
  props.item.charts = {}
}

// 全选\取消反选
const onAllcheckedChange = e => {
  const checked = e.target.checked

  if (checked) {
    props.item.charts = chartList.value.reduce((acc, chart) => {
      const { id, datasetId } = chart

      acc[id] = { datasetId }
      return acc
    }, {})
  } else {
    props.item.charts = {}
  }
}

// 图表选中
const onChartChange = (e, chart) => {
  const checked = e.target.checked
  const { id, datasetId } = chart

  if (checked) {
    modelValue.value[id] = { datasetId }
  } else {
    delete modelValue.value[id]
  }
}

// 字段改变
const onFieldChange = (e, chart) => {
  if (props.item.chartIntellect) {
    Object.keys(modelValue.value).forEach(id => {
      const item = modelValue.value[id]

      if (item.datasetId === chart.datasetId) {
        item.fieldName = e
      }
    })
  } else {
    modelValue.value[chart.id]['fieldName'] = e
  }
}
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
  overflow: auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.sub-header {
  margin-bottom: 10px;
}
.main {
  flex: 1;
  overflow: auto;
}
.list {
  margin: 0;
}
.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  line-height: 24px;
  margin-bottom: 4px;
  &-name {
    flex: 1;
    margin-right: 10px;
    overflow: hidden;

    :deep(.ant-checkbox-wrapper) {
      max-width: 100%;
      .ant-checkbox + span {
        @extend .ellipsis;
        flex: 1;
      }
    }
  }
}
</style>
