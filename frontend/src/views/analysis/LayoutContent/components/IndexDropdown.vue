<template>
  <!-- 使用 multiple 模式避免选中时menu就关掉 -->
  <!-- triggerSubMenuAction="click" -->
  <a-menu
    overlayClassname="indexDropdown"
    :selectedKeys="selectedKeys"
    @click="onMenuClick">
    <!-- :popupOffset="[10, -18]" -->
    <a-sub-menu
      v-if="props.field.aggregator !== SUMMARY_DEFAULT"
      popupClassName="index-submenu-dropdown"
      key="summary">
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
      <a-sub-menu
        popupClassName="index-submenu-dropdown"
        style="width: 120px"
        key="quantile"
        title="分位数">
        <a-menu-item
          v-for="item in quantileOptions"
          :key="item.value"
          :disabled="item.value === QUANTILE_PREFIX">
          <span v-if="item.value === QUANTILE_PREFIX">
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
              @change="onQuantileChange" />
          </span>
          <span v-else>{{ item.label }} </span>
        </a-menu-item>
      </a-sub-menu>
    </a-sub-menu>

    <!-- :popupOffset="[10, -50]" -->
    <a-sub-menu popupClassName="index-submenu-dropdown" key="quick">
      <template #title>
        快速计算
        <span v-if="field._quick" class="float-right font-help font-12">
          {{ quickLabel }}
        </span>
      </template>

      <a-menu-item v-for="item in quickCalculateOptions" :key="item.value">{{
        item.label
      }}</a-menu-item>
    </a-sub-menu>

    <a-menu-item key="rename">
      重命名
      <span v-if="field._modifyDisplayName" class="nameModified"></span>
    </a-menu-item>
  </a-menu>
</template>

<script setup lang="jsx">
import { ref, computed, inject, nextTick } from 'vue'
import { InfoCircleOutlined } from '@ant-design/icons-vue'
import {
  SUMMARY_DEFAULT,
  summaryOptions,
  quantileOptions,
  quickCalculateOptions,
  propertySummaryOptions,
  propertyTextSummaryOptions,
  propertyNumberSummaryOptions,
  QUANTILE_PREFIX,
} from '@/views/dataset/config.field'
import { CATEGORY } from '@/CONST.dict.js'
import CMenuList from '@/components/CMenuList/index.vue'

const emits = defineEmits(['update:open'])
const props = defineProps({
  dataset: { type: Object, default: () => ({}) },
  field: {
    type: Object,
    default: () => ({}),
  },
  category: {
    type: String,
    default: CATEGORY.INDEX,
  },
  open: {
    type: Boolean,
  },
})

const contentHeaderReject = inject('contentHeader')

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

    return propertySummaryOptions.concat(summary)
  }
})

// 汇总值
const aggregatorValue = ref()
// 汇总方式label
const aggregatorLabel = computed(() => {
  const options = summaryOptions.concat({ label: '分位数', value: QUANTILE_PREFIX })
  const item = options.find(
    item =>
      item.value === props.field.aggregator || item.value.startsWith(QUANTILE_PREFIX)
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
  if (!props.field._quick) return ''

  const item = quickCalculateOptions.find(t => t.value === props.field._quick)

  return item.label
})

watchEffect(() => {
  const { aggregator, _quick } = props.field

  aggregatorValue.value = aggregator
  quickValue.value = _quick
})

const selectedKeys = computed(() => {
  return [
    props.field._quantile ? QUANTILE_PREFIX : aggregatorValue.value,
    quickValue.value,
  ].filter(Boolean)
})
watch(selectedKeys, () => {
  props.field.aggregator = aggregatorValue.value
  props.field._quick = quickValue.value
})

const onMenuClick = item => {
  const { key, keyPath } = item

  if (key === 'rename') {
    contentHeaderReject.fieldRename(props.field)
  } else if (keyPath.includes('summary')) {
    props.field._quantile = undefined // 自定义分位数
    aggregatorValue.value = key
  } else if (keyPath.includes('quick')) {
    quickValue.value = key
  }

  emits('update:open', false)
}
</script>

<style scoped>
.font-help {
  margin-top: 3px;
}
.nameModified {
  position: absolute;
  top: 15px;
  right: 15px;
}
.nameModified::before {
  content: '';
  display: block;
  width: 6px;
  height: 10px;
  border: 2px solid #1677ff;
  border-top: 0;
  border-left: 0;
  transform: rotate(45deg) scale(1) translate(-50%, -50%);
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
    padding-left: 0;
  }
}
</style>
