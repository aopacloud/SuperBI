<template>
  <!-- 使用 multiple 模式避免选中时menu就关掉 -->
  <!-- triggerSubMenuAction="click" -->
  <a-menu :selectedKeys="selectedKeys" @click="onMenuClick">
    <!-- :popupOffset="[10, -18]" -->
    <a-sub-menu
      v-if="props.field.aggregator !== SUMMARY_DEFAULT"
      popupClassName="index-submenu-dropdown"
      key="summary"
    >
      <template #title>
        汇总方式
        <span v-if="field.aggregator" class="float-right font-help font-12">
          {{ aggregatorLabel }}
        </span>
      </template>
      <a-menu-item v-for="item in summaries" :key="item.value">
        {{ item.label }}
      </a-menu-item>

      <!-- :popupOffset="[10, -210]" -->
      <!-- style="width: 120px" -->
      <a-sub-menu
        popupClassName="index-submenu-dropdown"
        key="quantile"
        title="分位数"
      >
        <a-menu-item
          v-for="item in quantileOptions"
          :key="item.value"
          :disabled="item.value === QUANTILE_PREFIX"
        >
          <div v-if="item.value === QUANTILE_PREFIX">
            <a-input-number
              style="width: 100px"
              :placeholder="item.label"
              :min="1"
              :max="100"
              :precision="0"
              :value="customQuantileValue"
              :formatter="v => (v ? v + '分位数' : '')"
              :parser="v => v.replace('分位数', '')"
              @pressEnter.stop
              @blur="onQuantileBlur"
              @change="onQuantileChange"
            />
          </div>
          <span v-else>{{ item.label }} </span>
        </a-menu-item>
      </a-sub-menu>
    </a-sub-menu>

    <!-- :popupOffset="[10, -50]" -->
    <a-sub-menu popupClassName="index-submenu-dropdown" key="quick">
      <template #title>
        快速计算
        <span v-if="field.fastCompute" class="float-right font-help font-12">
          {{ quickLabel }}
        </span>
      </template>

      <a-menu-item v-for="item in quickCalculateOptions" :key="item.value">
        {{ item.label }}
      </a-menu-item>
    </a-sub-menu>

    <a-sub-menu
      v-if="!field.fastCompute"
      popupClassName="index-submenu-dropdown"
      key="formatter"
    >
      <template #title>
        数据格式
        <span v-if="!!formatItem" class="float-right font-help font-12">
          {{ formatLabel }}
        </span>
      </template>

      <a-menu-item v-for="item in formatterOptions" :key="item.value">
        {{
          item.value === FORMAT_CUSTOM_CODE
            ? displayCustomFormatterLabel(formatItem.config) || item.label
            : item.label
        }}
      </a-menu-item>
    </a-sub-menu>

    <a-menu-item
      key="rename"
      :class="{ 'selected-with-icon-right': field._modifyDisplayName }"
    >
      重命名
    </a-menu-item>
  </a-menu>
</template>

<script setup lang="jsx">
import { ref, computed, inject } from 'vue'
import {
  SUMMARY_DEFAULT,
  summaryOptions,
  quantileOptions,
  quickCalculateOptions,
  propertySummaryOptions,
  propertyTextSummaryOptions,
  propertyNumberSummaryOptions,
  QUANTILE_PREFIX,
  summaryQuantile,
  formatterOptions,
  FORMAT_CUSTOM_CODE
} from '@/views/dataset/config.field'
import { CATEGORY } from '@/CONST.dict.js'
import { displayCustomFormatterLabel } from '@/views/dataset/utils'

const emits = defineEmits(['update:open'])
const props = defineProps({
  dataset: { type: Object, default: () => ({}) },
  field: {
    type: Object,
    default: () => ({})
  },
  category: {
    type: String,
    default: CATEGORY.INDEX
  },
  open: {
    type: Boolean
  }
})

const contentHeaderReject = inject('contentHeader')

const { options: indexOptions } = inject('index')

const summaries = computed(() => {
  const { category } = props.field

  if (category === CATEGORY.INDEX) {
    return summaryOptions.filter(t => !t.hidden)
  } else {
    const isTextOrTime =
      props.field.dataType === 'TEXT' || props.field.dataType.includes('TIME')
    const isNumber = props.field.dataType === 'NUMBER'
    const summary = isTextOrTime
      ? propertyTextSummaryOptions
      : isNumber
        ? propertyNumberSummaryOptions
        : []

    return propertySummaryOptions.concat(summary).filter(t => !t.hidden)
  }
})

// 汇总值
const aggregatorValue = ref()
// 汇总方式label
const aggregatorLabel = computed(() => {
  const options = summaryOptions.concat(summaryQuantile)
  const item = options.find(
    item =>
      item.value === props.field.aggregator ||
      item.value.startsWith(QUANTILE_PREFIX)
  )

  return item?.label
})

// 分位数
const customQuantileValue = computed(() => {
  if (!props.field._quantile) return

  return aggregatorValue.value.split(QUANTILE_PREFIX)[1]
})
// 分位数自定义
const onQuantileBlur = e => {
  let value = parseInt(e.target.value)
  if (!value) return

  if (value < 0) value = 0
  if (value > 100) value = 100

  props.field._quantile = aggregatorValue.value = QUANTILE_PREFIX + value
}
// 分位数自定义change
const onQuantileChange = e => {
  const value = parseInt(e)
  if (!value) return

  props.field._quantile = aggregatorValue.value = QUANTILE_PREFIX + value
}

// 快速计算的值
const quickValue = ref()
// 快速计算label
const quickLabel = computed(() => {
  if (!props.field.fastCompute) return ''

  const item = quickCalculateOptions.find(
    t => t.value === props.field.fastCompute
  )
  return item.label
})

watchEffect(() => {
  const { aggregator, fastCompute } = props.field

  aggregatorValue.value = aggregator
  quickValue.value = fastCompute
})

const fieldName = computed(
  () => props.field.name + '.' + props.field.aggregator
)

// 格式化
const formatters = computed(() => indexOptions.get('formatters') || [])
const formatItem = computed(
  () => formatters.value.find(t => t.field === fieldName.value) || {}
)
// 数据格式label
const formatLabel = computed(() => {
  if (!formatItem.value) return ''

  const item = formatterOptions.find(t => t.value === formatItem.value.code)
  return item?.label
})

const selectedKeys = computed(() => {
  return [
    props.field._quantile ? QUANTILE_PREFIX : aggregatorValue.value,
    quickValue.value,
    formatItem.value?.code
  ].filter(Boolean)
})
watch(selectedKeys, () => {
  props.field.aggregator = aggregatorValue.value
  props.field.fastCompute = quickValue.value
})

const onMenuClick = item => {
  const { key, keyPath, domEvent } = item

  if (key === 'rename') {
    contentHeaderReject.fieldRename(props.field)
  } else if (keyPath.includes('summary')) {
    props.field._quantile = undefined // 自定义分位数
    aggregatorValue.value = key
  } else if (keyPath.includes('quick')) {
    quickValue.value = key ?? undefined
  } else if (keyPath.includes('formatter')) {
    if (key === 'CUSTOM') {
      emits('update:open', true)
      contentHeaderReject.onCustomFormatterShow(props.field, domEvent)
      return
    } else {
      const fieldName = props.field.name + '.' + props.field.aggregator
      const item = formatters.value.find(t => t.field === fieldName)
      if (item) {
        item.code = key
        item.config = undefined
      } else {
        indexOptions.set(
          'formatters',
          formatters.value.concat({
            field: fieldName,
            code: key
          })
        )
      }
    }
  }

  emits('update:open', false)
}
</script>

<style scoped>
.font-help {
  margin-top: 3px;
}
</style>

<style lang="scss">
.index-submenu-dropdown {
  .ant-dropdown-menu-item,
  .ant-dropdown-menu-submenu-title {
    color: inherit !important;
    padding-left: 30px !important;
  }
  .ant-dropdown-menu-item-selected,
  .ant-dropdown-menu-submenu-selected {
    background-color: initial !important;
    @extend .selected-with-icon-left;
  }
  .ant-dropdown-menu-submenu-selected {
    padding-left: 0 !important;
  }
}
</style>
