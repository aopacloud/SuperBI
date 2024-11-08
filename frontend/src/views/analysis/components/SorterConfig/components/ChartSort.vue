<template>
  <p class="section-title">横轴排序</p>

  <TTable bordered size="small" :dataSource="rowList" :columns="cColumns">
    <template #cellRender="text, { column, record }">
      <template v-if="column.key === 'label'">横轴字段</template>

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
    </template>
  </TTable>
</template>

<script setup lang="jsx">
import { ref, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import { columns, orderOptions } from '../config'
import TTable from '@/common/components/TTable/index.vue'

const emits = defineEmits(['update:dataSource', 'custom'])
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

const cColumns = computed(() => columns.filter(t => t.key !== 'action'))

const rowFields = computed(() => props.fields.row)

const rowList = ref([])

watchEffect(() => {
  rowList.value = props.dataSource.row || []
})

const onFieldChange = item => {
  item.order = 'asc'
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
  if (rowList.value.some(t => !t.field)) {
    message.warn('排序字段不能为空')
    return
  }

  const fields = rowList.value.map(t => t.field)
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
