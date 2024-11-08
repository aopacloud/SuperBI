<template>
  <div class="select-container">
    <div class="p-item">
      <div class="flex-1 ellipsis">{{ table.dbName }}.{{ table.tableName }}</div>

      <Filter style="margin-left: 12px" :table="table" :active="hasValidFilters" />
    </div>
    <div class="p-item">
      <span style="margin-right: 8px">
        ({{ rowSelection.selectedRowKeys.length }}/{{ originFields.length }})
      </span>
      <a-input-search
        style="width: 300px"
        size="small"
        allow-clear
        placeholder="请输入搜索"
        v-model:value.trim="keyword" />
    </div>
    <a-table
      size="small"
      rowKey="name"
      class="select-table"
      :scroll="{ y: 360 }"
      :row-selection="rowSelection"
      :columns="columns"
      :dataSource="list"
      :pagination="false"></a-table>
  </div>
</template>

<script setup lang="jsx">
import { ref, watchEffect } from 'vue'
import Filter from './Filter.vue'
import { getIconByFieldType } from '@/views/dataset/utils'

const columns = [
  {
    title: '',
    width: 30,
    dataIndex: 'dataType',
    customRender: ({ text }) => {
      const { icon, color } = getIconByFieldType(text)
      return (
        <span>
          <i
            class={['iconfont', icon]}
            style={{
              marginRight: '8px',
              color: color,
            }}></i>
        </span>
      )
    },
  },
  {
    title: '字段名',
    dataIndex: 'name',
  },
  {
    title: '字段描述',
    dataIndex: 'description',
  },
]

const props = defineProps({
  table: {
    type: Object,
    default: () => ({ columns: [], filters: [] }),
  },
  type: {
    type: String,
    default: 'origin',
    validator: str => ['source', 'target'].includes(str),
  },
  sourceHasDt: Boolean,
})

const hasValidFilters = computed(() => props.table.filters?.children?.length > 0)

const originFields = computed(() => props.table.originFields || [])
const keyword = ref('')
const list = computed(() => {
  const k = keyword.value.trim()
  if (!k.length) return originFields.value

  return originFields.value.filter(
    t => t.name.includes(k) || t.description.includes(k)
  )
})

const rowSelection = reactive({
  onChange: (selectedKeys = []) => {
    if (!keyword.value.length) {
      rowSelection.selectedRowKeys = selectedKeys
    } else {
      const allKeys = list.value.map(t => t.name)
      allKeys.forEach(key => {
        if (rowSelection.selectedRowKeys.includes(key)) {
          if (selectedKeys.includes(key)) {
            return
          } else {
            const i = rowSelection.selectedRowKeys.indexOf(key)
            rowSelection.selectedRowKeys.splice(i, 1)
          }
        } else {
          if (selectedKeys.includes(key)) {
            rowSelection.selectedRowKeys.push(key)
          }
        }
      })
    }
  },
  selectedRowKeys: [],
})
watch(
  () => rowSelection.selectedRowKeys,
  v => {
    props.table.columns = v
  }
)

watchEffect(() => {
  rowSelection.selectedRowKeys = props.table.columns || []
})
</script>

<style lang="scss" scoped>
.select-container {
  display: flex;
  flex-direction: column;
  overflow: auto;
}
.p-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.select-table {
  flex: 1;
  border: 1px solid #d9d9d9;
  border-radius: 2px;
  :deep(.ant-table) {
    th,
    td {
      padding: 4px !important;
    }

    .ant-table-row-selected > td {
      background-color: initial;
    }
  }
}
</style>
