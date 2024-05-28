<template>
  <section class="page dataset flex relative">
    <aside class="aside-tree" :class="{ collapsed }">
      <DirTree
        ref="dirTreeRef"
        style="padding: 16px"
        v-model:type="type"
        @select="onSelect" />
    </aside>

    <div
      class="aside-collapse-trigger"
      :class="{ collapsed }"
      @click="collapsed = !collapsed">
      <CaretLeftOutlined />
    </div>

    <main class="main flex-1 flex flex-column">
      <header class="flex justify-between align-center" style="margin-bottom: 16px">
        <div>
          <span>{{ typeLabel }}</span>
          <div
            class="select-tag"
            style="margin-left: 10px"
            v-if="typeof dirInfo.id === 'number'">
            {{ dirInfo.name }}

            <CloseOutlined
              class="pointer"
              style="margin-left: 10px"
              @click="onSelectClear" />
          </div>
        </div>

        <a-space>
          <span style="color: #bfbfbf">
            <ExclamationCircleFilled style="color: #1890ff; vertical-align: -2px" />
            {{ versionJs.ViewsDashboard.keywordSearchLeftTip }}
          </span>

          <a-input-search
            style="width: 240px"
            placeholder="输入关键字搜索"
            allow-clear
            v-model:value="keyword"
            @search="onSearch" />

          <a-button
            v-permission="'DASHBOARD:VIEW:CREATE'"
            type="primary"
            style="margin-left: 8px"
            @click="toCreate">
            新建看板
          </a-button>
        </a-space>
      </header>

      <a-table
        class="list-table"
        size="small"
        :loading="loading"
        :columns="columns"
        :dataSource="list"
        :pagination="pager"
        :scroll="{ x: 1200, y: 'auto' }"
        :row-class-name="setRowClassName"
        @change="onTableChange">
        <template #emptyText>
          <a-empty :description="type === 'ALL' ? '暂无数据' : ''" />
          <div v-if="type === 'PERSONAL'">
            暂无数据，
            <a-button
              type="primary"
              size="small"
              style="margin-right: 4px"
              @click="jumpToAll">
              跳转公共
            </a-button>
            列表查看更多数据
          </div>
        </template>

        <template #bodyCell="{ text, column, record }">
          <template v-if="column.dataIndex === 'name'">
            <a-dropdown
              v-if="column.dataIndex === 'name'"
              :trigger="['contextmenu']">
              <a
                class="row--name"
                target="_blank"
                :href="
                  hasReadPermission(record) ? getPreviewlHref(record) : undefined
                ">
                {{ text }}
              </a>

              <template
                #overlay
                v-if="
                  hasReadPermission(record) ||
                  hasWritePermission(record) ||
                  hasManagePermission(record)
                ">
                <a-menu @click="e => onMeunClick(e, record)">
                  <template v-if="hasReadPermission(record)">
                    <a-menu-item key="_self"> 当前页面打开 </a-menu-item>
                    <a-menu-item key="_blank"> 新页面打开 </a-menu-item>
                  </template>

                  <a-menu-item v-if="hasWritePermission(record)" key="edit">
                    编辑
                  </a-menu-item>

                  <template v-if="hasManagePermission(record)">
                    <a-menu-item v-if="record.status === 'UN_PUBLISH'" key="publish">
                      发布
                    </a-menu-item>
                    <a-menu-item v-if="record.status === 'ONLINE'" key="offline">
                      下线
                    </a-menu-item>
                    <a-menu-item key="share"> 共享 </a-menu-item>
                  </template>
                </a-menu>
              </template>
            </a-dropdown>
          </template>

          <template v-if="column.dataIndex === 'reportCount'">
            <span v-if="!text">-</span>
            <a-popover
              v-else
              placement="rightTop"
              overlayClassName="chartNamesList-popover">
              <a>{{ text }}</a>

              <template #content>
                <div class="chart-list" style="min-width: 120px">
                  <a
                    class="chart-item"
                    v-for="(t, i) in record.reportNames"
                    :key="i"
                    :title="t">
                    {{ t }}
                  </a>
                </div>
              </template>
            </a-popover>
          </template>

          <template v-if="column.dataIndex === 'action'">
            <a-space class="row--action">
              <a-tooltip title="收藏">
                <a-button
                  size="small"
                  type="text"
                  :style="{ color: record.favorite ? '#e6a23c' : '' }"
                  :icon="h(record.favorite ? StarFilled : StarOutlined)"
                  @click="favor(record)" />
              </a-tooltip>

              <a-tooltip v-if="hasWritePermission(record)" title="编辑">
                <a-button
                  size="small"
                  type="text"
                  :icon="h(EditOutlined)"
                  @click="edit(record)" />
              </a-tooltip>

              <a-dropdown
                v-if="hasManagePermission(record) || isPersonal"
                trigger="click">
                <a-button size="small" type="text" :icon="h(MoreOutlined)" />

                <template #overlay>
                  <a-menu @click="e => onMeunClick(e, record)">
                    <a-menu-item
                      v-if="hasManagePermission(record) || isPersonal"
                      key="move"
                      >移动至
                    </a-menu-item>

                    <template v-if="hasManagePermission(record)">
                      <a-menu-item key="share">共享 </a-menu-item>
                      <a-menu-item
                        key="publish"
                        v-if="record.status === 'UN_PUBLISH'">
                        发布
                      </a-menu-item>
                      <a-menu-item key="offline" v-if="record.status === 'ONLINE'">
                        下线
                      </a-menu-item>
                      <a-menu-item key="online" v-if="record.status === 'OFFLINE'">
                        上线
                      </a-menu-item>
                      <ViewsDashboardActionDropdownMenuItemPushSetting />
                      <a-menu-item key="delete" style="color: red">删除</a-menu-item>
                    </template>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </main>
  </section>

  <!-- 共享 -->
  <ShareDrawer
    v-model:open="shareDrawerOpen"
    :initData="rowInfo"
    @visibility-change="onVisibilityChange" />

  <!-- 移动 -->
  <MoveDrawer
    v-model:open="moveDrawerOpen"
    :init-data="rowInfo"
    :init-params="{ position: 'DASHBOARD', type, workspaceId }"
    @ok="onMoveOk" />

  <!-- 推送设置 -->
  <ViewsDashboardPushSettingDrawer
    v-model:open="pushSettingDrawerOpen"
    :initData="rowInfo" />

  <!-- 搜索页面 -->
  <SearchPageDrawer
    v-model:open="searchPageDrawerOpen"
    :initParams="searchPageParams"
    @close="onSearchPageDrawerClose" />
</template>
<script setup>
import { h, ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { tableColumns } from './config'
import {
  StarFilled,
  StarOutlined,
  EditOutlined,
  MoreOutlined,
  ExclamationCircleFilled,
  CaretLeftOutlined,
  CloseOutlined,
} from '@ant-design/icons-vue'
import DirTree from '@/components/DirTree'
import ShareDrawer from './components/ShareDrawer.vue'
import SearchPageDrawer from './SearchPageDrawer.vue'
import { MoveDrawer } from '@/components/DirTree'
import { getDashboardList } from '@/apis/dashboard'
import useMenus from './useMenus.js'
import useTable from 'common/hooks/useTable'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import { versionJs, versionVue } from '@/versions'

const {
  ViewsDashboardActionDropdownMenuItemPushSetting,
  ViewsDashboardPushSettingDrawer,
} = versionVue

const { getPreviewlHref, openWindow, edit, favor, publish, offline, online, del } =
  useMenus()

const router = useRouter()
const route = useRoute()

const appStore = useAppStore()
const userStore = useUserStore()

// 查看权限
const hasReadPermission = row => {
  if (userStore.hasPermission('DASHBOARD:READ:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('DASHBOARD:READ:HAS:PRIVILEGE')) {
    return row.permission === 'READ' || row.permission === 'WRITE'
  } else {
    return false
  }
}

// 编辑权限
const hasWritePermission = row => {
  if (userStore.hasPermission('DASHBOARD:WRITE:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('DASHBOARD:WRITE:CREATE')) {
    return row.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('DASHBOARD:WRITE:HAS:PRIVILEGE')) {
    return row.permission === 'WRITE'
  } else {
    return false
  }
}

// 管理权限（发布，移动，下线，删除，共享，推送）
const hasManagePermission = row => {
  if (userStore.hasPermission('DASHBOARD:MANAGE:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('DASHBOARD:MANAGE:CREATE')) {
    return row.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('DASHBOARD:MANAGE:HAS:PRIVILEGE')) {
    return row.permission === 'WRITE'
  } else {
    return false
  }
}

// 空间ID
const workspaceId = computed(() => appStore.workspaceId)
watch(workspaceId, () => {
  reset()
  fetchList()
})

// 重置
const reset = () => {
  dirInfo.value = {}
  keyword.value = ''
  pager.current = 1
}

const collapsed = ref(false) // 侧边文件夹收缩
const type = ref('ALL') // 文件夹类型
const isPersonal = computed(() => type.value === 'PERSONAL')
const typeLabel = computed(() => {
  return type.value === 'ALL' ? '公共' : '我的'
})

// 清空选中
const dirTreeRef = ref(null)
const onSelectClear = () => {
  dirTreeRef.value.set()
}

const dirInfo = ref({}) // 文件夹信息
const onSelect = node => {
  dirInfo.value = { ...node }

  fetchList()
}

const toCreate = () => {
  const routeRes = router.resolve({ name: 'DashboardModify' })
  if (!routeRes) return

  window.open(routeRes.href, '_blank')
}

// 额外的查询参数
const initQueryParams = computed(() => {
  return {
    workspaceId: workspaceId.value,
    folderType: type.value,
    folderId: dirInfo.value.id,
  }
})

const { loading, keyword, sorter, pager, list, fetchList } = useTable(
  getDashboardList,
  {
    sorter: { field: 'updateTime', order: 'descend' },
    initQueryParams,
  }
)

const setRowClassName = row => {
  return hasReadPermission(row) ? '' : 'no-permission'
}

const jumpToAll = () => {
  type.value = 'ALL'
  dirTreeRef.value.set()
}

const columns = computed(() => {
  const { field, order } = sorter.value || {}

  return tableColumns.map(col => {
    return {
      ...col,
      sortOrder: col.dataIndex === field && order,
    }
  })
})

// 关键字参数
const searchPageParams = ref({})
const searchPageDrawerOpen = ref(false)
const onSearch = (e = '') => {
  const str = e.trim()

  if (!str.length) {
    fetchList()
  } else {
    searchPageParams.value.keyword = str
    searchPageDrawerOpen.value = true
  }
}
const onSearchPageDrawerClose = () => {
  keyword.value = ''
}

// 表格事件
const onTableChange = (p = {}, filters, sorts) => {
  const { current, pageSize } = p

  if (pageSize !== pager.pageSize) {
    pager.current = 1
    pager.pageSize = pageSize
  } else {
    pager.current = current
  }

  sorter.value = sorts

  fetchList()
}

onMounted(() => {
  if (route.query.folder) return
  fetchList()
})

const onMeunClick = async ({ key }, row) => {
  switch (key) {
    case '_self':
      openWindow(row)
      break
    case '_blank':
      openWindow(row, true)
      break
    case 'edit':
      edit(row)
      break
    case 'publish':
      publish(row)
      break
    case 'offline':
      await offline(row)
      fetchList()
      break
    case 'online':
      online(row)
      break
    case 'delete':
      del(row, fetchList)
      break
    case 'share':
      share(row)
      break
    case 'move':
      move(row)
      break
    case 'pushSetting':
      setting(row)
      break
    default:
  }
}

// tableRow 信息
const rowInfo = ref({})

// 共享
const shareDrawerOpen = ref(false)
const share = row => {
  rowInfo.value = { ...row }
  shareDrawerOpen.value = true
}
const onVisibilityChange = e => {
  const item = list.value.find(t => t.id === e.id)

  item.visibility = e.visibility
}

// 移动
const moveDrawerOpen = ref(false)
const move = row => {
  rowInfo.value = { ...row, folderId: dirInfo.value.id ?? row.folderId ?? -6 }
  moveDrawerOpen.value = true
}
const onMoveOk = () => {
  dirTreeRef.value.reload()
  fetchList()
}

// 推送
const pushSettingDrawerOpen = ref(false)
const setting = row => {
  rowInfo.value = { ...row }
  pushSettingDrawerOpen.value = true
}
</script>
<style scoped lang="scss">
$asideWidth: 300px;
.aside-tree {
  width: $asideWidth;
  overflow: hidden;
  border-right: 1px solid #f3f3f3;
  transition: all 0.15s;
  &.collapsed {
    width: 0;
  }
}
.aside-collapse-trigger {
  position: absolute;
  left: 0;
  top: 50%;
  padding: 12px 1px;
  background-color: #f6f7ff;
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
  transition: all 0.15s;
  transform: translateX($asideWidth);
  cursor: pointer;
  z-index: 9;
  &.collapsed {
    transform: translateX(0);
    .anticon-caret-left {
      transform: rotateY(180deg);
    }
  }
}
.main {
  padding: 10px;
  overflow: auto;
}

.select-tag {
  display: inline-flex;
  align-items: center;
  line-height: 1;
  padding: 4px 4px 4px 10px;
  border: 1px solid #c1c2c2;
  border-radius: 4px;
}

.list-table {
  :deep(.ant-table-row) {
    .row--name {
      text-decoration: underline;
    }
    .row--action {
      line-height: 1;
      font-size: 16px;
    }
    &.no-permission {
      .ant-table-cell,
      .row--name {
        color: #d9d9d9 !important;
        cursor: not-allowed;
      }

      .row--action {
        color: initial;
      }
      .expire-flag {
        position: absolute;
        top: -32px;
        left: -32px;
        border: 32px solid transparent;
        border-bottom-color: #c6c6c6;
        transform: rotate(-45deg);
        &::before {
          content: attr(data-text);
          position: absolute;
          top: 12px;
          left: -18px;
          font-size: 12px;
          color: #fff;
          font-weight: bold;
          transform: scale(0.8);
        }
      }
    }
  }
}

.chart-item {
  display: block;
  @extend .ellipsis;
}
</style>

<style lang="scss">
.chartNamesList-popover {
  .ant-popover-content {
    max-height: 600px;
  }
}
</style>
