<template>
  <a-modal
    title="数据集列表"
    :width="1200"
    :open="open"
    :footer="null"
    @cancel="emits('update:open', false)">
    <p>数据表：{{ initData.tableName }}</p>
    <p>描述：{{ props.initData.description || '-' }}</p>

    <a-table
      size="small"
      :loading="loading"
      :scroll="{ y: 600 }"
      :pagination="pager"
      :columns="datasourceTableColumns"
      :dataSource="list"
      @change="onTableChange"></a-table>
  </a-modal>
</template>

<script setup>
import { watch } from 'vue'
import useTable from '@/common/hooks/useTable'
import { getDatasetDetail } from '@/apis/datasource'
import { datasourceTableColumns } from '../config'

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
  () => getDatasetDetail(props.datasourceId, props.initData.tableName),
  {
    transform(res) {
      return {
        data: res,
        total: res.length,
      }
    },
  }
)

const onTableChange = ({ current, pageSize }) => {
  pager.current = current
  pager.pageSize = pageSize
}
</script>

<style lang="scss" scoped></style>
