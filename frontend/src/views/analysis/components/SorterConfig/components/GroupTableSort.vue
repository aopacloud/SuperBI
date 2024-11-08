<template>
  <p class="section-title">
    行排序
    <span class="font-help" style="margin-left: 24px; font-size: 14px">
      表格中的首列数据将按照如下方式进行排序
    </span>
  </p>

  <TTable bordered size="small" :dataSource="list" :columns="columns">
    <template #cellRender="text, { column, record, rowIndex }">
      <template v-if="column.key === 'label'">
        <span v-if="rowIndex === 0">排序依据</span>
        <span v-else style="color: rgba(0, 0, 0, 0.38)">次要依据</span>
      </template>

      <a-select
        v-if="column.key === 'field'"
        style="width: 100%"
        class="select-no-border"
        placeholder="选择字段名称"
        size="small"
        :options="fields"
        v-model:value="record.field"
        @change="onFieldChange(record)"
      />

      <a-select
        v-if="column.key === 'order'"
        style="width: 100%"
        class="select-no-border"
        placeholder="选择排序方式"
        size="small"
        :options="orderOptions"
        :value="record.order"
        @select="e => onOrderSelect(e, record)"
      />

      <a-space v-if="column.key === 'action'" :size="0">
        <a-button
          style="margin-left: 4px; color: #0784f8"
          type="text"
          size="small"
          :icon="h(PlusCircleOutlined)"
          @click="addItem(rowIndex)"
        />
        <a-button
          style="margin-left: 4px"
          type="text"
          size="small"
          :style="{ color: list.length > 1 ? '#ff4e4f' : '' }"
          :disabled="list.length < 2"
          :icon="h(MinusCircleOutlined)"
          @click="removeItem(rowIndex)"
        />
      </a-space>
    </template>
  </TTable>
</template>

<script setup lang="jsx">
import { h, ref, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import { PlusCircleOutlined, MinusCircleOutlined } from '@ant-design/icons-vue'
import { columns, orderOptions } from '../config'
import TTable from '@/common/components/TTable/index.vue'

const emits = defineEmits(['update:dataSource', 'custom'])
const props = defineProps({
  dataSource: {
    type: Array,
    default: () => []
  },
  fields: {
    type: Array,
    default: () => []
  }
})

const list = ref([])
const addItem = index => {
  list.value.splice(index + 1, 0, { field: undefined, order: 'desc' })
}
const removeItem = index => {
  list.value.splice(index, 1)
}

watchEffect(() => {
  list.value = props.dataSource
})

const onFieldChange = item => {
  item.order = 'desc'
  item.custom = undefined
}

const onOrderSelect = (e, item) => {
  if (e === 'custom') {
    if (!item.field) return
    emits('custom', { ...item })
  } else {
    item.order = e
    item.custom = undefined
  }
}

const validate = () => {
  if (list.value.some(t => !t.field)) {
    message.warn('排序字段不能为空')
    return
  }

  const fields = list.value.map(t => t.field)
  if ([...new Set(fields)].length !== fields.length) {
    message.warn('排序字段不能重复')
    return
  }

  return true
}

defineExpose({ validate })
</script>

<style lang="scss" scoped>
.section-title {
  position: relative;
  padding: 4px 12px;
  margin-bottom: 12px;
  font-size: 16px;
  color: #1677ff;
  background-color: #f2f2f2;
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 2px;
    bottom: 2px;
    width: 3px;
    background-color: currentColor;
  }
}
</style>
