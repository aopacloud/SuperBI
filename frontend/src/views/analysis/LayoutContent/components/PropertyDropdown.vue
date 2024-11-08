<template>
  <a-menu>
    <!-- triggerSubMenuAction="click" -->
    <template v-if="isDate">
      <a-sub-menu key="group">
        <template #title>
          日期分组
          <span class="float-right font-help font-12">{{ groupValueLabel }}</span>
        </template>
        <CMenuList
          style="width: 100px"
          :list="curDateGroupList"
          :value="groupValues"
          @change="onGroupKeyChange" />
      </a-sub-menu>
      <a-sub-menu v-if="curDateDisplayOptions.length" key="display" title="日期显示">
        <CMenuList
          :list="curDateDisplayOptions"
          :value="
            [displayValue, field._weekStart, field._monthStart].filter(
              t => typeof t !== 'undefined'
            )
          "
          @change="onDisplayKeyChange" />
      </a-sub-menu>
    </template>

    <a-menu-item
      key="dynamic"
      :class="{ 'selected-with-icon-right': isFieldDynamicFilter }"
      @click="onDynamicToggle">
      动态维度
    </a-menu-item>
  </a-menu>
</template>

<script setup>
import { ref, computed, inject, watch, watchEffect } from 'vue'
import {
  dateGroupOptions,
  dateGroupOptions_HHMMSS,
  dateDisplayOptions,
  GROUP_MONTH,
  GROUP_WEEK,
  DEFAULT_WEEK_DISPLAY,
  DEFAULT_DAY_DISPLAY,
  GROUP_DAY,
  toContrastFiled,
  GROUP_MINUTE,
  DEFAULT_WEEK_DAY_DISPLAY,
  DEFAULT_WEEK_START,
  DEFAULT_MONTH_DISPLAY,
  DEFAULT_MONTH_START,
} from '@/views/analysis/config'
import { CATEGORY } from '@/CONST.dict.js'
import CMenuList from '@/components/CMenuList/index.vue'
import { isTime_HHMMSS } from '@/views/dataset/config.field'

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
})

const isDate = computed(() => props.field.dataType?.includes('TIME'))

const { compare: indexCompare, options: indexOptions } = inject('index')

// 当前日期分组
const curDateGroupList = computed(() => {
  if (isTime_HHMMSS(props.field.dataType)) {
    return dateGroupOptions.concat(dateGroupOptions_HHMMSS)
  } else {
    return dateGroupOptions
  }
})
// 分组值
const groupValues = ref([])
const groupValueLabel = computed(() => {
  const [a] = groupValues.value
  if (!a) return

  const opt = curDateGroupList.value.find(t => t.value === a)
  return opt?.label
})
const onGroupKeyChange = key => {
  const [a, b] = key
  const children = curDateGroupList.value.find(t => t.value === a)['children']

  // 切换类型清空同环比配置
  if (toContrastFiled(props.field) && !groupValues.value.includes(a)) {
    indexCompare.set()
  }

  // 没有子级
  if (!children?.length) {
    groupValues.value = [a]
  } else {
    groupValues.value = [a, b || children[0]['value']]
  }

  const oldDisplayKey = displayValue.value
  if (a === GROUP_DAY) {
    props.field._weekStart = undefined
    if (!oldDisplayKey || !oldDisplayKey.startsWith(GROUP_DAY)) {
      displayValue.value = DEFAULT_DAY_DISPLAY
    }
  } else if (a === GROUP_WEEK) {
    if (!oldDisplayKey || !oldDisplayKey.startsWith(GROUP_WEEK)) {
      displayValue.value = DEFAULT_WEEK_DISPLAY
    }
    if (displayValue.value === DEFAULT_WEEK_DAY_DISPLAY) {
      props.field._weekStart = props.field._weekStart || DEFAULT_WEEK_START
    }
  } else if (a == GROUP_MONTH) {
    props.field._weekStart = undefined
    if (!oldDisplayKey || !oldDisplayKey.startsWith(GROUP_MONTH)) {
      displayValue.value = DEFAULT_MONTH_DISPLAY
    }
    if (displayValue.value === DEFAULT_MONTH_DISPLAY) {
      props.field._monthStart = props.field._monthStart || DEFAULT_MONTH_START
    }
  } else {
    props.field._weekStart = undefined
    props.field._monthStart = undefined
    displayValue.value = undefined
  }
}

// 当前的日期显示类型
const curDateDisplayOptions = computed(() => {
  const [a] = groupValues.value

  const hasDisplayKey = [GROUP_WEEK, GROUP_DAY, GROUP_MONTH].includes(a)
  if (!hasDisplayKey) return []

  return dateDisplayOptions.filter(t => t.group === a)
})

// 显示值
const displayValue = ref()
const onDisplayKeyChange = key => {
  const [viewModel, startValue] = key
  displayValue.value = viewModel

  props.field._weekStart = undefined
  props.field._monthStart = undefined

  if (viewModel === DEFAULT_WEEK_DAY_DISPLAY) {
    props.field._weekStart = startValue ?? (startValue || DEFAULT_WEEK_START)
  } else if (viewModel === DEFAULT_MONTH_DISPLAY) {
    props.field._monthStart = startValue ?? (startValue || DEFAULT_MONTH_START)
  }
}

const selectedKeys = computed(() => {
  const [a, b] = groupValues.value

  return [a, b, displayValue.value]
})

watch(selectedKeys, keys => {
  updateDateOption(keys)
})
const updateDateOption = (keys = selectedKeys.value) => {
  const [a, b, c] = keys

  if (a === GROUP_MINUTE) {
    props.field.dateTrunc = a + '_' + b
    props.field.firstDayOfWeek = undefined
  } else {
    props.field.dateTrunc = a
    props.field.firstDayOfWeek = b
  }
  props.field.viewModel = c
}

watchEffect(() => {
  const { dateTrunc, firstDayOfWeek, viewModel } = props.field

  if (dateTrunc?.includes(GROUP_MINUTE)) {
    groupValues.value = dateTrunc.split('_')
  } else {
    groupValues.value = [dateTrunc, firstDayOfWeek]
  }
  displayValue.value = viewModel
})

const dynamicFilters = computed(() => indexOptions.get('dynamicFilters') || {})
const isFieldDynamicFilter = computed(() => props.field.name in dynamicFilters.value)
const onDynamicToggle = () => {
  if (dynamicFilters.value[props.field.name]) {
    delete dynamicFilters.value[props.field.name]
  } else {
    dynamicFilters.value[props.field.name] = []
  }
}
</script>

<style scoped>
.font-help {
  margin-top: 3px;
}
</style>
