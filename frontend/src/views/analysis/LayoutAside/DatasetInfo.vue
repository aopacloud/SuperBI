<template>
  <section class="info">
    <header class="info-header">
      <span class="label">数据集</span>

      <div class="creator">{{ dataset.creatorAlias || '-' }}</div>
    </header>

    <main class="info-main" style="justify-content: initial">
      <div class="name" :title="dataset.name">
        {{ dataset.name || '-' }}
      </div>

      <a-tooltip
        v-if="dataset.description || dataset.docUrl"
        :title="dataset.description"
      >
        <a
          class="doc"
          target="_blank"
          :href="dataset.docUrl ? dataset.docUrl : null"
        >
          <InfoCircleOutlined />
        </a>
      </a-tooltip>

      <a-dropdown trigger="click" v-model:open="dropdownOpen">
        <EllipsisOutlined
          style="margin-left: auto"
          @click="dropdownOpen = true"
        />

        <template #overlay>
          <a-menu @click="onMenuClick">
            <a-menu-item v-if="hasDatasetEditPermission" key="edit">
              编辑数据集
            </a-menu-item>

            <a-menu-item key="togle" style="padding: 0">
              <a-popover
                trigger="click"
                placement="rightTop"
                v-model:open="datasetListOpen"
              >
                <div
                  class="flex justify-between align-center"
                  style="padding: 5px 5px 5px 12px"
                >
                  切换数据集
                  <RightOutlined style="margin-left: 4px" />
                </div>
                <template #content>
                  <DatasetList
                    ref="datasetListRef"
                    :dataset-id="dataset.id"
                    @toggle="onToggle"
                    @upload="onUpload"
                  />
                </template>
              </a-popover>
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </main>

    <DatasetUploadModal
      :mode="uploadMode"
      :id="uploadId"
      v-model:open="uploadOpen"
      @ok="onUploadOk"
    />
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  InfoCircleOutlined,
  EllipsisOutlined,
  RightOutlined
} from '@ant-design/icons-vue'
import DatasetList from './DatasetList.vue'
import DatasetUploadModal from '@/components/DatasetUploadModal/index.vue'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const emits = defineEmits(['dataset-toggled'])
const props = defineProps({
  chart: {
    type: Object,
    default: () => ({})
  },
  dataset: {
    type: Object,
    default: () => ({})
  }
})

// 数据集编辑权限
const hasDatasetEditPermission = computed(() => {
  if (userStore.hasPermission('DATASET:WRITE:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('DATASET:WRITE:CREATE')) {
    return props.dataset.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('DATASET:WRITE:HAS:PRIVILEGE')) {
    return props.dataset.permission === 'WRITE'
  } else {
    return false
  }
})

const dropdownOpen = ref(false)

const edit = () => {
  // chart.id || dataset.id 图表详情id或数据集id
  const routeRes = router.resolve({
    name: 'DatasetModify',
    query: { from: route.name, reportId: props.chart.id || props.dataset.id },
    params: { id: props.dataset.id }
  })
  if (!routeRes) return

  window.open(routeRes.href, '_blank')
}
const onMenuClick = ({ key }) => {
  if (key === 'edit') {
    if (props.dataset.upload) {
      onDatasetListClicked()
      uploadMode.value = 'EDIT'
      uploadId.value = props.dataset.id
      uploadOpen.value = true
    } else {
      edit()
    }
  }
}

const datasetListOpen = ref(false)
const onDatasetListClicked = () => {
  datasetListOpen.value = false
  dropdownOpen.value = false
}
// 切换数据集
const onToggle = payload => {
  if (payload.id === props.dataset.id) return
  onDatasetListClicked()
  emits('dataset-toggled', payload)
}

const uploadOpen = ref(false)
const uploadMode = ref()
const uploadId = ref()
const onUpload = () => {
  onDatasetListClicked()
  uploadMode.value = 'CREATE'
  uploadId.value = undefined
  uploadOpen.value = true
}
const onUploadOk = datasetId => {
  emits('dataset-toggled', { id: datasetId })
}
</script>

<style scoped lang="scss">
.info {
  padding: 10px;
}
.info-header,
.info-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 17px;
}
.info-header {
  font-size: 12px;
  .label {
    font-weight: 600;
  }
  .creator {
    font-size: 12px;
    color: #999;
  }
}
.info-main {
  margin-top: 12px;
  .name {
    font-size: 12px;
    color: #606060;
    @extend .ellipsis;
  }
  .doc {
    margin: 0 10px 0 6px;
    font-size: 14px;
  }
}
</style>
