<template>
  <a-input-search
    class="table-keyword"
    style="width: 300px"
    placeholder="请输入搜索"
    allow-clear
    v-model:value="keyword"
    @search="fetchData" />

  <a-table :loading="loading" :columns="dataSourceListTableColumns" :dataSource="list">
    <template #bodyCell="{ text, column, record }">
      <a v-if="column.dataIndex === 'datasetCount'" @click="showDatasetModal(record)">
        {{ text }}
      </a>

      <a-space v-if="column.dataIndex === 'action'">
        <a-tooltip title="创建数据集">
          <a-button
            size="small"
            type="text"
            :icon="h(AppstoreAddOutlined)"
            @click="toDatasetCreate(record)" />
        </a-tooltip>
        <a-tooltip title="数据表详情">
          <a-button
            size="small"
            type="text"
            :icon="h(ProfileOutlined)"
            @click="showTableDetailModal(record)" />
        </a-tooltip>
      </a-space>
    </template>
  </a-table>

  <!-- 数据集modal -->
  <DatasetModal :datasourceId="id" :initData="rowInfo" v-model:open="datasetModalOpen" />

  <!-- 表详情modal -->
  <TableDetailModal
    :datasourceId="id"
    :initData="rowInfo"
    v-model:open="tableDetailModalOpen" />
</template>

<script setup>
import { h, ref, shallowRef, watch } from 'vue'
import { useRouter } from 'vue-router'
import { AppstoreAddOutlined, ProfileOutlined } from '@ant-design/icons-vue'
import { getDatasourceTableById } from '@/apis/datasource'
import { dataSourceListTableColumns } from './config'
import DatasetModal from './components/DatasetModal.vue'
import TableDetailModal from './components/TableDetailModal.vue'

const router = useRouter()
const props = defineProps({
  id: {
    type: Number,
    default: 1,
  },
})

const keyword = ref('')
const loading = ref(false)
const list = shallowRef([])

const fetchData = async () => {
  try {
    loading.value = true
    const res = await getDatasourceTableById(props.id)

    const kw = keyword.value.trim()
    list.value = !kw ? res : res.filter(t => t.tableName.includes(kw))
  } catch (error) {
    console.error('获取数据列表失败', error)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.id,
  () => {
    list.value = []
    fetchData()
  },
  { immediate: true }
)

const toDatasetCreate = ({ datasourceName, tableName }) => {
  const routeRes = router.resolve({
    name: 'DatasetModify',
    query: { db: datasourceName, tb: tableName },
  })
  if (!routeRes) return

  window.open(routeRes.href, '_blank')
}

const rowInfo = ref({})

const datasetModalOpen = ref(false)
const showDatasetModal = row => {
  rowInfo.value = { datasourceId: props.id, ...row }
  datasetModalOpen.value = true
}

const tableDetailModalOpen = ref(false)
const showTableDetailModal = row => {
  rowInfo.value = { datasourceId: props.id, ...row }
  tableDetailModalOpen.value = true
}
</script>

<style lang="scss" scoped>
.table-keyword {
  position: absolute;
  top: -55px;
  right: 0;
}
</style>
