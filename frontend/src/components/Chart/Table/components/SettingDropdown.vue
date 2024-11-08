<template>
  <a-dropdown trigger="click" @openChange="onOpenChange">
    <slot>
      <SettingOutlined />
    </slot>
    <template #overlay>
      <a-menu @click="onMenuClick" :selectedKeys="selectedKeys">
        <a-menu-item
          v-for="item in sortOptions"
          :key="SORT_PREFFIX + '_/_' + item.order">
          {{ item.label }}
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
</template>
<script setup>
import { computed } from 'vue'
import { SettingOutlined } from '@ant-design/icons-vue'
import { CATEGORY } from '@/CONST.dict.js'
import { sortOptions, SORT_PREFFIX } from './config'
import { FORMAT_PREFFIX } from '@/views/dataset/config.field'

const emits = defineEmits(['openChange', 'menuClick'])
const props = defineProps({
  column: {
    type: Object,
    default: () => ({}),
  },
  sort: {
    type: Object,
    default: () => ({}),
  },
  formatter: {
    type: [Number, Object],
    default: () => ({}),
  },
})

const selectedKeys = computed(() => {
  const fieldName = props.column.field
  const { field: sortField, order } = props.sort
  const { field: formatterFeield, code } = props.formatter

  const sortKey = fieldName === sortField ? [SORT_PREFFIX + '_/_' + order] : []
  const formatterKey =
    fieldName === formatterFeield ? [FORMAT_PREFFIX + '_/_' + code] : []

  return sortKey.concat(formatterKey)
})

const onOpenChange = () => emits('openChange')

const onMenuClick = ({ key }) => {
  emits('menuClick', { key })
}
</script>

<style scoped>
.custom-formatter {
  position: fixed;
  z-index: 9;
  background: #fff;
  transform: translate(calc(-100% + 20px), 10px);
  cursor: initial;
}
</style>
