<template>
  <a-modal title="图表" :width="650" :open @cancel="cancel">
    <p>
      {{ nameLabel }}: <b>{{ initData.name }}</b>
    </p>

    <a-table
      size="small"
      style="margin-bottom: 20px"
      :scroll="{ y: 400 }"
      :loading="loading"
      :columns="columns"
      :dataSource="list"
      :pagination="false"
    ></a-table>

    <template #footer>
      <a-button @click="toManage">管理图表</a-button>
      <a-button type="primary" @click="cancel">回到列表</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { shallowRef, ref } from 'vue'
import { getResourceReport } from '@/apis/resource/manage'

const columns = [
  { title: '图表名称', dataIndex: 'name' },
  { title: '负责人', dataIndex: 'creatorAlias', width: 160 },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 }
]

const open = defineModel('open', { type: Boolean })
const cancel = () => {
  open.value = false
}

const { initData, resourceType } = defineProps({
  initData: {
    type: Object,
    default: () => ({})
  },
  resourceType: {
    type: String
  }
})

const nameLabel = computed(() =>
  resourceType === 'DATASET'
    ? '数据集'
    : resourceType === 'DASHBOARD'
      ? '看板'
      : '名称'
)

const loading = ref(false)
const list = shallowRef([])
// 按需缓存数据，避免重新请求
const listCache = shallowRef([])
const fetchList = async () => {
  try {
    loading.value = true

    const params = { position: resourceType, id: initData.sourceId }
    const res = await getResourceReport({ ...params, pageSize: 10000 })

    queryParams.value = params
    list.value = res
  } catch (error) {
    console.error('获取图表列表失败', error)
  } finally {
    loading.value = false
  }
}

const queryParams = shallowRef({})

watch(open, e => {
  if (!e) {
    setTimeout(() => {
      listCache.value = list.value
      list.value = []
    }, 30)
    return
  }

  if (
    initData.sourceId === queryParams.value.id &&
    resourceType === queryParams.value.position
  ) {
    list.value = listCache.value
    return
  }

  fetchList()
})

const emits = defineEmits(['to-manage'])
const toManage = e => {
  cancel()
  emits('to-manage', initData)
}
</script>

<style lang="scss" scoped></style>
