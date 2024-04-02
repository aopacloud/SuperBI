<template>
  <section class="header">
    <aside class="back" @click="toBack">
      <LeftOutlined style="font-size: 20px" />
    </aside>

    <main class="header-info">
      <div class="info-left">
        <div class="info-name">
          <span>{{ chart.name }}</span>
        </div>
        <ComponentsTimeoffsetPreview
          :value="timeOffset"
          @change="e => emits('timeoffset-change', e)" />
      </div>

      <!-- 时区预览 -->

      <p-a-space class="tools">
        <a-button @click="toBack">返回</a-button>

        <a-button
          v-if="
            chart.id && (chartPermission.hasWrite() || chartPermission.hasRead())
          "
          @click="saveAs">
          另存为
        </a-button>

        <a-button
          v-if="chartPermission.hasWrite()"
          type="primary"
          :disabled="queryResponse.status !== 'SUCCESS'"
          @click="save">
          {{ chart.id ? '更新图表' : '保存图表' }}
        </a-button>

        <a-dropdown v-if="datasetPermission.hasManage()">
          <a-button :icon="h(MoreOutlined)"></a-button>
          <template #overlay>
            <a-menu @click="onMenuClick">
              <a-menu-item key="authorize">授权</a-menu-item>
              <a-menu-item key="move">移动至</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </p-a-space>
    </main>

    <!-- 数据集授权 -->
    <AuthorizeDrawer v-model:open="authorizeDrawerOpen" :initData="dataset" />

    <!-- 数据集移动 -->
    <MoveDrawer
      v-model:open="moveDrawerOpen"
      :initParams="{ position: 'DATASET', type: 'ALL', workspaceId }"
      :initData="dataset"
      @ok="onMoveOk" />

    <!-- 保存 -->
    <SaveModal
      v-model:open="modifyModalOpen"
      :initData="{ ...modifyInfo, workspaceId }"
      @ok="onModifyOk" />
  </section>
</template>

<script setup>
import { h, ref, computed, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { LeftOutlined, MoreOutlined } from '@ant-design/icons-vue'
import useAppStore from '@/store/modules/app'
import AuthorizeDrawer from '@/components/Authorize/ListDrawer.vue'
import { MoveDrawer } from '@/components/DirTree'
import SaveModal from './components/SaveModal.vue'
import { versionVue } from '@/versions'

const { ComponentsTimeoffsetPreview } = versionVue

const { requestResponse, permissions, timeOffset } = inject('index', {})
const { dataset: datasetPermission, chart: chartPermission } = permissions
const queryResponse = computed(() => requestResponse.get().response || {})

const emits = defineEmits(['update-chart', 'update-dataset', 'timeoffset-change'])
const props = defineProps({
  chart: {
    type: Object,
    default: () => ({}),
  },
  dataset: {
    type: Object,
    default: () => ({}),
  },
})

const appStore = useAppStore()
const workspaceId = computed(() => appStore.workspaceId)

const route = useRoute()
const router = useRouter()
const toBack = () => {
  const { from } = route.query

  if (!from) {
    router.replace({ name: 'DatasetList' })
  } else {
    router.replace({ name: from })
  }
}

const onMenuClick = ({ key }) => {
  if (key === 'authorize') {
    authorize()
  } else if (key === 'move') {
    move()
  }
}

const modifyModalOpen = ref(false)
const modifyInfo = ref({})
const save = () => {
  modifyModalOpen.value = true
  modifyInfo.value = { ...props.chart }
}
const saveAs = () => {
  modifyModalOpen.value = true
  modifyInfo.value = { ...props.chart, id: undefined }
}
const onModifyOk = e => {
  emits('update-chart', e)
}

// 授权
const authorizeDrawerOpen = ref(false)
const authorize = () => {
  authorizeDrawerOpen.value = true
}

// 移动
const moveDrawerOpen = ref(false)
const move = () => {
  moveDrawerOpen.value = true
}
const onMoveOk = e => {
  emits('update-dataset', { folderId: e.folderId })
}
</script>

<style lang="scss" scoped>
$border-color: #eee;
.header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid $border-color;
  .back {
    padding: 10px;
    border-right: 1px solid $border-color;
    cursor: pointer;
    transition: all 0.2s;
    &:hover {
      background-color: rgba(0, 0, 0, 0.04);
    }
  }
}
.header-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  background: #fff;
  padding: 5px 10px;
  .info-left {
    display: flex;
    align-items: center;
  }
  .info-name {
    margin-right: 12px;
  }

  .tools {
    display: inline-flex;
    align-items: center;
  }
  .tool-item {
    margin-left: 8px;
  }
}
</style>
