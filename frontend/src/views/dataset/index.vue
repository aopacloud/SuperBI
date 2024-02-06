<template>
  <section class="page dataset flex relative">
    <aside class="aside-tree" :class="{ collapsed }">
      <DirTree
        ref="dirTreeRef"
        style="padding: 16px"
        position="DATASET"
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
      <div class="flex justify-between align-center" style="margin-bottom: 16px">
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
            数据集权限可点击右侧操作列中的图标进行申请
          </span>

          <a-input-search
            style="width: 240px"
            placeholder="输入关键字搜索"
            allow-clear
            v-model:value="keyword"
            @search="onSearch" />

          <a-button v-permission="'DATASET:VIEW:CREATE'" type="primary" @click="toCreate">
            新建数据集
          </a-button>
        </a-space>
      </div>

      <a-table
        class="list-table"
        size="small"
        rowKey="id"
        :loading="loading"
        :columns="columns"
        :data-source="list"
        :scroll="{ x: 1200, y: 'auto' }"
        :pagination="pager"
        :row-class-name="setRowClassName"
        @change="onTableChange">
        <template #emptyText>
          <a-empty :description="!isPersonal ? '暂无数据' : ''" />
          <div v-if="isPersonal">
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
        <template #bodyCell="{ text, record, column }">
          <template v-if="column.dataIndex === 'name'">
            <span
              v-if="record.permission === 'EXPIRED'"
              class="expire-flag"
              data-text="已过期"></span>

            <a-dropdown :trigger="['contextmenu']">
              <a class="row--name" :href="getAnalysisHref(record)" target="_blank">
                {{ text }}
              </a>
              <template #overlay>
                <a-menu @click="e => onMenuClick(e, record)">
                  <template v-if="hasAnalysisPermission(record)">
                    <a-menu-item key="_self">当前页面打开</a-menu-item>
                    <a-menu-item key="_blank">新页面打开</a-menu-item>
                  </template>

                  <a-menu-item v-if="hasWritePermission(record)" key="edit">
                    编辑
                  </a-menu-item>

                  <template v-if="hasManagePermission(record)">
                    <a-menu-item key="authorize">授权</a-menu-item>
                    <a-menu-item
                      v-if="record.status === 'UN_PUBLISH' || record.status === 'OFFLINE'"
                      key="publish">
                      发布
                    </a-menu-item>
                    <a-menu-item v-if="record.status === 'ONLINE'" key="offline">
                      下线
                    </a-menu-item>
                    <a-menu-item key="export">导出</a-menu-item>
                  </template>
                </a-menu>
              </template>
            </a-dropdown>
          </template>

          <template v-if="column.dataIndex === 'action'">
            <a-space class="row--action">
              <a-tooltip
                v-if="
                  record.enableApply === 1 &&
                  (record.permission === 'NONE' || record.permission === 'EXPIRED') &&
                  !record.applying
                "
                title="申请">
                <a-button
                  size="small"
                  type="text"
                  :icon="h(LockOutlined)"
                  @click="apply(record)" />
              </a-tooltip>

              <a-tooltip title="收藏">
                <a-button
                  size="small"
                  type="text"
                  :style="{ color: record.favorite ? '#e6a23c' : '' }"
                  :icon="h(record.favorite ? StarFilled : StarOutlined)"
                  @click="favor(record)" />
              </a-tooltip>

              <a-tooltip v-if="hasAnalysisPermission(record)" title="分析">
                <a-button
                  size="small"
                  type="text"
                  :icon="h(LineChartOutlined)"
                  @click="toAnalysis(record, true)" />
              </a-tooltip>

              <a-dropdown
                v-if="
                  hasWritePermission(record) || hasManagePermission(record) || isPersonal
                "
                :trigger="['click']">
                <a-button size="small" type="text" :icon="h(MoreOutlined)" />

                <template #overlay>
                  <a-menu @click="e => onMenuClick(e, record)">
                    <a-menu-item v-if="hasWritePermission(record)" key="edit">
                      编辑
                    </a-menu-item>

                    <template v-if="hasManagePermission(record) || isPersonal">
                      <template v-if="hasManagePermission(record)">
                        <a-menu-item key="authorize">授权</a-menu-item>
                        <a-menu-item key="setting">设置</a-menu-item>
                        <a-menu-item key="export">导出</a-menu-item>
                      </template>

                      <a-menu-item
                        v-if="hasManagePermission(record) || isPersonal"
                        key="move">
                        移动至
                      </a-menu-item>

                      <template v-if="hasManagePermission(record)">
                        <a-menu-item
                          v-if="
                            record.status === 'UN_PUBLISH' || record.status === 'OFFLINE'
                          "
                          key="publish">
                          发布
                        </a-menu-item>
                        <a-menu-item v-if="record.status === 'ONLINE'" key="offline">
                          下线
                        </a-menu-item>
                        <a-menu-item key="delete" style="color: red">删除</a-menu-item>
                      </template>
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

  <!-- 权限申请 -->
  <ApplyModal v-model:open="applyDrawerOpen" :init-data="rowInfo" @ok="onApplyOk" />

  <!-- 移动至 -->
  <MoveDrawer
    v-model:open="moveDrawerOpen"
    :init-data="rowInfo"
    :init-params="{ position: 'DATASET', type, workspaceId }"
    @ok="onMoveOk" />

  <!-- 设置 -->
  <SettingDrawer
    v-model:open="settingDrawerOpen"
    :init-data="rowInfo"
    @success="onSettingSuccess" />

  <!-- 授权 -->
  <AuthorizeDrawer v-model:open="authorizeDrawerOpen" :init-data="rowInfo" />

  <!-- 搜索页面 -->
  <SearchPageDrawer
    v-model:open="searchPageDrawerOpen"
    :initParams="searchPageParams"
    @close="onSearchPageDrawerClose" />
</template>

<script setup lang="jsx">
import { h, ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  LockOutlined,
  StarFilled,
  StarOutlined,
  LineChartOutlined,
  MoreOutlined,
  CaretLeftOutlined,
  CloseOutlined,
  ExclamationCircleFilled,
} from '@ant-design/icons-vue'
import { tableColumns } from './config'
import { getDatasetList } from '@/apis/dataset'
import ApplyModal from '@/components/ApplyModal/index.vue'
import DirTree, { MoveDrawer } from '@/components/DirTree'
import SettingDrawer from './components/SettingDrawer.vue'
import AuthorizeDrawer from '@/components/Authorize/ListDrawer.vue'
import SearchPageDrawer from './SearchPageDrawer.vue'
import useMenus from './useMenus'
import useTable from 'common/hooks/useTable'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()
const { getAnalysisHref, toAnalysis, edit, favor, handleExport, publish, offline, del } =
  useMenus()

// 分析权限
const hasAnalysisPermission = row => {
  if (userStore.hasPermission('DATASET:ANALYSIS:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('DATASET:ANALYSIS:HAS:PRIVILEGE')) {
    return row.permission === 'READ' || row.permission === 'WRITE'
  } else {
    return false
  }
}

// 编辑权限
const hasWritePermission = row => {
  if (userStore.hasPermission('DATASET:WRITE:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('DATASET:WRITE:CREATE')) {
    return row.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('DATASET:WRITE:HAS:PRIVILEGE')) {
    return row.permission === 'WRITE'
  } else {
    return false
  }
}

// 管理权限（发布，移动，下线，删除，共享，推送）
const hasManagePermission = row => {
  if (userStore.hasPermission('DATASET:MANAGE:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('DATASET:MANAGE:CREATE')) {
    return row.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('DATASET:MANAGE:HAS:PRIVILEGE')) {
    return row.permission === 'WRITE'
  } else {
    return false
  }
}

const workspaceId = computed(() => appStore.workspaceId) // 空间ID
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

// 新建modal
const toCreate = () => {
  const routeRes = router.resolve({
    name: 'DatasetModify',
  })
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
  getDatasetList,
  {
    sorter: { field: 'updateTime', order: 'descend' },
    initQueryParams,
  },
  {
    error: e => console.error('列表请求失败', e),
  }
)

const setRowClassName = ({ permission, status }) => {
  return permission === 'NONE' || permission === 'EXPIRED' || status === 'OFFLINE'
    ? 'no-permission'
    : ''
}

const jumpToAll = () => {
  type.value = 'ALL'
  dirTreeRef.value.set()
}

// 表格列
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

  if (!str.trim().length) {
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
const onTableChange = ({ current, pageSize }, filters, sorts) => {
  pager.current = current
  pager.pageSize = pageSize

  sorter.value = sorts

  fetchList()
}

// 生命周期
onMounted(() => {
  if (route.query.folder) return

  fetchList()
})

const onMenuClick = async ({ key }, row) => {
  switch (key) {
    case '_self':
      toAnalysis(row)
      break
    case '_blank':
      toAnalysis(row, true)
      break
    case 'edit':
      edit(row)
      break
    case 'export':
      handleExport(row)
      break
    case 'move':
      move(row)
      break
    case 'authorize':
      authorize(row)
      break
    case 'setting':
      setting(row)
      break
    case 'publish':
      publish(row)
      break
    case 'offline':
      await offline(row)
      fetchList()
      break
    case 'delete':
      del(row, fetchList)
      break
    default:
      break
  }
}

const rowInfo = ref({})

// 授权
const authorizeDrawerOpen = ref(false)
const authorize = row => {
  rowInfo.value = { ...row }
  authorizeDrawerOpen.value = true
}

// 申请
const applyDrawerOpen = ref(false)
const apply = row => {
  rowInfo.value = { ...row }
  applyDrawerOpen.value = true
}
const onApplyOk = e => {
  const item = list.value.find(item => item.id === e.datasetId)

  item.applying = true
}

// 移动
const moveDrawerOpen = ref(false)
const move = row => {
  // -6 全部
  rowInfo.value = { ...row, folderId: dirInfo.value.id ?? row.folderId ?? -6 }
  moveDrawerOpen.value = true
}
const onMoveOk = () => {
  dirTreeRef.value.reload()
  fetchList()
}

// 设置
const settingDrawerOpen = ref(false)
const setting = row => {
  rowInfo.value = { ...row }
  settingDrawerOpen.value = true
}
const onSettingSuccess = payload => {
  const item = list.value.find(t => t.id === payload.id)

  item.enableApply = payload.enableApply || 0
}
</script>

<style lang="scss" scoped>
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
      margin-left: 24px;
      text-decoration: underline;
    }
    .row--action {
      line-height: 1;
      font-size: 16px;
    }
    &.no-permission {
      .ant-table-cell {
        color: #d9d9d9;
      }
      .row--name {
        color: #d9d9d9 !important;
      }
      .row--action {
        color: initial;
      }
      .expire-flag {
        position: absolute;
        top: -30px;
        left: -30px;
        border: 30px solid transparent;
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
</style>
