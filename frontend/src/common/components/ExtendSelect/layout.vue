<template>
  <div
    :class="[
      'select-layout',
      'select-layout--wrapper',
      hasTabsSlot ? 'select-layout--reverse' : '',
      bordered ? 'select-layout--borderd' : ''
    ]"
  >
    <!-- 头部区域 -->
    <div class="select-layout--title" v-if="multiple || hasTitleExtraSlot">
      <a-checkbox
        v-if="multiple"
        style="user-select: none"
        :disabled="disabled || enableList.length === 0"
        :checked="isAllSelected"
        :indeterminate="isIndeterminate"
        @change="allCheckedHandler"
      >
        全选
      </a-checkbox>

      <slot name="title-extra" v-bind="{ value: modelValue, enableList, list }">
        {{ modelValue.length }}/{{ enableList.length }}
      </slot>
    </div>

    <!-- tabs 区域 -->
    <slot name="tabs"></slot>

    <!-- 搜索区 -->
    <div class="select-layout--search">
      <a-input-search
        size="small"
        allow-clear
        placeholder="请输入搜索"
        v-model:value="keyword"
        :disabled="disabled"
      />
    </div>

    <!-- 面板区 -->
    <a-spin :spinning="loading">
      <!-- 无数据 -->
      <a-empty v-if="list.length === 0" style="flex: 1; margin: 60px 0" />

      <div v-else class="select-layout--content">
        <slot
          name="pane"
          v-bind="{
            dataSource: list,
            value: modelValue,
            updateValue: paneSlotUpdateValue
          }"
        />
      </div>
    </a-spin>

    <!-- 底部区域 -->
    <div v-if="$slots.footer" class="select-layout--footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, shallowRef, computed, useSlots, watchEffect } from 'vue'

defineOptions({
  name: 'ExtendSelectLayout'
})

const emits = defineEmits(['update:value', 'change'])
const props = defineProps({
  // 值
  value: {
    type: [String, Number, Array]
  },
  // 是否可多选
  multiple: {
    type: Boolean,
    default: true
  },
  // 数据源
  dataSource: {
    type: Array,
    default: () => []
  },
  // 是否需要外边框（也可使用时style指定）
  bordered: {
    type: Boolean,
    default: true
  },
  // 渲染 label 的字段
  labelField: {
    type: String,
    default: 'label'
  },
  // 渲染 key 的字段
  keyField: {
    type: String,
    default: 'key'
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // loading
  loading: {
    type: Boolean,
    default: false
  },
  // 自定义过滤方法
  filterOption: {
    type: Function
  },
  height: {
    stype: Number,
    default: 500
  }
})

const modelValue = ref()

watch(
  () => props.value,
  value => {
    if (props.multiple) {
      modelValue.value =
        typeof value === 'undefined'
          ? []
          : Array.isArray(value)
            ? [...value]
            : [value]
    } else {
      modelValue.value = Array.isArray(value) ? value[0] : value
    }
  },
  { immediate: true, deep: true }
)

const originList = shallowRef([])
const list = shallowRef([]) // 显示数据

// 监听数据源的变化
watchEffect(() => {
  originList.value = props.dataSource.map(t => {
    return {
      ...t,
      disabled: t.disabled || props.disabled
    }
  })

  list.value = originList.value
})

// 是否有tabs插槽
const hasTabsSlot = computed(() => {
  return !!useSlots().tabs
})

// 是否有titleExtra插槽
const hasTitleExtraSlot = computed(() => {
  return !!useSlots().titleExtra
})

// 可选的列表
const enableList = computed(() => {
  return list.value.filter(t => !t.disabled)
})

// 已经选中的不可选值
const disabledValueSelected = computed(() => {
  return modelValue.value.filter(v =>
    list.value.some(f => f[props.keyField] === v && f.disabled)
  )
})
// 半选
const isIndeterminate = computed(() => {
  if (!props.multiple) return false

  return (
    modelValue.value.length > 0 &&
    modelValue.value.length !==
      enableList.value.length + disabledValueSelected.value.length
  )
})

// 全选
const isAllSelected = computed(() => {
  if (!props.multiple) return false
  if (!enableList.value.length) return false

  return (
    modelValue.value.length ===
    enableList.value.length + disabledValueSelected.value.length
  )
})
const allCheckedHandler = e => {
  const checked = e.target.checked

  modelValue.value = !checked
    ? [...disabledValueSelected.value]
    : disabledValueSelected.value.concat(
        enableList.value.map(t => t[props.keyField])
      )

  emits('update:value', modelValue.value)
  emits('change', [...modelValue.value])
}

const _toString = s => String(s).toLowerCase()

// 搜索关键字
const keyword = ref()
watch(keyword, word => {
  let str = word.trim()

  if (!str.length) {
    list.value = originList.value
  } else {
    str = _toString(str)
    list.value = originList.value.filter(item => {
      return props.filterOption && typeof props.filterOption === 'function'
        ? props.filterOption(str, item)
        : _toString(item[props.labelField]).includes(str) ||
            _toString(item[props.keyField]).includes(str)
    })
  }
})
// 清除过滤关键字
const clearSearch = () => {
  keyword.value = ''
}
// 向外暴露的方法
defineExpose({
  clearSearch
})
// 面板值的传递
const paneSlotUpdateValue = e => {
  modelValue.value = e

  emits('update:value', e)
  emits('change', e)
}
</script>

<style scoped lang="scss">
.select-layout {
  position: relative;
  display: flex;
  flex-direction: column;
  line-height: initial;
  min-height: 360px;
  max-height: 520px;
  &--disabled {
    color: rgba(0, 0, 0, 0.25);
  }
  &--disabled:before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    z-index: 1;
    background-color: rgba(245, 245, 245, 0.15);
    cursor: not-allowed;
  }
  &--borderd {
    border: 1px solid #e8e8e8;
  }
  &--reverse {
    .select-layout--title {
      order: 999;
      border-top: 1px solid #e8e8e8;
      border-bottom: none;
    }
  }
  &--title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 5px 12px;
    border-bottom: 1px solid #e8e8e8;
  }
  &--tabs {
    :deep(.ant-tabs-nav) .ant-tabs-tab {
      padding: 6px 12px;
      margin: 0 8px;
    }
  }
  &--search {
    margin-top: 10px;
    padding: 0 12px;
  }
  & > :deep(.ant-spin-nested-loading) {
    margin-top: 8px;
    flex: 1;
    overflow: auto;
    display: flex;
    overflow: auto;
    & > .ant-spin-container {
      flex: 1;
      overflow: auto;
    }
  }

  &--content {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
  }
  &--footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 3px 12px;
    margin-top: 3px;
    border-top: 1px solid #e8e8e8;
    &-extra {
      flex: 1;
      text-align: right;
      user-select: none;
    }
  }
  &--list {
    margin-top: 10px;
    max-height: 300px;
  }

  :deep(.ant-input-affix-wrapper-status-error) {
    border-color: #d9d9d9 !important;
  }
}
</style>
