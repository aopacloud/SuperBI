<template>
  <a-table
    class="history-table"
    size="small"
    rowKey="id"
    bordered
    v-resize="onResize"
    :loading="loading"
    :columns="columns"
    :data-source="list"
    :pagination="pager"
    :scroll="{ y: tableHeight }"
    @change="onTableChange">
    <template #bodyCell="{ text, column, record }">
      <div class="sql-cell" v-if="column.dataIndex === 'sql'">
        <div>
          <a @click="sqlPreview(record)">{{ text }}</a>
        </div>

        <a-button
          size="small"
          type="text"
          :icon="h(CopyOutlined)"
          v-copy="text"
          v-copy:success="onCopySuccess"></a-button>
      </div>

      <p-a-space v-if="column.dataIndex === 'action'">
        <a-button size="small" :loading="record.loading" @click="download(record)">
          下载
        </a-button>
        <a-button size="small" type="primary" @click="query(record)">查看</a-button>
      </p-a-space>
    </template>
  </a-table>

  <SqlPreviewDrawer v-model:open="sqlDrawerOpen" :sql="record.sql" />
</template>
<script setup>
import { message } from 'ant-design-vue'
import { h, ref, reactive, shallowReactive, computed, inject, onActivated } from 'vue'
import { CopyOutlined } from '@ant-design/icons-vue'
import { historyTableColums } from './config'
import { getQueryHistory, downloadHistoryResult } from '@/apis/analysis'
import SqlPreviewDrawer from '@/views/dashboard/modify/components/SqlPreviewDrawer.vue'
import { downloadWithBlob } from 'common/utils/file'

const emits = defineEmits(['revert'])

const indexInject = inject('index')

const tableHeight = ref(0)
const onResize = e => {
  tableHeight.value = e.target.parentElement.clientHeight - 95
}

const loading = ref(false)
const columns = shallowReactive(historyTableColums)
const list = ref([])
const pager = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const queryParams = computed(() => {
  const { current: pageNum, pageSize } = pager
  const dataset = indexInject.dataset.get()

  return { pageNum, pageSize, datasetId: dataset.id }
})
// 获取数据
const fetchData = async () => {
  try {
    loading.value = true

    const { data = [], total = 0 } = await getQueryHistory(queryParams.value)

    list.value = data
    pager.total = total
  } catch (error) {
    console.error('获取记录查询错误', error)
  } finally {
    loading.value = false
  }
}
const onTableChange = p => {
  const { current, pageSize } = p

  pager.current = current
  pager.pageSize = pageSize

  fetchData()
}
onActivated(() => {
  fetchData()
})

// 复制
const onCopySuccess = () => {
  message.success('复制成功')
}

const record = ref({})
const sqlDrawerOpen = ref(false)
const sqlPreview = row => {
  sqlDrawerOpen.value = true
  record.value = { ...row }
}

// 下载
const download = async row => {
  try {
    row.loading = true

    const res = await downloadHistoryResult(row.id)

    await downloadWithBlob(res, `${row.createTime}-${Date.now()}.xlsx`)
  } catch (error) {
    console.error('查询历史下载错误', error)
  } finally {
    row.loading = false
  }
}

// 查看\恢复
const query = row => {
  emits('revert', { ...row, _from: 'history' })
}
</script>

<style lang="scss" scoped>
.history-table {
  height: 100%;
  .sql-cell {
    display: flex;
    justify-content: space-between;
    align-items: center;
    & > div {
      flex: 1;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
}
</style>
