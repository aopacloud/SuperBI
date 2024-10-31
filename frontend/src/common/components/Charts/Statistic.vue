<template>
  <div class="statistic">
    <div class="title" v-if="labelShow">
      <slot name="title">{{ title }}</slot>
    </div>
    <div class="sub-title" v-if="subTitle || $slots['sub-title']">
      <slot name="sub-title"> {{ subTitle }}</slot>
    </div>
    <div class="value">
      <slot>{{ formatFieldDisplay(value) }}</slot>
    </div>

    <div v-if="hasCompare" class="compare-wrapper">
      <div class="compare-content">
        <div
          class="compare-item"
          v-for="(item, index) in compareFields"
          :key="item.renderName"
        >
          <div class="item-label">
            {{ getRatioLabel(item) }}
          </div>
          <div
            class="item-value"
            :style="{ color: getDiffColor(compareData[item._index]) }"
          >
            {{ displayDiffValue(compareData[item._index]) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ratioOptions } from '@/views/analysis/config'
import { VS_FIELD_SUFFIX } from '@/components/Chart/utils/index'
import { DEFAULT_RATIO_TYPE } from '@/views/analysis/config'
import { COMPARE } from '@/CONST.dict'
import { getCompareDisplay } from '@/components/Chart/Table/utils'
import { formatFieldDisplay as _formatFieldDisplay } from '@/components/Chart/utils/index'

const props = defineProps({
  field: {
    type: Object,
    default: () => ({})
  },
  subTitle: {
    type: String
  },
  value: {
    type: [String, Number],
    default: '-'
  },
  fields: {
    type: Array,
    default: () => []
  },
  row: {
    type: Array,
    default: () => []
  },
  compare: {
    type: Object
  },
  options: {
    type: Object
  },
  formatters: {
    type: Array,
    default: () => []
  }
})

const title = computed(() => props.field.displayName)

const getOrderIndx = v =>
  [
    DEFAULT_RATIO_TYPE,
    'WEEK_ON_WEEK',
    'MONTH_ON_MONTH',
    'YEAR_ON_YEAR'
  ].indexOf(v)

const sortFn = (a, b) => {
  const ai = getOrderIndx(a.ratioType),
    bi = getOrderIndx(b.ratioType)
  return ai - bi
}

const getRatioOpt = ({ renderName = '' }) => {
  const str = renderName.split(VS_FIELD_SUFFIX)[1]
  if (!str) return renderName

  const [ratioType, period] = str.split('.')

  return {
    ratioType,
    period
  }
}

const hasCompare = computed(() => props.compare?.measures?.length)

const labelShow = computed(() => props.options.labelShow)

// 同环比配置
const compareOption = computed(() => props.options?.compare || {})
// 数据集
const dataset = computed(() => props.options.dataset)
// 数据集字段
const datasetFields = computed(() => dataset.value?.fields || [])
// 同环比数据
const compareData = computed(() => props.row.slice(1))
// 同环比数据对应的字段
const compareFields = computed(() => {
  const list = props.fields.slice(1)
  return list
    .map((t, i) => {
      const { ratioType, period } = getRatioOpt(t)
      return {
        ...t,
        _index: i,
        ratioType,
        period
      }
    })
    .sort(sortFn)
})

const formatFieldDisplay = (value = props.value) => {
  const { field, formatters } = props
  return _formatFieldDisplay(value, field, datasetFields.value, formatters)
}

const getRatioLabel = item => {
  const { ratioType } = getRatioOpt(item)

  return ratioOptions.find(i => i.value === ratioType)?.label
}

const displayDiffValue = origin => {
  return getCompareDisplay({
    origin,
    target: props.value,
    mode: compareOption.value.mode
  })(props.field, datasetFields.value)
}

const getDiffColor = origin => {
  const target = props.value

  if (target === origin) {
    return ''
  } else {
    const color = target > origin ? 'green' : 'red'
    if (target / origin === Infinity) {
      return compareOption.value.mode === COMPARE.MODE.DIFF_PERSENT ? '' : color
    } else {
      return color
    }
  }
}
</script>

<style lang="scss" scoped>
.statistic {
  width: 100%;
  height: 100%;
  display: inline-flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 90px;
  min-width: 160px;
  .title {
    font-size: 16px;
    color: #666;
  }
  .sub-title {
    font-size: 14px;
    margin-bottom: 2px;
    color: #999;
  }
  .value {
    font-size: 30px;
    font-weight: 600;
    color: #333;
  }
}

.compare-wrapper {
  margin-top: 6px;
}
.compare-content {
  display: grid;
  grid-template-columns: 1fr 1fr; // 两列
  gap: 2px;
  font-size: 12px;
}
.compare-item {
  display: flex;
  align-items: center;
  &:only-child {
    grid-column: 1 / span 2; // 跨越两列，独占一行
  }
  &:nth-of-type(2n) {
    margin-left: 20px;
  }
}
.item-label {
  width: 50px;
  // text-align: right;
}
.item-value {
  margin-left: 12px;
  font-size: 16px;
}
</style>
