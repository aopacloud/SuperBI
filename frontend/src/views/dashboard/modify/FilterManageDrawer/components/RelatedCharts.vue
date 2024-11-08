<template>
  <section class="section">
    <header class="header">
      <h3 style="margin: 0">关联表及字段</h3>
      <a :disabled="disabled" @click="reset">清空选择字段</a>
    </header>

    <header class="header sub-header">
      <a-checkbox
        :disabled="disabled"
        :checked="allChecked"
        :indeterminate="indeterminate"
        @change="onAllcheckedChange"
        >全选</a-checkbox
      >
      <a-checkbox :disabled="disabled" v-model:checked="item.chartIntellect"
        >智能选入</a-checkbox
      >
    </header>

    <main class="main">
      <ul class="list">
        <li v-for="chart in chartList" class="item">
          <div class="item-name">
            <a-checkbox
              :title="chart.name"
              :disabled="disabled"
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
            :disabled="disabled"
            :filterOption="filterOption"
            :value="modelValue[chart.id]['fieldName']"
            @change="e => onFieldChange(e, chart)"
          >
            <a-select-option
              v-for="field in chart.datasetFields"
              :key="field.name"
            >
              {{ field.displayName }}
            </a-select-option>
          </a-select>
        </li>
      </ul>
    </main>
  </section>
</template>

<script setup>
import { computed, watchPostEffect } from 'vue'
import { message } from 'ant-design-vue'
import { CATEGORY } from '@/CONST.dict.js'

const props = defineProps({
  item: {
    type: Object,
    default: () => ({})
  },
  list: {
    type: Array,
    default: () => []
  },
  charts: {
    type: Array,
    default: () => []
  }
})

const disabled = computed(() => !props.item.id)

const chartList = computed(() => {
  return props.charts.map(chart => {
    const { id, name, dataset } = chart
    return {
      id,
      name,
      datasetId: dataset.id,
      datasetFields: dataset.fields.filter(
        t => t.category === CATEGORY.PROPERTY && t.status === 'VIEW'
      )
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
    chartList.value.length > 0 &&
    chartList.value.length === selectedChartIds.value.length
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

const validate = () => {
  // 校验关联图表
  const checkedRelationCharts = (charts = {}) => {
    const result = { ...charts }
    for (const cId in charts) {
      const f = charts[cId]
      const originChart = props.charts.find(c => c.datasetId === f.datasetId)

      if (!originChart) delete result[cId]
    }

    return result
  }

  const validateFn = (item = {}) => {
    const itemCharts = checkedRelationCharts(item.charts)
    const chartKeys = Object.keys(itemCharts)
    if (!chartKeys.length) {
      message.warn('查询条件必须关键图表')
      return false
    }

    if (
      chartKeys.some(key => {
        const chart = itemCharts[key]
        return typeof chart.fieldName === 'undefined'
      })
    ) {
      message.warn('关联图表必须选择筛选项')

      return false
    }

    // 关联字段的源字段
    const fields = chartKeys.map(key => {
      const { datasetId, fieldName } = itemCharts[key]
      const originChart = props.charts.find(
        chart => chart.datasetId === datasetId
      )
      const fields = originChart.dataset.fields
      const field = fields.find(f => f.name === fieldName)

      return { ...field, datasetId }
    })
    // 字段的所有类型
    const fieldTypes = fields.map(t => t.dataType)
    // 有日期字段
    const hasDate = fieldTypes.some(t => t.includes('TIME'))
    const hasOther = fieldTypes.some(t => !t.includes('TIME'))
    if (hasDate && hasOther) {
      message.warn('多个图表的关联字段类型不一致，请修改后再保存')

      return false
    }

    return true
  }

  if (props.list.every(validateFn)) {
    return true
  } else {
    return false
  }
}
defineExpose({
  validate
})
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
