<template>
  <div
    ref="tagRef"
    :class="['tag', hasColor && 'has-color']"
    :data-id="tag._id"
    :data-color="color"
    :style="style"
  >
    <div v-if="!showPopover" class="tag-name">
      <slot>{{ tag }}</slot>
    </div>

    <template v-else>
      <a-dropdown
        trigger="click"
        :overlayStyle="dropdownOverlayStyle"
        :overlayClassName="category + '-dropdown'"
        v-model:open="dropdownOpen"
        @openChange="onBlur"
      >
        <div class="tag-dropdown--trigger" :class="{ open: dropdownOpen }">
          <div class="tag-name">
            <slot>{{ tag }}</slot>
          </div>

          <div class="tag-extra">
            <span v-if="category === CATEGORY.PROPERTY">
              {{ displayPropertyDateGroup(tag) }}
            </span>
            <span
              v-else-if="category === CATEGORY.INDEX"
              :title="displayIndexAggregator(tag)"
            >
              {{ displayIndexAggregator(tag) }}
            </span>
            <span
              v-else-if="category === CATEGORY.FILTER"
              :title="displayFilter(tag, { timeOffset })"
            >
              {{ displayFilter(tag, { timeOffset }) }}
            </span>
          </div>

          <FilterOutlined
            v-if="category === CATEGORY.FILTER"
            class="tag-trigger--filter-icon"
            :class="{
              active:
                (isNested && !!tag.tableFilter) ||
                isDate ||
                (tag.conditions && tag.conditions.length)
            }"
          />
          <DownOutlined v-else class="tag-trigger--icon" />
        </div>

        <template #overlay>
          <component
            :is="renderView"
            :single="tag.filterMode === 'single'"
            :dataset="dataset"
            :field="tag"
            :timeOffset="timeOffset"
            v-model:open="dropdownOpen"
          />
        </template>
      </a-dropdown>
    </template>

    <CloseOutlined
      v-if="isTagRemovable"
      @click="handlRemove"
      class="tag-remove"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, inject, ref } from 'vue'
import {
  CloseOutlined,
  DownOutlined,
  FilterOutlined
} from '@ant-design/icons-vue'
import PropertyDropdown from './PropertyDropdown.vue'
import IndexDropdown from './IndexDropdown.vue'
import FilterDropdown from '@/components/Filter/index.vue'
import { displayFilter as _displayFilter } from '@/components/Filter/utils'
import { CATEGORY } from '@/CONST.dict.js'
import {
  summaryOptions,
  propertySummaryOptions,
  propertyTextSummaryOptions,
  propertyNumberSummaryOptions,
  SUMMARY_PROPERTY_DEFAULT,
  SUMMARY_INDEX_DEFAULT,
  operatorMap,
  QUANTILE_PREFIX,
  summaryQuantile
} from '@/views/dataset/config.field'
import { isDateField } from '@/views/dataset/utils'
import {
  dateGroupOptions,
  dateGroupOptions_HHMMSS,
  GROUP_MINUTE
} from '@/views/analysis/config'
import {
  getStartDateStr,
  getEndDateStr
} from 'common/components/DatePickers/utils'
import dayjs from 'dayjs'

const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({})
  },
  category: {
    type: String,
    default: CATEGORY.PROPERTY
  },
  tag: {
    type: Object,
    default: () => ({})
  },
  color: {
    type: String
  },
  open: Boolean
})

const { timeOffset } = inject('index')

const hasColor = computed(() => !!props.color)
const style = computed(() => {
  return {
    backgroundColor: props.color
  }
})

const dropdownOverlayStyle = computed(() => {
  const c = props.category
  const isP = c === CATEGORY.PROPERTY
  const isI = c === CATEGORY.INDEX
  const isF = c === CATEGORY.FILTER

  return {
    width: isP ? '140px' : isI ? '200px' : '',
    minWidth: 'initial'
  }
})

const showPopover = computed(() => {
  return true

  // return (
  //   (props.category === CATEGORY.PROPERTY && props.tag.dataType?.includes('TIME')) ||
  //   props.category === CATEGORY.INDEX ||
  //   props.category === CATEGORY.FILTER
  // )
})
const renderView = computed(() => {
  return props.category === CATEGORY.PROPERTY
    ? PropertyDropdown
    : props.category === CATEGORY.INDEX
      ? IndexDropdown
      : FilterDropdown
})

const isTagRemovable = computed(() => {
  return props.tag._forced !== true
})

const isNested = computed(() => props.tag.nested)

// 日期筛选
const isDate = computed(
  () => !isNested.value && props.tag.dataType.includes('TIME')
)

const getOffsetByDate = date => {
  const offset = dayjs().diff(date, 'day')

  return [offset, offset]
}

// 初始化过滤条件
const initFieldFilter = () => {
  // _latestPartitionValue 最近有数的一天
  const {
    dataType,
    _latestPartitionValue,
    filterType = 'ENUM',
    logical = 'AND',
    having = false,
    conditions = [],
    nested
  } = props.tag

  if (nested) return

  const { force, filters: forcedFilter = [] } = props.dataset.extraConfig

  const forcedItem = force
    ? forcedFilter.find(t => t.name === props.tag.name)
    : null
  if (forcedItem) {
    props.tag.filterMode = forcedItem.filterMode
    props.tag._forced = true
  }

  if (dataType.includes('TIME')) {
    props.tag.logical = logical
    props.tag.conditions = conditions.length
      ? conditions
      : [
          {
            functionalOperator: 'BETWEEN',
            timeType: 'RELATIVE', // 时间字段的筛选类型， EXACT 精确时间， RELATIVE 相对时间
            args: _latestPartitionValue
              ? getOffsetByDate(_latestPartitionValue)
              : []
          }
        ]
  } else {
    props.tag.filterType = filterType
    props.tag.logical = logical
    props.tag.having = having
    props.tag.conditions = conditions
  }
}

// 初始化聚合方式
const initFieldAggregator = () => {
  props.tag.aggregator =
    props.tag.category === CATEGORY.PROPERTY
      ? SUMMARY_PROPERTY_DEFAULT
      : SUMMARY_INDEX_DEFAULT
}

const tagRef = ref(null)
onMounted(() => {
  if (props.category === CATEGORY.FILTER) {
    initFieldFilter()

    if (props.tag.autoPopover) {
      dropdownOpen.value = true
    }
  } else {
    if (props.category === CATEGORY.INDEX && !props.tag.aggregator) {
      initFieldAggregator()
    }
  }

  tagRef.value.style.setProperty('--color', props.color)
})

const getSummaryOptions = (category, dataType) => {
  let result = []
  if (category === CATEGORY.INDEX) {
    result = summaryOptions
  } else {
    const isTextOrTime = dataType === 'TEXT' || dataType.includes('TIME')
    const isNumber = dataType === 'NUMBER'
    const summary = isTextOrTime
      ? propertyTextSummaryOptions
      : isNumber
        ? propertyNumberSummaryOptions
        : []

    result = propertySummaryOptions.concat(summary)
  }

  return result.concat(summaryQuantile)
}

// 显示日期分组
const displayPropertyDateGroup = field => {
  if (!isDateField(field)) return

  let { dateTrunc } = field
  if (!dateTrunc) return

  if (dateTrunc.includes(GROUP_MINUTE)) dateTrunc = GROUP_MINUTE

  const dateGroups = [...dateGroupOptions, ...dateGroupOptions_HHMMSS]
  const opt = dateGroups.find(t => t.value === dateTrunc)
  return opt?.label
}

// 聚合显示文本
const displayIndexAggregator = field => {
  const { category, dataType, aggregator } = field
  if (!aggregator) return

  const options = getSummaryOptions(category, dataType)
  const item = options.find(t => {
    if (t.value === aggregator) return true

    if (aggregator.startsWith(QUANTILE_PREFIX))
      return t.value === QUANTILE_PREFIX

    return false
  })
  if (!item) return aggregator

  const [_, number] = aggregator.split(item.value)
  return number + item.label
}

const displayFilter = (tag, ...e) => {
  if (tag.category === CATEGORY.FILTER && tag.nested) {
    return
  }

  return _displayFilter(tag, ...e)
}

// 所有的操作符
const operators = operatorMap.DEFAULT

const emits = defineEmits(['remove'])
const handlRemove = () => {
  emits('remove', props.tag)
}

const dropdownOpen = ref(false)

const { getCustomFormatter } = inject('contentHeader')
const customFormatter = computed(() => getCustomFormatter())

const onBlur = () => {
  if (customFormatter.value.open) {
    dropdownOpen.value = true
  }
}

// 自定义格式完成，关闭下拉框的显示
watch(
  () => customFormatter.value.open,
  e => {
    if (!e) dropdownOpen.value = false
  }
)
</script>

<style lang="scss" scoped>
.tag {
  --color: 'transparent';
  display: inline-flex;
  justify-content: space-between;
  align-items: center;
  line-height: 22px;
  min-width: 60px;
  padding: 0 8px 0 0;
  margin-right: 8px;
  background-color: rgba(0, 0, 0, 0.02);
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 12px;
  transition: all 0.2s;
  color: rgba(0, 0, 0, 0.88);
  cursor: default;

  &:hover {
    .tag-remove {
      opacity: 1;
    }
  }

  &.has-color {
    color: #fff;
    border-color: transparent;
    .tag-remove {
      color: #fff;
      &:hover {
        background-color: #fff;
        color: var(--color);
      }
    }
  }
}

.tag-dropdown--trigger {
  display: flex;
  .tag-trigger--icon,
  .tag-trigger--filter-icon {
    margin-left: 12px;
    transition: all 0.2s;
  }
  .tag-trigger--filter-icon.active {
    color: #1677ff;
  }
  &.open {
    .tag-trigger--icon {
      transform: rotate(180deg);
    }
  }
}

.tag-name {
  padding-left: 10px;
  flex: 1;
  max-width: 200px;
  @extend .ellipsis;
}

.tag-extra {
  margin-left: 24px;
  color: rgba(255, 255, 255, 0.7);
  max-width: 235px;
  @extend .ellipsis;
}

.tag-remove {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 16px;
  height: 16px;
  margin-left: 6px;
  border-radius: 50%;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 10px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
}
</style>

<style lang="scss">
.MEASURE-dropdown {
  .ant-dropdown-menu-submenu-selected .ant-dropdown-menu-submenu-title {
    color: inherit !important;
  }
}
</style>
