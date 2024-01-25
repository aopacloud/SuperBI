<template>
  <VirtualList
    class="pane-list"
    :class="{ single: !multiple }"
    :data-source="dataSource"
    :item-height="26">
    <template #item="{ item, index, $index }">
      <div
        class="pane-item"
        :class="{ active: isItemActive(item), disabled: item.disabled }"
        @click="itemClick(item)">
        <slot name="item" v-bind="{ item, index, $index }">
          <div class="pane-item-content" style="padding: 0 12px">
            <a-checkbox
              v-if="multiple"
              style="margin-right: 6px"
              :disabled="item.disabled"
              :checked="isItemActive(item)" />
            <div class="pane-item-label">
              {{ item[labelField] }}
            </div>
          </div>
        </slot>
      </div>
    </template>
  </VirtualList>
</template>

<script setup>
import { ref, watch } from 'vue'
import VirtualList from 'common/components/VirtualList/index.vue'

const emits = defineEmits(['update:value'])
const props = defineProps({
  value: {
    type: [String, Number, Array],
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
  // 渲染 key 的字段
  keyField: {
    type: String,
    default: 'key',
  },
  // 渲染 label 的字段
  labelField: {
    type: String,
    default: 'label',
  },
  // 是否可多选
  multiple: {
    type: Boolean,
    default: true,
  },
})

const modelValue = ref()

watch(
  () => props.value,
  value => {
    if (props.multiple) {
      modelValue.value =
        typeof value === 'undefined' ? [] : Array.isArray(value) ? [...value] : [value]
    } else {
      modelValue.value = Array.isArray(value) ? value[0] : value
    }
  },
  { immediate: true, deep: true }
)

// 高亮
const isItemActive = item => {
  const { multiple, keyField } = props

  return multiple
    ? modelValue.value.includes(item[keyField])
    : modelValue.value === item[keyField]
}

const itemClick = item => {
  if (item.disabled) return

  onSelected(item)
}
// 选中
const onSelected = item => {
  const { multiple, keyField } = props

  if (multiple) {
    const inIndex = modelValue.value.findIndex(t => t === item[keyField])

    if (inIndex > -1) {
      modelValue.value.splice(inIndex, 1)
    } else {
      modelValue.value.push(item[keyField])
    }

    emits('update:value', modelValue.value)
  } else {
    modelValue.value = item[keyField]

    emits('update:value', item[keyField])
  }
}
</script>

<style lang="scss" scoped>
.pane-list {
  &.single {
    .pane-item {
      &.active {
        background-color: #1677ff;
        color: #fff;
      }
      &.disabled.active {
        background-color: lighten(#1677ff, 42%);
        color: rgba(0, 0, 0, 0.25);
      }
    }
  }
}
.pane-item {
  cursor: pointer;

  &:not(.disabled):hover {
    background-color: #ebfaff;
  }

  &-content {
    padding: 0 12px;
    display: flex;
    align-items: center;
  }

  &.disabled {
    color: rgba(0, 0, 0, 0.25);
    cursor: not-allowed;
  }

  &-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
