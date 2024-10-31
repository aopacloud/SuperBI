<template>
  <a-space-compact class="join-item" block size="small">
    <a-select
      style="flex: 1"
      show-search
      placeholder="请选择关联字段"
      :filterOption="filterOption"
      v-model:value="item.sourceField">
      <a-select-option
        v-for="f in sourceFields"
        :key="f.name"
        :label="f.description || f.name">
        {{ f.description || f.name }}
      </a-select-option>
    </a-select>
    <LinkOutlined style="margin: 0 12px" />
    <a-select
      style="flex: 1"
      show-search
      placeholder="请选择关联字段"
      :filterOption="filterOption"
      v-model:value="item.targetField">
      <a-select-option
        v-for="f in targetFields"
        :key="f.name"
        :label="f.description || f.name">
        {{ f.description || f.name }}
      </a-select-option>
    </a-select>
    <a-button
      class="remove-btn"
      type="text"
      size="small"
      :disabled="!removable"
      :icon="h(MinusOutlined)"
      :style="{ marginLeft: '6px', color: removable ? 'red' : '' }"
      @click="emits('remove')" />
  </a-space-compact>
</template>

<script setup>
import { h } from 'vue'
import { LinkOutlined, MinusOutlined } from '@ant-design/icons-vue'

const emits = defineEmits(['remove'])
const props = defineProps({
  source: {
    type: Object,
    default: () => ({}),
  },
  target: {
    type: Object,
    default: () => ({}),
  },
  item: {
    type: Object,
    default: () => ({}),
  },
  removable: Boolean,
})

const sourceFields = computed(() => props.source.originFields || [])
const targetFields = computed(() => props.target.originFields || [])

const filterOption = (input, option) => {
  const { value, label } = option
  return (
    value.toLowerCase().indexOf(input.toLowerCase()) >= 0 ||
    label.toLowerCase().indexOf(input.toLowerCase()) >= 0
  )
}
</script>
