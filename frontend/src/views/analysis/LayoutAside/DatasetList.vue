<template>
  <div class="list-wrapper">
    <a-input-search
      size="small"
      placeholder="输入查询"
      allow-clear
      v-model:value="keyword" />

    <a-spin :spinning="loading">
      <p v-if="!list.length" style="margin: 50px 0; text-align: center; color: #aaa">
        暂无数据
      </p>

      <ul class="list" v-else>
        <li
          class="item"
          v-for="item in list"
          :class="{ active: datasetId === item.id }"
          :key="item.id"
          @click="toggle(item)">
          {{ item.name }}
        </li>
      </ul>
    </a-spin>

    <div style="text-align: center">
      <a-button block size="small" type="primary"> 新建数据集 </a-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeMount } from 'vue'
import { getDatasetList } from '@/apis/dataset'
import useAppStore from '@/store/modules/app'

const appStore = useAppStore()
const workspaceId = computed(() => appStore.workspaceId)

const emits = defineEmits(['toggle'])
const props = defineProps({
  datasetId: [String, Number],
})

// 搜索关键字
const keyword = ref('')
// 所有数据集列表
const loading = ref(false)
const allList = ref([])
// 获取数据集列表
const fetchData = async () => {
  try {
    loading.value = true
    const { data = [] } = await getDatasetList({
      workspaceId: workspaceId.value,
      folderType: 'PERSONAL',
    })

    allList.value = data.filter(t => t.permission === 'READ' || t.permission === 'WRITE')
  } catch (error) {
    console.error('获取数据集列表错误', error)
  } finally {
    loading.value = false
  }
}

onBeforeMount(fetchData)

// 显示的数据集列表
const list = computed(() => {
  const s = keyword.value.trim()

  if (!s.length) return allList.value

  return allList.value.filter(
    item => item.name.includes(s) || item.id === props.datasetId
  )
})

const toggle = item => {
  emits('toggle', { ...item })
}
</script>

<style lang="scss" scoped>
.list-wrapper {
  display: flex;
  flex-direction: column;

  :deep(.ant-spin-nested-loading) {
    flex: 1;
    .ant-spin-container {
      height: 100%;
    }
  }
}

.list {
  height: inherit;
  flex: 1;
  overflow: auto;
}

.item {
  position: relative;
  padding: 0 28px 0 8px;
  line-height: 24px;
  transition: all 0.2s;
  @extend .ellipsis;
  cursor: pointer;
  &:hover {
    background-color: #f5f7fa;
  }
  &.active {
    color: #1677ff;
    font-weight: 600;
    &::after {
      content: '';
      position: absolute;
      right: 12px;
      top: 50%;
      display: table;
      width: 6px;
      height: 10px;
      border: 2px solid #1677ff;
      border-top: 0;
      border-left: none;
      opacity: 1;
      transform: rotate(45deg) scale(1) translate(-50%, -50%);
      transition: all 0.2s cubic-bezier(0.12, 0.4, 0.29, 1.46) 0.1s;
    }
  }
}
</style>
