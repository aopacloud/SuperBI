<template>
  <a-modal
    title="数据表详情"
    :width="750"
    :open="open"
    :footer="null"
    :bodyStyle="{ marginBottom: '-16px' }"
    @cancel="emits('update:open', false)">
    <p>数据表：{{ initData.tableName }}</p>
    <p>描述：{{ initData.description || '-' }}</p>

    <a-table
      size="small"
      :scroll="{ y: 600 }"
      :loading="loading"
      :pagination="pager"
      :columns="datasetColumns"
      :dataSource="list"
      @change="onTableChange"></a-table>
  </a-modal>
</template>

<script setup>
import { watch } from 'vue'
import useTable from '@/common/hooks/useTable'
import { getTableDetail } from '@/apis/datasource'
import { datasetColumns } from '../config'

const emits = defineEmits(['update:open', 'cancel'])
const props = defineProps({
  open: { type: Boolean, default: false },
  datasourceId: { type: Number },
  initData: { type: Object, default: () => ({}) },
})

watch(
  () => props.open,
  op => {
    if (op === true) {
      list.value = []
      pager.current = 1
      fetchList()
    }
  }
)

const { loading, list, pager, fetchList } = useTable(
  () => getTableDetail(props.datasourceId, props.initData.tableName),
  {
    transform(res) {
      const { fields } = res

      return {
        data: fields,
        total: fields.length,
      }
    },
    onErorr(error) {
      console.error('获取表详情失败', error)
    },
  }
)
const onTableChange = ({ current, pageSize }) => {
  pager.current = current
  pager.pageSize = pageSize
}
</script>

<style lang="scss" scoped></style>
