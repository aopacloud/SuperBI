<template>
  <section class="header">
    <aside class="back" @click="toBack">
      <LeftOutlined />
    </aside>
    <main class="content">
      <a-space class="left">
        <span>
          {{ detail.name }}
          <a-tooltip v-if="detail.description" :title="detail.description">
            <InfoCircleOutlined />
          </a-tooltip>
        </span>
        <!-- 编辑 -->
        <a-button
          v-if="hasWritePermission && mode === 'EDIT'"
          size="small"
          type="text"
          :icon="h(EditOutlined)"
          @click="modifyOpen" />
        <!-- 时区预览 -->
        <ComponentsTimeoffsetPreview
          :value="timeOffset"
          @change="e => emits('timeoffset-change', e)" />
        <!-- 创建人 -->
        <div class="creator">
          <span style="color: #999">创建者:</span>
          {{ detail.creatorAlias }}
        </div>
      </a-space>
      <div class="right tools" style="margin-left: auto">
        <a-space>
          <a-badge
            v-if="
              mode === 'READONLY' && detail.lastEditVersion > (detail.version || 0)
            "
            color="blue"
            text="有保存的最新版本，可点击“编辑”前往查看" />

          <a-badge v-if="mode === 'EDIT'" v-bind="displayStatusProps()" />

          <ViewsDashboardLHeaderGlobalDate
            v-if="mode !== 'EDIT'"
            :utcOffset="timeOffset"
            :globalDateConfig="globalDateConfig"
            @reset="resetGlobalDate">
          </ViewsDashboardLHeaderGlobalDate>

          <AutoRefresh
            :detail="detail"
            :writeable="hasWritePermission && mode === 'EDIT'" />

          <a-tooltip title="图表">
            <div class="tools-item" @click="toReport">
              <AreaChartOutlined />
            </div>
          </a-tooltip>

          <a-tooltip title="全局筛选" v-if="mode === 'EDIT'">
            <div class="tools-item" @click="emits('filter')">
              <FilterOutlined />
            </div>
          </a-tooltip>

          <template v-if="hasWritePermission">
            <a-tooltip title="编辑" v-if="mode === 'READONLY'">
              <div class="tools-item" @click="emits('edit')">
                <EditOutlined />
              </div>
            </a-tooltip>

            <a-tooltip title="管理看板内容" v-if="mode === 'EDIT'">
              <div class="tools-item" @click="emits('manage')">
                <AppstoreOutlined />
              </div>
            </a-tooltip>

            <template v-if="mode === 'EDIT' || mode === 'PREVIEW'">
              <keep-alive>
                <a-button
                  v-if="mode === 'EDIT'"
                  size="small"
                  @click="emits('update:mode', 'PREVIEW')">
                  预览
                </a-button>
                <a-button v-else size="small" @click="emits('update:mode', 'EDIT')">
                  编辑
                </a-button>
              </keep-alive>
            </template>
          </template>

          <!-- template 会触发 [Vue warn]: Duplicate keys found during update: -1 Make sure keys are unique -->

          <template v-if="hasWritePermission && mode === 'EDIT'">
            <a-button
              size="small"
              :loading="saveLoading"
              :disabled="publishLoading"
              @click="save">
              保存
            </a-button>
            <a-button
              v-if="hasManagePermission"
              size="small"
              type="primary"
              :loading="publishLoading"
              :disabled="saveLoading"
              @click="rePublish">
              {{ !detail.id ? '提交并发布' : '重新发布' }}
            </a-button>
          </template>

          <!-- 更多操作 -->
          <a-dropdown
            v-if="hasManagePermission"
            trigger="click"
            :loading="actionLoading">
            <div class="tools-item">
              <keep-alive>
                <LoadingOutlined v-if="actionLoading" />
                <MoreOutlined v-else />
              </keep-alive>
            </div>

            <template #overlay>
              <a-menu @click="onMenuClick">
                <a-menu-item key="share">共享</a-menu-item>
                <a-menu-item key="move">移动至</a-menu-item>
                <a-menu-item v-if="detail.status === 'ONLINE'" key="offline">
                  下线
                </a-menu-item>
                <ViewsDashboardActionDropdownMenuItemPushSetting />
                <a-menu-item
                  v-if="detail.status === 'UN_PUBLISH' && mode == 0"
                  key="publish">
                  发布
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </div>
    </main>
  </section>

  <!-- 看板信息编辑 -->
  <ModifyModal
    v-model:open="modifyModalOpen"
    :detail="detail"
    :init-params="{ position: 'DASHBOARD', type: 'ALL', workspaceId }"
    @ok="onModifyOk" />

  <!-- 分享 -->
  <ShareDrawer
    v-model:open="shareDrawerOpen"
    :initData="detail"
    @visibility-change="onVisibilityChange" />

  <!-- 移动 -->
  <MoveDrawer
    v-model:open="moveDrawerOpen"
    :init-data="detail"
    :init-params="{ position: 'DASHBOARD', type: 'ALL', workspaceId }"
    @ok="onMoveOk" />

  <!-- 推送设置 -->
  <ViewsDashboardPushSettingDrawer
    v-model:open="pushSettingDrawerOpen"
    :initData="detail" />
</template>

<script setup lang="jsx">
import { h, ref, computed, watch, inject } from 'vue'
import { useRouter } from 'vue-router'
import {
  LeftOutlined,
  AreaChartOutlined,
  FilterOutlined,
  EditOutlined,
  MoreOutlined,
  AppstoreOutlined,
  LoadingOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons-vue'
import useAppStore from '@/store/modules/app'
import ModifyModal from './components/ModifyModal.vue'
import AutoRefresh from './components/AutoRefresh.vue'
import ShareDrawer from '@/views/dashboard/components/ShareDrawer.vue'
import { MoveDrawer } from '@/components/DirTree'
import {
  postSave,
  putUpdate,
  postPublishById,
  postOfflineById,
} from '@/apis/dashboard'
import { message } from 'ant-design-vue'
import { deepCloneByJson } from '@/common/utils/help'
import useUserStore from '@/store/modules/user'
import { versionVue } from '@/versions'
import { moveDirectory } from '@/apis/directory'

const {
  ComponentsTimeoffsetPreview,
  ViewsDashboardLHeaderGlobalDate,
  ViewsDashboardActionDropdownMenuItemPushSetting,
  ViewsDashboardPushSettingDrawer,
} = versionVue

const emits = defineEmits([
  'edit',
  'manage',
  'filter',
  'preview',
  'save',
  'publish',
  'republish',
  'global-date-change',
  'update-detail',
  'published',
  'update:mode',
  'timeoffset-change',
])
const props = defineProps({
  // 看板详情
  detail: {
    type: Object,
    default: () => ({}),
  },
  mode: {
    type: String,
    default: 0,
    validator: t => {
      // READONLY 看板详情; EDIT 看板编辑; PREVIEW 编辑预览
      return ['READONLY', 'EDIT', 'PREVIEW'].includes(t)
    },
  },
  // 看板组件
  layoutComponents: {
    type: Array,
    default: () => [],
  },
  // 全局日期筛选
  globalDateConfig: {
    type: Object,
  },
  needBeforeInit: {
    type: Boolean,
  },
})

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const workspaceId = computed(() => props.detail.workspaceId || appStore.workspaceId)

// 编辑权限
const hasWritePermission = computed(() => {
  if (userStore.hasPermission('DASHBOARD:WRITE:ALL:WORKSPACE')) {
    return true
  } else if (!props.detail.id) {
    return userStore.hasPermission('DASHBOARD:VIEW:CREATE')
  } else if (userStore.hasPermission('DASHBOARD:WRITE:CREATE')) {
    return props.detail.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('DASHBOARD:WRITE:HAS:PRIVILEGE')) {
    return props.detail.permission === 'WRITE'
  } else {
    return false
  }
})

// 管理权限（发布，移动，下线，删除，共享，推送）
const hasManagePermission = computed(() => {
  if (userStore.hasPermission('DASHBOARD:MANAGE:ALL:WORKSPACE')) {
    return true
  } else if (!props.detail.id) {
    return userStore.hasPermission('DASHBOARD:VIEW:CREATE')
  } else if (userStore.hasPermission('DASHBOARD:MANAGE:CREATE')) {
    return props.detail.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('DASHBOARD:MANAGE:HAS:PRIVILEGE')) {
    return props.detail.permission === 'WRITE'
  } else {
    return false
  }
})

watch(
  () => props.needBeforeInit,
  b => {
    if (b) {
      modifyModalOpen.value = true
    }
  }
)

const toBack = () => {
  router.replace({ name: 'DashboardList' })
}

// 时区偏移
const { timeOffset } = inject('index')

const displayStatusProps = () => {
  const { status, version = 0, lastEditVersion = 0 } = props.detail
  let color = '',
    text = ''

  if (status === 'UNPUBLISH') {
    color = 'grey'
    text = '未发布'
  }
  if (status === 'ONLINE' && lastEditVersion === version) {
    color = 'green'
    text = '已发布'
  }
  if (status === 'ONLINE' && lastEditVersion > version) {
    color = 'blue'
    text = '已保存未发布'
  }
  if (status === 'OFFLINE') {
    color = 'grey'
    text = '已下线'
  }

  return {
    color,
    text,
  }
}

const toReport = () => {
  const routeRes = router.resolve({ name: 'ReportList' })
  if (!routeRes) return

  window.open(routeRes.href, '_blank')
}

const resetGlobalDate = () => {
  props.globalDateConfig.offset = []
  props.globalDateConfig.date = []
}

const modifyModalOpen = ref(false)
const modifyOpen = () => {
  modifyModalOpen.value = true
}
const onModifyOk = e => {
  emits('update-detail', e)
}

// 共享
const shareDrawerOpen = ref(false)
const onVisibilityChange = e => {
  props.detail.visibility = e.visibility
}

// 移动
const moveDrawerOpen = ref(false)
const onMoveOk = e => {
  props.detail.folderId = e.folderId
}

// 设置
const pushSettingDrawerOpen = ref(false)

const onMenuClick = ({ key }) => {
  switch (key) {
    case 'share':
      shareDrawerOpen.value = true
      break
    case 'move':
      moveDrawerOpen.value = true
      break
    case 'pushSetting':
      pushSettingDrawerOpen.value = true
      break
    case 'offline':
      offline()
      break
    case 'publish':
      publish()
      break
    default:
      break
  }
}

// 更多操作的loading
const actionLoading = ref(false)
// 下线
const offline = async () => {
  try {
    actionLoading.value = true

    await postOfflineById(props.detail.id)

    emits('update-detail', { status: 'OFFLINE' })
    message.success('下线成功')
  } catch (error) {
    console.error('下线错误', error)
  } finally {
    actionLoading.value = false
  }
}
// 发布
const publish = async () => {
  try {
    actionLoading.value = true

    const { status } = await postPublishById(props.detail.id)

    emits('update-detail', { status })
    message.success('发布成功')
  } catch (error) {
    console.error('发布错误', error)
  } finally {
    actionLoading.value = false
  }
}

const validate = () => {
  if (!props.detail.name) {
    message.warn('看板名称不能为空')

    return false
  }

  return true
}

const transformPayload = (list = []) => {
  return deepCloneByJson(list).map(item => {
    delete item._loaded

    const { i, type, content, ...layout } = item

    delete content.report

    return {
      i,
      type,
      reportId: type === 'REPORT' ? content.id : undefined,
      content: JSON.stringify(content),
      layout: JSON.stringify({ ...layout, i }),
    }
  })
}

const move = dId => {
  try {
    const payload = {
      position: 'DASHBOARD',
      type: 'ALL',
      workspaceId: workspaceId.value,
      targetId: dId,
      folderId: props.detail.folderId,
    }

    return moveDirectory(payload)
  } catch (error) {
    console.error('更新文件夹位置失败', error)
  }
}

const saveLoading = ref(false)
const save = async () => {
  if (!validate()) return Promise.reject('校验错误')

  try {
    saveLoading.value = true

    const payload = {
      ...props.detail,
      workspaceId: workspaceId.value,
      dashboardComponents: transformPayload(props.layoutComponents),
    }

    const fn = !props.detail.id
      ? () => postSave(payload)
      : () => putUpdate(props.detail.id, payload)
    const { id, version, status, lastEditVersion, permission, folder } = await fn()

    emits('update-detail', { id, version, status, lastEditVersion, permission })
    message.success('保存成功')

    // 当前位置与保存的位置不一致
    if (folder.id !== props.detail.folderId) move(id)
  } catch (error) {
    console.error('看板保存错误', error)
    return Promise.reject()
  } finally {
    saveLoading.value = false
  }
}

const publishLoading = ref(false)
const rePublish = async () => {
  try {
    publishLoading.value = true

    await save()
    const { version, status, lastEditVersion } = await postPublishById(
      props.detail.id
    )

    emits('update-detail', { version, status, lastEditVersion })
    emits('published', { version, status, lastEditVersion })
    message.success('发布成功')
  } catch (error) {
    console.error('重新发布错误', error)
  } finally {
    publishLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
$height: 40px;
.header {
  position: relative;
  display: flex;
  align-items: center;
  height: $height;
  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.back {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: $height;
  width: $height;
  cursor: pointer;
  &:hover {
    background-color: lighten(#1677ff, 40%);
  }
}

.content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  padding: 0 8px;
}
.left {
  display: inline-flex;
  align-items: center;
}

.tools-item,
:deep(.tools-item) {
  padding: 4px 6px;
  border-radius: 4px;
  transition: all 0.2s;
  color: #333;
  font-size: 16px;
  cursor: pointer;
  &:hover {
    background-color: lighten(#1677ff, 35%);
  }
  &.active {
    background-color: #1677ff;
    color: #fff;
  }
}

.date-label {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  line-height: 1;
  background-color: #e4f2ff;
  border-radius: 2px;
  color: #666;
  &:hover > .clear {
    visibility: visible;
  }
  &-text {
    padding: 0 6px 0 8px;
  }
  & > .clear {
    visibility: hidden;
    margin-left: 4px;
  }
}
</style>
