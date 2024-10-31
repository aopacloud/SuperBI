<template>
  <section class="page flex-column">
    <header>
      <a-tabs
        v-model:activeKey="tabActiveKey"
        type="card"
        @change="onTabChange"
        style="margin: 20px 20px 0"
      >
        <a-tab-pane key="DATASET" :tab="resourceTypeMap['DATASET']" />
        <a-tab-pane key="DASHBOARD" :tab="resourceTypeMap['DASHBOARD']" />
        <a-tab-pane key="REPORT" :tab="resourceTypeMap['REPORT']" />

        <template #rightExtra>
          <a-input-search
            style="width: 300px"
            placeholder="请输入搜索"
            allow-clear
            v-model:value="keyword"
            @search="fetchList()"
          />
        </template>
      </a-tabs>
    </header>

    <main class="flex-1">
      <a-table
        style="height: 100%; margin: 0 10px 0"
        size="small"
        rowKey="sourceId"
        :sticky="{ getContainer }"
        :loading="loading"
        :columns="columns"
        :data-source="list"
        :pagination="pager"
        :row-selection="rowSelection"
        :scroll="{ x: 1200 }"
        @change="onTableChange"
      >
        <template
          #customFilterDropdown="{
            setSelectedKeys,
            selectedKeys,
            confirm,
            clearFilters,
            column
          }"
        >
          <div class="filter-wrapper">
            <a-menu
              v-if="column.dataIndex === 'workspaceIds'"
              style="margin: 0 -8px; border-bottom: 1px solid #f0f0f0"
              multiple
              :selectedKeys="selectedKeys"
              @deselect="e => setSelectedKeys(e.selectedKeys)"
              @select="e => setSelectedKeys(e.selectedKeys)"
            >
              <a-menu-item v-for="item in workspaceList" :key="item.id">
                <a-checkbox :checked="selectedKeys.includes(item.id)">
                  {{ item.name }}
                </a-checkbox>
              </a-menu-item>
            </a-menu>

            <a-menu
              v-if="column.dataIndex === 'status'"
              style="margin: 0 -8px; border-bottom: 1px solid #f0f0f0"
              multiple
              :selectedKeys="selectedKeys"
              @deselect="e => setSelectedKeys(e.selectedKeys)"
              @select="e => setSelectedKeys(e.selectedKeys)"
            >
              <a-menu-item v-for="item in statusMap.DATASET" :key="item.value">
                <a-checkbox :checked="selectedKeys.includes(item.value)">
                  {{ item.text }}
                </a-checkbox>
              </a-menu-item>
            </a-menu>

            <div class="filter-action">
              <a-button
                size="small"
                style="width: 90px"
                @click="clearFilters({ confirm: true, closeDropdown: true })"
              >
                重置
              </a-button>
              <a-button
                type="primary"
                size="small"
                style="width: 90px; margin-left: 8px"
                @click="confirm()"
              >
                确认
              </a-button>
            </div>

            <!-- 负责人筛选 -->
            <a-select
              v-if="column.dataIndex === 'creators'"
              style="width: 188px"
              placeholder="筛选负责人"
              mode="multiple"
              size="small"
              allow-clear
              :value="selectedKeys"
              :options="filterUsers"
              :field-names="{ label: 'aliasName', value: 'username' }"
              :filterOption="filterOption"
              @change="e => setSelectedKeys(e)"
            />

            <!-- 应用看板筛选 -->
            <a-select
              v-if="column.dataIndex === 'dashboards'"
              style="width: 188px"
              placeholder="筛选应用看板"
              mode="multiple"
              size="small"
              allow-clear
              :value="selectedKeys"
              :options="allDashboards"
              :field-names="{ label: 'name', value: 'sourceId' }"
              :filterOption="dashboardFilterOption"
              @change="e => setSelectedKeys(e)"
            />
          </div>
        </template>

        <template #bodyCell="{ text, column, record, record: { status } }">
          <template v-if="column.dataIndex === 'name'">
            <a target="_blank" :href="getResourceHref(record)">{{ text }}</a>
          </template>

          <!-- 图表数量 -->
          <template v-else-if="column.dataIndex === 'reportNum'">
            <template v-if="!text">{{ text ?? '-' }}</template>
            <a v-else @click="toReportModal(record)">{{ text }}</a>
          </template>

          <!-- 应用看板 -->
          <template v-if="column.dataIndex === 'dashboards'">
            <span v-if="!record.dashboardCount"> - </span>
            <a-popover
              v-else
              placement="rightTop"
              overlayClassName="dashboardList-popover"
              @openChange="e => onDashboardCountOpenChange(e, record)"
            >
              <a>{{ record.dashboardCount }}</a>
              <template #content>
                <a-spin :spinning="record._dashboardLoading">
                  <div class="d-list">
                    <div
                      class="d-item"
                      v-for="dashboard in record.dashboards"
                      :key="dashboard.id"
                    >
                      <a
                        :href="getReportDashboardUrl(dashboard.id)"
                        target="_blank"
                      >
                        {{ dashboard.name }}
                      </a>
                    </div>
                  </div>
                </a-spin>
              </template>
            </a-popover>
          </template>

          <template v-if="column.key === 'action'">
            <a-space :size="0">
              <a-button
                v-if="tabActiveKey !== 'REPORT'"
                style="color: #1677ff"
                size="small"
                type="text"
                @click="authorHandler(record)"
              >
                {{ tabActiveKey === 'DATASET' ? '授权' : '共享' }}
              </a-button>
              <a-button
                style="color: #1677ff"
                size="small"
                type="text"
                @click="transferHandler(record)"
              >
                移交
              </a-button>

              <a-button
                v-if="tabActiveKey === 'REPORT'"
                style="color: red"
                size="small"
                type="text"
                @click="deleteHandler(record)"
              >
                删除
              </a-button>

              <a-dropdown trigger="click" v-if="tabActiveKey !== 'REPORT'">
                <a-button size="small" type="text">更多</a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item key="move" @click="moveHandler(record)">
                      移动至
                    </a-menu-item>
                    <a-menu-item
                      v-if="status === 'ONLINE'"
                      key="offline"
                      @click="offlineHandler(record.sourceId)"
                    >
                      下线
                    </a-menu-item>
                    <a-menu-item
                      v-if="
                        tabActiveKey === 'DASHBOARD' && status === 'OFFLINE'
                      "
                      key="online"
                      @click="onlineHandler(record.sourceId)"
                    >
                      上线
                    </a-menu-item>
                    <a-menu-item
                      v-if="
                        status === 'UN_PUBLISH' ||
                        (tabActiveKey === 'DATASET' && status === 'OFFLINE')
                      "
                      key="publish"
                      @click="publishHandler(record.sourceId)"
                    >
                      发布
                    </a-menu-item>
                    <a-menu-item
                      style="color: red"
                      @click="deleteHandler(record)"
                    >
                      删除
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>

        <template #summary v-if="rowSelection.selectedRowKeys.length">
          <a-table-summary fixed="bottom">
            <a-table-summary-row>
              <a-table-summary-cell :index="0" :col-span="2">
                选中 {{ rowSelection.selectedRowKeys.length }} 项
                <a-button type="link" @click="resetRowSelectKeys">
                  清空
                </a-button>
              </a-table-summary-cell>

              <a-table-summary-cell :col-span="summaryColspan">
              </a-table-summary-cell>

              <a-table-summary-cell
                :col-span="2"
                :index="summaryColspan + 1"
                fixed="right"
                align="right"
              >
                <div class="summary-action-fixed">
                  <a-space class="fixed-action" :size="16">
                    <a-button
                      v-if="
                        tabActiveKey !== 'REPORT' &&
                        getMultipleActionButton['MOVE']
                      "
                      ghost
                      size="small"
                      type="primary"
                      @click="multipleAuthor"
                    >
                      {{ tabActiveKey === 'DATASET' ? '授权' : '共享' }}
                    </a-button>
                    <a-button
                      ghost
                      size="small"
                      type="primary"
                      @click="multipleTransfer"
                    >
                      移交
                    </a-button>
                    <a-button
                      v-if="
                        tabActiveKey !== 'REPORT' &&
                        getMultipleActionButton['MOVE']
                      "
                      ghost
                      size="small"
                      type="primary"
                      @click="multipleMove"
                    >
                      移动至
                    </a-button>

                    <template v-if="tabActiveKey !== 'REPORT'">
                      <a-button
                        v-if="getMultipleActionButton['ONLINE']"
                        ghost
                        size="small"
                        type="primary"
                        @click="multipleOffline"
                      >
                        下线
                      </a-button>
                      <a-button
                        v-if="
                          tabActiveKey === 'DASHBOARD' &&
                          getMultipleActionButton['OFFLINE']
                        "
                        ghost
                        size="small"
                        type="primary"
                        @click="multipleOnline"
                      >
                        上线
                      </a-button>
                      <a-button
                        v-if="
                          (tabActiveKey === 'DASHBOARD' &&
                            getMultipleActionButton['UN_PUBLISH']) ||
                          (tabActiveKey === 'DATASET' &&
                            getMultipleActionButton['OFFLINE'])
                        "
                        ghost
                        size="small"
                        type="primary"
                        @click="multiplePublish"
                      >
                        发布
                      </a-button>
                    </template>

                    <a-button
                      ghost
                      size="small"
                      danger
                      @click="deleteHandler()"
                    >
                      删除
                    </a-button>
                  </a-space>
                </div>
              </a-table-summary-cell>
            </a-table-summary-row>
          </a-table-summary>
        </template>
      </a-table>
    </main>

    <modalContext />

    <!-- 图表 -->
    <ReportModal
      :resourceType="tabActiveKey"
      :initData="rowInfo"
      v-model:open="reportModalOpen"
      @to-manage="onToReportManage"
    />

    <!-- 授权 -->
    <AuthorizeListDrawer :initData="rowInfo" v-model:open="authorDrawerOpen" />

    <!-- 共享 -->
    <ShareDrawer :initData="rowInfo" v-model:open="shareDrawerOpen" />

    <!-- 批量授权/共享 -->
    <MultipleAuthorDrawer
      v-bind="{
        [tabActiveKey === 'DATASET' ? 'datasetIds' : 'dashboardIds']:
          rowInfo._resourceIds
      }"
      :resource="tabActiveKey"
      :init-data="rowInfo"
      :workspaceId="rowInfo.workspaceId"
      :multiple="!rowInfo.sourceId && rowSelection.selectedRowKeys.length > 0"
      :multipleDatasetDisabled="
        tabActiveKey === 'DATASET' && rowSelection.selectedRowKeys.length > 0
      "
      :multipleDashboardDisabled="
        tabActiveKey === 'DASHBOARD' && rowSelection.selectedRowKeys.length > 0
      "
      v-model:open="multipleAuthorDrawerOpen"
      @success="onMultipleActionSuccess"
    />

    <!-- 移交 -->
    <TransferModal
      :resourceType="tabActiveKey"
      :initData="rowInfo"
      :workspaceId="rowInfo.workspaceId"
      :ids="!rowInfo.sourceId ? rowSelection.selectedRowKeys : undefined"
      v-model:open="transferModalOpen"
      @ok="onMultipleActionSuccess"
    />

    <!-- 移动至 -->
    <MoveDrawer
      :initData="rowInfo"
      :initParams="{
        type: 'ALL',
        position: tabActiveKey,
        workspaceId: rowInfo.workspaceId
      }"
      :ids="!rowInfo.sourceId ? rowSelection.selectedRowKeys : undefined"
      :customRequest="multipleMoveApi"
      v-model:open="moveDrawerOpen"
      @ok="onMultipleActionSuccess"
    />
  </section>
</template>
<script setup lang="jsx">
import { computed, reactive, ref, shallowRef, getCurrentInstance } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useRouter, useRoute } from 'vue-router'
import useAppStore from '@/store/modules/app.js'
import useTable from '@/common/hooks/useTable/index'
import {
  statusMap,
  resourceTypeMap,
  datasetTableColumns,
  dashboardTableColumns,
  reportTableColumns
} from './config.jsx'
import { getAllUser } from '@/apis/user'
import {
  getResourceList,
  postOfflineByIds,
  deleteByIds,
  postMoveByList,
  postOnlineByIds,
  postPublishByIds
} from '@/apis/resource/manage'
import {
  postPublishById as postDatasetPublishById,
  postOfflineById as postDatasetOfflineById,
  deleteById as deleteDatasetById
} from '@/apis/dataset'
import {
  postPublishById as postDashboardPublishById,
  postOnlineById as postDashboardOnlineById,
  postOfflineById as postDashboardOfflineById,
  deleteById as deleteDashboardById,
  getDasboardsByReportId
} from '@/apis/dashboard'
import { deleteById as deleteReportById } from '@/apis/report'
import ReportModal from './components/ReportModal.vue'
import AuthorizeListDrawer from '@/components/Authorize/ListDrawer.vue'
import ShareDrawer from '@/views/dashboard/components/ShareDrawer.vue'
import MultipleAuthorDrawer from '@/components/Authorize/AuthorDrawer/index.vue'
import TransferModal from '@/components/TransferModal/index.vue'
import MoveDrawer from '@/components/DirTree/components/MoveDrawer.vue'
import { clearQuerys } from '@/common/utils/window.js'

const router = useRouter()

const route = useRoute()

const appStore = useAppStore()

const workspaceList = computed(() => appStore.workspaceList)

const getContainer = () => document.querySelector('#page-wrapper')

const [modal, modalContext] = Modal.useModal()

const tabActiveKey = ref(
  route.query.tab in resourceTypeMap ? route.query.tab : 'DATASET'
)
const onTabChange = () => {
  sorter.value = { field: 'sourceUpdateTime', order: 'descend' }
  keyword.value = ''
  filters.value = {}
  pager.current = 1
  resetRowSelectKeys()
  fetchList()
}

const {
  loading,
  keyword,
  pager,
  sorter,
  filters,
  list,
  onTableChange,
  fetchList
} = useTable(getResourceList, {
  pager: {
    showTotal: true
  },
  keyword: route.query.kw,
  sorter: { field: 'sourceUpdateTime', order: 'descend' },
  initQueryParams: () => {
    return { position: tabActiveKey.value }
  },
  onSuccess() {
    rowSelection.selectedRowKeys = []
  },
  onEnd() {
    clearQuerys(['tab', 'kw'])
  }
})

const allDashboards = shallowRef([])
const onAllDashboardsUpdate = open => {
  if (!open) return

  getResourceList({ position: 'DASHBOARD', pageSize: 10000 })
    .then(r => {
      allDashboards.value = r.data
    })
    .catch(e => {
      console.error('获取所有看板失败', e)
    })
}

const columns = computed(() => {
  const k = tabActiveKey.value
  let _columns = []

  if (k === 'DATASET') {
    _columns = datasetTableColumns
  } else if (k === 'DASHBOARD') {
    _columns = dashboardTableColumns
  } else if (k === 'REPORT') {
    _columns = reportTableColumns.map(t => {
      return t.dataIndex === 'dashboards'
        ? { ...t, onFilterDropdownOpenChange: onAllDashboardsUpdate }
        : t
    })
  }

  const { field, order } = sorter.value || {}

  return _columns.map(col => {
    return {
      ...col,
      filteredValue:
        col.customFilterDropdown || col.filters
          ? (filters.value?.[col.dataIndex] ?? null)
          : undefined,
      sortOrder: col.dataIndex === field ? order : undefined,
      filters: typeof col.filters === 'function' ? col.filters() : col.filters
    }
  })
})

// 汇总行占的列数
const summaryColspan = computed(() => {
  const k = tabActiveKey.value
  return k === 'DATASET' ? 7 : k === 'DASHBOARD' ? 8 : 5
})

// 所有人
const filterUsers = shallowRef([])
const getFilterUsers = async () => {
  getAllUser({ pageSize: 10000 }).then(r => {
    filterUsers.value = r.data
  })
}
getFilterUsers()

const filterOption = (input, opt) =>
  (opt.username + opt.aliasName).toLowerCase().indexOf(input.toLowerCase()) > -1
const dashboardFilterOption = (input, opt) =>
  opt.name.toLowerCase().includes(input.toLowerCase())

// 选中行
const rowSelection = reactive({
  selectedRowKeys: [],
  onChange: keys => {
    rowSelection.selectedRowKeys = keys
  }
})
const resetRowSelectKeys = () => {
  rowSelection.selectedRowKeys = []
}

// 批量操作的按钮的显示隐藏
const getMultipleActionButton = computed(() => {
  const keys = rowSelection.selectedRowKeys
  if (!keys.length) return {}

  const rows = keys.map(k => list.value.find(t => t.sourceId === k))
  const status = [...new Set(rows.map(t => t.status))]
  const workspaceId = [...new Set(rows.map(t => t.workspaceId))]

  // 上线、下线、发布
  const canStatus = status.length === 1
  // 跨空间移动
  const MOVE = workspaceId.length === 1

  // 选中的行状态去重后有多个，不能执行批量 上线、下线、发布的操作
  if (!canStatus) return { MOVE }

  return { [status[0]]: canStatus, MOVE }
})

const ROUTER_NAME_MAP = {
  DASHBOARD: 'DashboardPreview',
  DATASET: 'DatasetAnalysis',
  REPORT: 'ReportDetail'
}
const getResourceHref = row => {
  const { position, sourceId } = row
  const routeRes = router.resolve({
    name: ROUTER_NAME_MAP[position],
    params: { id: sourceId }
  })

  return routeRes?.href
}

// 删除
const deleteHandler = row => {
  const resourceTypeLabel = resourceTypeMap[tabActiveKey.value]

  const content = (
    <div style='margin: 12px 0 16px'>
      您正在
      {row ? (
        <span>
          删除{resourceTypeLabel}
          <b style='color: red'>[{row.name}]</b>
        </span>
      ) : (
        <span>
          批量删除
          <b style='color: red'> {rowSelection.selectedRowKeys.length} </b>个
          {resourceTypeLabel}
        </span>
      )}
      ，
      {tabActiveKey.value === 'DATASET' ? (
        <span>
          删除{resourceTypeLabel}会将基于此创建的
          <b style='color: red'>图表同步删除，</b>
        </span>
      ) : null}
      删除后<b> 30 </b>天内您可以在回收站将其找回
    </div>
  )

  modal.confirm({
    title: '确认删除吗？',
    content,
    okType: 'danger',
    onOk() {
      const fn = row
        ? tabActiveKey.value === 'DATASET'
          ? deleteDatasetById
          : tabActiveKey.value === 'DASHBOARD'
            ? deleteDashboardById
            : deleteReportById
        : () =>
            deleteByIds(
              { position: tabActiveKey.value },
              rowSelection.selectedRowKeys
            )

      return fn(row?.sourceId)
        .then(() => {
          fetchList()
        })
        .catch(error => {
          console.error('删除失败', error)
          return Promise.reject()
        })
    }
  })
}

fetchList()

const rowInfo = ref({})

// 图表
const reportModalOpen = ref(false)
const toReportModal = row => {
  rowInfo.value = { ...row }
  reportModalOpen.value = true
}
const onToReportManage = e => {
  const routeRef = router.resolve({
    name: 'ResourceManage',
    query: { tab: 'REPORT', kw: e.name }
  })
  if (!routeRef) return

  window.open(routeRef.href, '_blank')
}

// 授权
const authorDrawerOpen = ref(false)
const shareDrawerOpen = ref(false)
const authorHandler = row => {
  rowInfo.value = {
    ...row,
    id: row.sourceId
  }

  if (tabActiveKey.value === 'DATASET') {
    authorDrawerOpen.value = true
  } else {
    shareDrawerOpen.value = true
  }
}
// 批量授权
const multipleAuthorDrawerOpen = ref(false)
const multipleAuthor = () => {
  const rows = rowSelection.selectedRowKeys.map(k =>
    list.value.find(t => t.sourceId === k)
  )
  rowInfo.value = {
    _resourceIds: rowSelection.selectedRowKeys,
    workspaceId: rows[0]['workspaceId']
  }
  multipleAuthorDrawerOpen.value = true
}

// 移交
const transferModalOpen = ref(false)
const transferHandler = row => {
  rowInfo.value = { ...row, id: row.sourceId }
  transferModalOpen.value = true
}
const multipleTransfer = () => {
  const selectedRowKeys = rowSelection.selectedRowKeys
  const rows = selectedRowKeys.map(id =>
    list.value.find(t => t.sourceId === id)
  )
  rowInfo.value = {
    id: undefined,
    ids: selectedRowKeys,
    creator: selectedRowKeys.length > 1 ? undefined : rows[0].creator,
    workspaceId: rows.map(t => t.workspaceId),
    creators: rows.map(t => t.creator)
  }
  transferModalOpen.value = true
}

// 移动
const moveDrawerOpen = ref(false)
const moveHandler = row => {
  rowInfo.value = { ...row, id: row.sourceId }
  moveDrawerOpen.value = true
}
const multipleMove = () => {
  const selectedRowKeys = rowSelection.selectedRowKeys
  const first = list.value.find(t => t.sourceId === selectedRowKeys[0])
  rowInfo.value = {
    ids: selectedRowKeys,
    workspaceId: first?.workspaceId
  }
  moveDrawerOpen.value = true
}

// 下线
const offlineHandler = resourceId => {
  const fn =
    tabActiveKey.value === 'DATASET'
      ? postDatasetOfflineById
      : postDashboardOfflineById

  fn(resourceId)
    .then(r => {
      message.success('下线成功')
      fetchList()
    })
    .catch(e => {
      console.error('下线失败', e)
    })
}
const multipleOffline = () => {
  postOfflineByIds(
    { position: tabActiveKey.value },
    rowSelection.selectedRowKeys
  )
    .then(() => {
      message.success('批量下线成功')
      fetchList()
    })
    .catch(e => {
      console.error('批量下线失败', e)
    })
}

// 上线
const onlineHandler = resourceId => {
  postDashboardOnlineById(resourceId)
    .then(r => {
      message.success('上线成功')
      fetchList()
    })
    .catch(e => {
      console.error('上线失败', e)
    })
}
const multipleOnline = () => {
  postOnlineByIds(
    { position: tabActiveKey.value },
    rowSelection.selectedRowKeys
  )
    .then(() => {
      message.success('批量上线成功')
      fetchList()
    })
    .catch(e => {
      console.error('批量上线失败', e)
    })
}

// 发布
const publishHandler = resourceId => {
  const fn =
    tabActiveKey.value === 'DATASET'
      ? postDatasetPublishById
      : postDashboardPublishById
  fn(resourceId)
    .then(() => {
      message.success('发布成功')
      fetchList()
    })
    .catch(e => {
      console.error('发布失败', e)
    })
}
const multiplePublish = () => {
  postPublishByIds(
    { position: tabActiveKey.value },
    rowSelection.selectedRowKeys
  )
    .then(() => {
      message.success('批量发布成功')
      fetchList()
    })
    .catch(e => {
      console.error('批量发布失败', e)
    })
}

// 批量移动
const multipleMoveApi = payload => {
  const params = rowSelection.selectedRowKeys.map(key => {
    const row = list.value.find(t => t.sourceId === key)
    return {
      ...payload,
      targetId: key,
      workspaceId: row.workspaceId
    }
  })

  return postMoveByList(params)
}
const onMultipleActionSuccess = () => {
  resetRowSelectKeys()
  fetchList()
}

const getReportDashboardUrl = sourceId => {
  return getResourceHref({ position: 'DASHBOARD', sourceId })
}
// 被应用的看板
const onDashboardCountOpenChange = async (visible, row) => {
  if (!visible || row._dashboardLoading || row._dashboardLoaded) return

  try {
    row._dashboardLoading = true

    const res = await getDasboardsByReportId({ reportId: row.sourceId })

    row.dashboards = res || []
    row._dashboardLoaded = true
  } catch (error) {
    console.error('图表应用看板获取错误', error)
  } finally {
    row._dashboardLoading = false
  }
}

const setA = e => {
  console.log('e', e)
}
</script>

<style lang="scss" scoped>
.filter-wrapper {
  padding: 8px;
  .filter-action {
    margin: 8px 0;
    &:first-child {
      margin-top: 0;
    }
    &:last-child {
      margin-bottom: 0;
    }
  }
}

.dashboardList-popover {
  .ant-popover-content {
    max-height: 600px;
    .d-list {
      min-width: 120px;
      max-width: 520px;
      max-height: 600px;
      margin-right: -10px;
      padding-right: 10px;
      overflow: auto;
    }
    .d-item {
      & + & {
        margin-top: 4px;
      }
      @extend .ellipsis;
    }
  }
}

.summary-action-fixed {
  position: absolute;
  height: 49px;
  background-color: #fff;
  width: 200%;
  top: -1px;
  right: 0;
  z-index: 2;

  .fixed-action {
    position: absolute;
    right: 8px;
    top: 12px;
  }
}
</style>
