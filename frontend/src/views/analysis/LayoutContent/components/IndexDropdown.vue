﻿<template>
  <a-dropdown
    :overlayStyle="{ width: '140px', minWidth: 'initial' }"
    trigger="click"
    v-model:open="open">
    <div class="index-dropdown-trigger" :class="{ open: open }">
      <slot></slot>

      <DownOutlined class="dropdown-trigger-icon" />
    </div>
    <template #overlay>
      <a-menu :selectedKeys="selectedKeys" @click="onMenuClick">
        <a-sub-menu
          v-if="props.field.aggregator !== SUMMARY_DEFAULT"
          key="summary"
          title="汇总方式">
          <a-menu-item v-for="item in summaries" :key="item.value">
            {{ item.label }}
          </a-menu-item>
        </a-sub-menu>
        <a-menu-item key="rename">重命名</a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
</template>

<script setup>
import { ref, computed, toRefs, inject, watchEffect } from 'vue'
import { DownOutlined } from '@ant-design/icons-vue'
import {
  SUMMARY_DEFAULT,
  summaryOptions,
  propertySummaryOptions,
  propertyTextSummaryOptions,
  propertyNumberSummaryOptions,
} from '@/views/dataset/config.field'
import { CATEGORY } from '@/CONST.dict.js'

const props = defineProps({
  field: {
    type: Object,
    default: () => ({}),
  },
  category: {
    type: String,
    default: CATEGORY.INDEX,
  },
})

const open = ref(false)
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

const contentHeaderReject = inject('contentHeader')
const selectedKeys = ref([])
watchEffect(() => {
  selectedKeys.value = [props.field.aggregator]
})
const onMenuClick = ({ key }) => {
  open.value = false
  if (key === 'rename') {
    contentHeaderReject.fieldRename(props.field)
  } else {
    selectedKeys.value = [key]

    props.field.aggregator = key
  }
}
</script>

<style lang="scss" scoped>
.index-dropdown-trigger {
  display: inline-flex;
  &.open {
    .dropdown-trigger-icon {
      transform: rotate(180deg);
    }
  }
}
.dropdown-trigger-icon {
  margin-left: 12px;
  transition: all 0.2s;
}
</style>
