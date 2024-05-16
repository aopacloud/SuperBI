<template>
  <section class="preview-section">
    <LHeader
      :needBeforeInit="needBeforeInit"
      :detail="detail"
      :globalDateConfig="globalDateConfig"
      :layout-components="layout"
      v-model:mode="mode"
      @edit="onEdit"
      @filter="onFilterAdd"
      @manage="openManageDrawer"
      @update-detail="onDetailUpdate"
      @published="onPublished"
      @timeoffset-change="e => (timeOffset = e)" />

    <main class="main">
      <div class="aside-navs" :class="{ collapsed: navCollapsed }">
        <SideNavs :data-source="layout" />
        <div
          class="aside-collapse"
          :class="{ collapsed: navCollapsed }"
          @click="navCollapsed = !navCollapsed">
          <CaretLeftOutlined />
        </div>
      </div>

      <div class="content">
        <a-spin :spinning="loading">
          <span v-if="loading"></span>
          <Empty v-else-if="!layout.length">
            <a-button
              type="primary"
              :disabled="detail.userLevel === 3 && detail.dataEnable === 0"
              @click="openManageDrawer"
              >{{ mode === 'EDIT' ? '添加图表' : '编辑看板' }}
            </a-button>
          </Empty>

          <grid-layout
            v-else
            id="dashboard-grid-layout"
            class="layout"
            :col-num="colNum"
            :cols="{ lg: 3, md: 3, sm: 2, xs: 1, xxs: 1 }"
            :row-height="rowHeight"
            :margin="margin"
            :is-draggable="mode === 'EDIT'"
            :is-resizable="isResizable"
            v-model:layout="layout">
            <grid-item
              class="layout-item"
              v-for="item in layout"
              dragAllowFrom=".graggable-handler"
              :x="item.x"
              :y="item.y"
              :w="item.w"
              :h="item.h"
              :i="item.i"
              :key="item._id"
              :isResizable="mode === 'EDIT' && item._size === 'large'"
              :minW="colNum / 4"
              :minH="item._size === 'large' ? rowHeight * 36 : rowHeight * 18"
              :maxW="colNum"
              :maxH="rowHeight * 100"
              :style="{
                background:
                  item.type === 'FILTER' || item.type === 'REMARK'
                    ? 'transparent'
                    : '#fff',
              }">
              <!-- 便签 -->
              <RemarkItem
                v-if="item.type === 'REMARK'"
                v-resize="e => onLayoutItemFilterReize(e, item)"
                :mode="mode"
                :id="item._id"
                :initial-value="item.content"
                @edit="onRemarkEdit"
                @update="e => onLayoutItemFilterReize(e, item)" />
              <!-- 筛选项 -->
              <FilterItem
                v-else-if="item.type === 'FILTER'"
                v-resize="e => onLayoutItemFilterReize(e, item)"
                :mode="mode"
                :id="item._id"
                :dIt="detail.id"
                :data-source="item.content"
                @edit="onFilterItemEdit"
                @copy="onFilterItemCopy"
                @delete="onFilterItemDelete"
                @query="onFilterItemQuery" />
              <!-- 图表 -->

              <!-- -if="defer(index)" -->
              <!-- :observer="layoutObserver" -->
              <Box
                v-else
                class="item-box"
                :detail="detail"
                :shouldLoad="taskQueues.includes(item.reportId)"
                :reportId="item.reportId"
                :globalDateConfig="globalDateConfig"
                :filters="filterMap[item.reportId]"
                :item="item.content"
                :timeOffset="timeOffset"
                @loaded="onChartTaskSuccess"
                @sql="e => onSqlPreview(e, item)"
                @download="e => onDownload(e, item)"
                @dataset-apply="e => onDatasetApply(e, item)"
                @reload="e => onBoxReload(e, item)" />
            </grid-item>
          </grid-layout>
        </a-spin>
      </div>
    </main>
  </section>

  <!-- 便签编辑 -->
  <RemarkModal
    v-model:open="remarkModalOpen"
    :initial-value="currentLayoutItem.content"
    @ok="onRemarkOk" />

  <!-- 看板管理 -->
  <ManageDrawer
    v-model:open="manageDrawerOpen"
    :data-source="layout"
    @ok="onManageOk" />

  <!-- 过滤器管理 -->
  <FilterManageDrawer
    v-model:open="filterManageDrawerOpen"
    :reports="filterReports"
    :data-source="currentLayoutItem.content"
    @ok="onFilterManageOk" />

  <!-- sql预览 -->
  <SqlPreviewDrawer
    v-model:open="sqlPreviewDrawerOpen"
    :sql="currentLayoutItem.response ? currentLayoutItem.response.sql : undefined" />

  <!-- 下载 -->
  <DownloadModal
    v-model:open="downloadOpen"
    :filename="currentLayoutItem.content?.name"
    :initParams="currentLayoutItem.request" />

  <!-- // dataset-apply -->
  <ApplyModal v-model:open="applyModalOpen" :initData="applyInfo" @ok="onApplyOk" />
</template>

<script setup>
import {
  ref,
  reactive,
  watch,
  provide,
  onMounted,
  onBeforeUnmount,
  computed,
} from 'vue'
import { CaretLeftOutlined } from '@ant-design/icons-vue'
import { useRoute } from 'vue-router'
import LHeader from './LHeader.vue'
import SideNavs from './components/SideNavs.vue'
import Empty from './components/Empty.vue'
import RemarkItem from './components/RemarkItem.vue'
import FilterItem from './components/FilterItem.vue'
import Box from './components/Box.vue'
import ManageDrawer from './components/ManageDrawer.vue'
import SqlPreviewDrawer from './components/SqlPreviewDrawer.vue'
import FilterManageDrawer from './FilterManageDrawer/index.vue'
import RemarkModal from './components/RemarkModal.vue'
import DownloadModal from '@/components/DownloadModal/index.vue'
import ApplyModal from '@/components/ApplyModal/index.vue'
import { LayoutOptions } from './config'
import {
  getLayoutItemHByRealHeight,
  getLayoutItemSize,
  transformGridLayoutItem,
  compatibleGridLayoutItem,
} from './utils'
import { getDetailById, getLastVersionById } from '@/apis/dashboard'
import useAppStore from '@/store/modules/app'
import emittor from 'common/plugins/emittor'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

// 时区偏移
const timeOffset = computed(() => appStore.activeTimeOffset)
// 当前空间id
const currentWorkspaceId = computed(() => appStore.workspaceId)

const { colNum, rowHeight, margin, resizable } = LayoutOptions

// 侧边栏折叠
const navCollapsed = ref(false)

const loading = ref(false)
const mode = ref('READONLY') // READONLY 查看态，EDIT: 编辑态，PREVIEW: 预览态

// 检查看板权限
const checkAccessable = e => {
  // 查看权限
  const READ = row => {
    if (userStore.hasPermission('DASHBOARD:READ:ALL:WORKSPACE')) {
      return true
    } else if (userStore.hasPermission('DASHBOARD:READ:HAS:PRIVILEGE')) {
      return row.permission === 'READ' || row.permission === 'WRITE'
    } else {
      return false
    }
  }

  // 编辑权限
  const WRITE = row => {
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

  const accessable = READ(e) || WRITE(e)

  if (!accessable) {
    appStore.setUnAccessableInfo(
      `没有该看板的访问权限，请联系看板负责人${e.creatorAlias || ''}进行授权`
    )
    appStore.setUnAccessable(true)
  }

  return accessable
}

// 看板详情信息
const detail = ref({})
const fetchDetail = async id => {
  try {
    loading.value = true

    const res = await getDetailById(id)

    if (res.workspaceId && res.workspaceId !== currentWorkspaceId.value) {
      await appStore.setWorkspaceId(res.workspaceId, true)
    }

    const { version = 0, lastEditVersion = 0 } = res
    if (lastEditVersion > version) {
      mode.value = 'READONLY'
    }

    if (!checkAccessable(res)) return

    initWithLayout(res)
  } catch (error) {
    console.error('获取看板详情错误', error)
  } finally {
    loading.value = false
  }
}
// 获取最近编辑版本
const fetchDetailWithLastVersion = async () => {
  try {
    loading.value = true

    const res = await getLastVersionById(detail.value.id)

    initWithLayout(res)
  } catch (error) {
    console.error('获取看板详情错误', error)
  } finally {
    loading.value = false
  }
}

// 全部完成
const allDone = ref(false)
// 刷新全部
const refreshLoading = ref(false)
const refreshAll = async () => {
  const charts = layout.value.filter(t => t.type !== 'FILTER' && t.type !== 'REMARK')
  if (!charts.length) return

  allDone.value = false
  refreshLoading.value = true

  layout.value.forEach(t => {
    t._loaded = false
  })
  initRequestTask()
}

provide('index', {
  timeOffset,
  detail: {
    get: k => (k ? detail.value[k] : detail.value),
    set(k, v) {
      if (!k) {
        detail.value = { ...v }
      } else {
        detail.value[k] = v
      }

      return detail.value
    },
  },
  allDone: allDone,
  refresh: {
    loading: refreshLoading,
    run: refreshAll,
  },
})

const initWithLayout = res => {
  detail.value = res

  layout.value = res.dashboardComponents
    .map(transformGridLayoutItem) // 格式处理
    .map(compatibleGridLayoutItem) // 兼容旧数据
    .sort((a, b) => (a.y === b.y ? a.x - b.x : a.y - b.y))

  initFilters()

  initRequestTask()
}

// 最大队列数量
const MAX_REQUEST_COUNT = 6
// 所有的图表请求队列
const allChartQueues = ref([])
// 当前请求队列
const taskQueues = ref([])
// 初始化队列
const initRequestTask = () => {
  allChartQueues.value = layout.value
    .filter(t => t.type !== 'FILTER' && t.type !== 'REMARK' && !t._loaded)
    .map(t => t.reportId)

  if (allChartQueues.value.length <= MAX_REQUEST_COUNT) {
    taskQueues.value = allChartQueues.value

    allChartQueues.value = []
  } else {
    taskQueues.value = allChartQueues.value.slice(0, MAX_REQUEST_COUNT)

    allChartQueues.value = allChartQueues.value.slice(MAX_REQUEST_COUNT)
  }
}
// 图表请求完成
const onChartTaskSuccess = reportId => {
  const item = layout.value.find(t => t.reportId === reportId)
  item._loaded = true

  const index = taskQueues.value.findIndex(t => t === reportId)
  taskQueues.value.splice(index, 1)

  const next = allChartQueues.value.shift()
  if (!next) {
    allDone.value = true
    refreshLoading.value = false

    return
  }

  taskQueues.value.push(next)
}

// 初始化全局筛选条件
const initFilters = () => {
  const list = layout.value.filter(t => t.type === 'FILTER').map(t => t.content)

  filterMap.value = list.map(_dealFilterChartItem).reduce((acc, cur) => {
    for (const reportId in cur) {
      if (!acc[reportId]) {
        acc[reportId] = []
      }

      acc[reportId].push(...cur[reportId])
    }

    return acc
  }, {})
}

// 全局日期过滤
const globalDateConfig = reactive({
  mode: 1,
  offset: [],
  date: [],
  hms: [],
  showTime: true,
})
const onDetailUpdate = e => {
  detail.value = { ...detail.value, ...e }
}
const onPublished = e => {
  onDetailUpdate(e)

  mode.value = 'PREVIEW'
}

// 初始化前需要填写基本信息
const needBeforeInit = ref(false)

onMounted(() => {
  appStore.toggleSideBarHide(true)

  const {
    name,
    params: { id },
  } = route

  if (name === 'DashboardPreview') {
    mode.value = 'READONLY'
  } else {
    mode.value = 'EDIT'
  }

  if (id) {
    fetchDetail(id)
  } else {
    needBeforeInit.value = true
  }
})
onBeforeUnmount(() => {
  appStore.toggleSideBarHide(false)
})

watch(
  () => route.params,
  (nP, oP) => {
    // fix 跳转到别的路由下的错误请求
    if (!['DashboardPreview', 'DashboardPreview'].includes(route.name)) return

    if (nP.id !== oP.id) fetchDetail(nP.id)
  },
  { deep: true }
)

const isResizable = ref(resizable)
const layout = ref([])
// 看板布局更新
const updateLayout = () => {
  layout.value = layout.value.map(t => t)
}

// 看板管理抽屉
const manageDrawerOpen = ref(false)
const openManageDrawer = () => {
  manageDrawerOpen.value = true
  mode.value = 'EDIT'
}
const onManageOk = payload => {
  const list = payload.map(item => {
    const { _size, _oH, reportId, content } = item
    const layoutItem = layout.value.find(t => t.reportId === reportId)

    return {
      ...item,
      reportId: reportId || content.id,
      h: _oH || getLayoutItemSize('height', _size),
      _loaded: layoutItem?._loaded,
    }
  })

  layout.value = list.sort((a, b) => a._sort - b._sort)

  initRequestTask()
}

const onEdit = () => {
  mode.value = 'EDIT'
  fetchDetailWithLastVersion()
}

// 添加一个 FILTER
const onFilterAdd = () => {
  const i = Date.now() + ''
  layout.value.unshift({
    i,
    _id: i,
    type: 'FILTER',
    x: 0,
    y: 0,
    w: getLayoutItemSize('width', 'large'),
    h: 10,
    content: [],
  })
}

// 看板项尺寸改变(内容改变)
const onLayoutItemFilterReize = (e, payload) => {
  const height = e.target.offsetHeight
  // const height = e.target.getBoundingClientRect().height
  const h = getLayoutItemHByRealHeight(height)
  const item = layout.value.find(t => +t.i === +payload.i)

  item.h = h

  updateLayout()
}

// 当前操作的看板项
const currentLayoutItem = ref({})

// 便签
const remarkModalOpen = ref(false)
const onRemarkEdit = item => {
  currentLayoutItem.value = { ...item }
  remarkModalOpen.value = true
}
const onRemarkOk = payload => {
  const item = layout.value.find(t => t._id === currentLayoutItem.value._id)

  item.content = { ...payload }
  currentLayoutItem.value = {}
}

// 过滤器中关联的图表
const filterReports = ref([])
// 过滤器管理
const filterManageDrawerOpen = ref(false)
const onFilterItemEdit = ({ _id }) => {
  const item = layout.value.find(t => t._id === _id)

  currentLayoutItem.value = { _id, content: item.content }

  filterReports.value = toRaw(layout.value)
    .filter(t => t.type === 'REPORT')
    .map(t => {
      return {
        ...t.content.report,
      }
    })
    .filter(t => Object.keys(t).length > 0)
  filterManageDrawerOpen.value = true
}
const onFilterManageOk = payload => {
  const item = layout.value.find(t => t._id === currentLayoutItem.value._id)

  item.content = [...payload]
  currentLayoutItem.value = {}
  filterManageDrawerOpen.value = false
}
// 复制
const onFilterItemCopy = item => {
  const index = layout.value.findIndex(t => t._id === item._id)
  const preItem = layout.value[index]
  const _i = Date.now()

  const newItem = {
    ...preItem,
    i: _i,
    _id: _i,
    y: preItem.y + 1,
  }

  layout.value.splice(index + 1, 0, newItem)
}
// 删除
const onFilterItemDelete = item => {
  const index = layout.value.findIndex(t => t._id === item._id)

  layout.value.splice(index, 1)
  updateLayout()
}

// 筛选项
const filterMap = ref({})
// 处理筛选查询参数
const _dealFilterChartItem = (list = []) => {
  const res = {}

  list.forEach(item => {
    const { filterType, filterMethod, charts, value } = item

    for (const reportId in charts) {
      if (!res[reportId]) {
        res[reportId] = []
      }

      res[reportId].push({
        filterType,
        filterMethod,
        value,
        ...charts[reportId],
      })
    }
  })

  return res
}
const onFilterItemQuery = (payload = []) => {
  const filterRes = _dealFilterChartItem(payload)

  for (const reportId in filterRes) {
    filterMap.value[reportId] = filterRes[reportId]
  }
}

// sql 预览
const sqlPreviewDrawerOpen = ref(false)
const onSqlPreview = (response, item) => {
  currentLayoutItem.value = { ...item, response }
  sqlPreviewDrawerOpen.value = true
}

// 下载
const downloadOpen = ref(false)
const onDownload = (request, item) => {
  currentLayoutItem.value = {
    ...item,
    request: { ...request, fromSource: 'dashboard' },
  }
  downloadOpen.value = true
}

// 数据集权限申请
const applyModalOpen = ref(false)
const applyInfo = ref({})
const onDatasetApply = e => {
  applyModalOpen.value = true
  applyInfo.value = { ...e }
}
const onApplyOk = e => {
  emittor.emit('dataset-applying', e)
}

// 看板项刷新
const onBoxReload = (e, item) => {
  // 更新看板项名称的显示
  item.content.name = e.name
}

watch(
  [remarkModalOpen, filterManageDrawerOpen, sqlPreviewDrawerOpen, downloadOpen],
  r => {
    if (!r.some(t => t)) {
      currentLayoutItem.value = {}
    }
  }
)

// const observeCb = entries => {
//   for (const entry of entries) {
//     const { target, isIntersecting } = entry

//     if (isIntersecting) {
//       const targetReportId = target.getAttribute('data-reportId')
//       const layoutItem = layout.value.find(t => t.reportId === +targetReportId)

//       if (!layoutItem || layoutItem._loaded) return

//       layoutItem._loaded = true
//     }
//   }
// }

// const layoutObserver = new IntersectionObserver(observeCb, {
//   root: document.querySelector('#dashboard-grid-layout'),
//   rootMargin: '10px 10px 10px 10px',
//   threshold: 0.2,
// })
</script>

<style scoped lang="scss">
.preview-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.main {
  position: relative;
  flex: 1;
  display: flex;
  overflow: auto;
}

.content {
  flex: 1;
  background-color: #f0f2f5;
  overflow-x: hidden;

  & > :deep(.ant-spin-nested-loading) {
    height: 100%;
    & > .ant-spin-container {
      height: 100%;
    }
    & > div > .ant-spin {
      max-height: initial !important;
    }
  }
}

.aside-navs {
  width: 260px;
  transition: all 0.15s;
  &.collapsed {
    width: 0;
    overflow: hidden;
  }
}
.aside-collapse {
  position: absolute;
  left: 260px;
  top: 50%;
  padding: 12px 1px;
  background-color: #e5e9ec;
  border-left: none;
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
  transition: all 0.15s;
  cursor: pointer;
  z-index: 9;

  &.collapsed {
    left: 0;
  }
}

.layout {
  // background: #f0f2f5;
}
.layout-item:not(.vue-grid-placeholder) {
  background: #fff;
}
</style>
