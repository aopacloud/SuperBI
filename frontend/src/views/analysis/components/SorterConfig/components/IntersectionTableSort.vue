<template>
  <p class="section-title">
    行排序
    <span class="font-help" style="margin-left: 24px; font-size: 14px">
      表格中的首列数据将按照如下方式进行排序
    </span>
  </p>
  <TTable bordered size="small" :dataSource="rowList" :columns="columns">
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
        :options="rowFields"
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
          @click="addRowListItem(rowIndex)"
        />
        <a-button
          style="margin-left: 4px"
          type="text"
          size="small"
          :style="{ color: rowList.length > 1 ? '#ff4e4f' : '' }"
          :disabled="rowList.length < 2"
          :icon="h(MinusCircleOutlined)"
          @click="removeRowListItem(rowIndex)"
        />
      </a-space>
    </template>
  </TTable>

  <p class="section-title">
    列排序
    <span class="font-help" style="margin-left: 24px; font-size: 14px">
      表格中的列表头将按照如下方式进行排序
    </span>
  </p>
  <TTable bordered size="small" :dataSource="columnList" :columns="columns">
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
        :options="columnFields"
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
          @click="addColumnListItem(rowIndex)"
        />
        <a-button
          style="margin-left: 4px"
          type="text"
          size="small"
          :style="{ color: columnList.length > 1 ? '#ff4e4f' : '' }"
          :disabled="columnList.length < 2"
          :icon="h(MinusCircleOutlined)"
          @click="removeColumnListItem(rowIndex)"
        />
      </a-space>
    </template>
  </TTable>
</template>

<script setup lang="jsx">
import { h, ref, computed, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import { PlusCircleOutlined, MinusCircleOutlined } from '@ant-design/icons-vue'
import { columns, orderOptions } from '../config'
import TTable from '@/common/components/TTable/index.vue'
import { shallowRef } from 'vue'

const emits = defineEmits(['custom'])
const props = defineProps({
  dataSource: {
    type: Object,
    default: () => ({ row: [], columns: [] })
  },
  fields: {
    type: Object,
    default: () => ({ row: [], columns: [] })
  }
})

const rowFields = computed(() => props.fields.row)
const columnFields = computed(() => props.fields.column)

// 行分组
const rowList = shallowRef([])
const addRowListItem = index => {
  rowList.value.splice(index + 1, 0, { field: undefined, order: 'desc' })
}
const removeRowListItem = index => {
  rowList.value.splice(index, 1)
}

// 列分组
const columnList = shallowRef([])
const addColumnListItem = index => {
  columnList.value.splice(index + 1, 0, { field: undefined, order: 'desc' })
}
const removeColumnListItem = index => {
  columnList.value.splice(index, 1)
}

watchEffect(() => {
  rowList.value = props.dataSource.row
  columnList.value = props.dataSource.column
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
  const fn = (list = []) => {
    if (list.some(t => !t.field)) {
      message.warn('排序字段不能为空')
      return
    }

    const fields = list.map(t => t.field)
    if ([...new Set(fields)].length !== fields.length) {
      message.warn('排序字段不能重复')
      return
    }

    return true
  }

  if (!fn(rowList.value)) return
  if (!fn(columnList.value)) return

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
