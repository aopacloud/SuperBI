<template>
  <section class="preview-section">
    <LHeader
      v-if="!isMonitor"
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
      @timeoffset-change="e => (timeOffset = e)"
      @auto-grid-layout="onGridLayoutAuto"
    />

    <main class="main">
      <Resize
        v-if="!isMonitor"
        class="resize-theme-1"
        :directions="['right']"
        :style="{ border: 'none', width: asideWidth + 'px' }"
        :minWidth="2"
        :autoChange="false"
        @resized="onResized"
        @dblclick="onResizeDblclick"
      >
        <SideNavs :data-source="layout" />
      </Resize>

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
            :class="{ layoutEditable: mode === 'EDIT' }"
            :col-num="colNum"
            :cols="{
              lg: colNum,
              md: colNum,
              sm: colNum,
              xs: colNum,
              xxs: colNum
            }"
            :row-height="rowHeight"
            :margin="margin"
            :verticalCompact="verticalCompact"
            :is-draggable="mode === 'EDIT'"
            v-model:layout="layout"
          >
            <grid-item
              class="layout-item"
              v-for="item in layout"
              dragAllowFrom=".draggable-handler"
              :class="[`layout-item-${item.type}`]"
              :x="item.x"
              :y="item.y"
              :w="item.w"
              :h="item.h"
              :i="item.i"
              :key="item._id"
              :minW="colNum / 5"
              :minH="18"
              :maxW="colNum"
              :maxH="rowHeight * 100"
              :style="{
                background:
                  item.type === 'FILTER' || item.type === 'REMARK'
                    ? 'transparent'
                    : '#fff'
              }"
              :is-resizable="item.type === 'REPORT' && mode === 'EDIT'"
              :dragIgnoreFrom="item.type === 'FILTER' ? '.resize-handle' : ''"
              :resizeFrom="['top', 'right', 'bottom', 'left']"
              @resized="onLayoutItemResized"
            >
              <!-- 便签 -->
              <RemarkItem
                v-if="item.type === 'REMARK'"
                v-resize="e => onLayoutItemFilterResize(e, item)"
                :mode="mode"
                :id="item._id"
                :initial-value="item.content"
                @edit="onRemarkEdit"
                @update="e => onLayoutItemFilterResize(e, item)"
              />
              <!-- 筛选项 -->
              <FilterItem
                v-else-if="item.type === 'FILTER'"
                v-resize="e => onLayoutItemFilterResize(e, item)"
                :mode="mode"
                :id="item._id"
                :dIt="detail.id"
                v-model:data-source="item.content"
                @edit="onFilterItemEdit"
                @copy="onFilterItemCopy"
                @delete="onFilterItemDelete"
                @query="onFilterItemQuery"
              />
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
                :layoutItem="item"
                :chartItem="item.content"
                :timeOffset="timeOffset"
                @loaded="onChartTaskSuccess"
                @sql="e => onSqlPreview(e, item)"
                @download="e => onDownload(e, item)"
                @dataset-apply="e => onDatasetApply(e, item)"
                @reload="e => onBoxReload(e, item)"
                @warning="onWarningSetting"
              />
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
    @ok="onRemarkOk"
  />

  <!-- 看板管理 -->
  <ManageDrawer
    v-model:open="manageDrawerOpen"
    :data-source="layout"
    @ok="onManageOk"
  />

  <!-- 过滤器管理 -->
  <FilterManageDrawer
    v-model:open="filterManageDrawerOpen"
    :loading="requestLoading"
    :reports="filterReports"
    :data-source="currentLayoutItem.content"
    @ok="onFilterManageOk"
  />

  <!-- sql预览 -->
  <SqlPreviewDrawer
    v-model:open="sqlPreviewDrawerOpen"
    :sql="
      currentLayoutItem.response ? currentLayoutItem.response.sql : undefined
    "
  />

  <!-- 下载 -->
  <DownloadModal
    v-model:open="downloadOpen"
    :filename="currentLayoutItem.content?.name"
    :initParams="currentLayoutItem.request"
    :options="currentLayoutItem.options"
    @download="handleDownload"
  />

  <!-- // dataset-apply -->
  <ApplyModal
    v-model:open="applyModalOpen"
    :initData="applyInfo"
    @ok="onApplyOk"
  />

  <!-- 预警设置 -->
  <ViewsReportComponentsWarningDrawer
    :info="warningReportInfo"
    v-model:open="warningDrawerOpen"
    @cancel="warningReportInfo = {}"
  />
</template>

<script setup>
import {
  ref,
  toRaw,
  reactive,
  watch,
  provide,
  onMounted,
  onBeforeUnmount,
  computed,
  nextTick
} from 'vue'
import { message } from 'ant-design-vue'
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
  getLayoutItemHByRealWidth
} from './utils'
import { getDetailById, getLastVersionById } from '@/apis/dashboard'
import useAppStore from '@/store/modules/app'
import emittor from 'common/plugins/emittor'
import useUserStore from '@/store/modules/user'
import { uninitWorker } from '@/components/Chart/Table/exportUtil'
import { versionVue } from '@/versions'
import { debounce, deepClone } from '@/common/utils/help'
import Resize from 'common/components/Layout/Resize/index.vue'

// import GridLayout from '@/components/GridLayoutResizable/components/GridLayout.vue'
// import GridItem from '@/components/GridLayoutResizable/components/GridItem.vue'

const { ViewsReportComponentsWarningDrawer } = versionVue

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

// 来源是监控页面
const isMonitor = computed(() => route.name === 'DashboardMonitor')

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
  const charts = layout.value.filter(
    t => t.type !== 'FILTER' && t.type !== 'REMARK'
  )
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
    }
  },
  allDone: allDone,
  refresh: {
    loading: refreshLoading,
    run: refreshAll
  },
  layout: {
    get: () => toRaw(layout.value)
  }
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
// 看板项的请求loading
const requestLoading = computed(() => taskQueues.value.length > 0)

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
  showTime: true
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
    params: { id }
  } = route

  if (name === 'DashboardPreview' || name === 'DashboardMonitor') {
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
  uninitWorker()
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

const layout = ref([])
// 看板布局更新
const updateLayout = debounce(() => {
  layout.value = layout.value.map(t => t)
}, 40)

// 看板管理抽屉
const manageDrawerOpen = ref(false)
const openManageDrawer = () => {
  manageDrawerOpen.value = true
  mode.value = 'EDIT'
}
const onManageOk = (payload = []) => {
  layout.value = payload.map((item, index) => {
    const { reportId, content, h, y, _oH = h } = item
    const layoutItem = layout.value.find(t => t.reportId === reportId)

    return {
      ...item,
      reportId: reportId || content.id,
      h: _oH,
      _oH: undefined,
      _loaded: layoutItem?._loaded
    }
  })

  nextTick(() => {
    initRequestTask()
  })
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
    content: []
  })
}

// 看板项尺寸改变(内容改变)
const onLayoutItemFilterResize = (e, payload) => {
  if (!e?.target) return

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
const filterReports = computed(() => {
  return toRaw(layout.value)
    .filter(t => t.type === 'REPORT')
    .map(t => {
      return {
        ...t.content.report
      }
    })
    .filter(t => Object.keys(t).length > 0)
})

const filterManageDrawerOpen = ref(false)
const onFilterItemEdit = ({ _id }) => {
  const item = layout.value.find(t => t._id === _id)

  currentLayoutItem.value = { _id, content: item.content }
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
    y: preItem.y + 1
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
        ...charts[reportId]
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
const onDownload = (e, item) => {
  const { payload, download, options } = e
  currentLayoutItem.value = {
    ...item,
    request: { ...payload, fromSource: 'dashboard' },
    options,
    download
  }
  downloadOpen.value = true
}
const handleDownload = () => {
  if (!currentLayoutItem.value.download) {
    message.warn('无法下载')
  } else {
    currentLayoutItem.value.download(currentLayoutItem.value.content?.name)
  }
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

const warningReportInfo = ref()
const warningDrawerOpen = ref(false)
const onWarningSetting = e => {
  warningReportInfo.value = deepClone(e)
  warningDrawerOpen.value = true
}

const onLayoutItemResized = (id, ...res) => {
  const item = layout.value.find(t => t._id === id)
  item._size = undefined
}

// 布局紧凑方式
const verticalCompact = ref(true)
const onGridLayoutAuto = e => {
  verticalCompact.value = true
  updateLayout()
  setTimeout(() => {
    verticalCompact.value = false
  }, 600)
}

const asideWidth = computed(() => {
  const width = appStore.appLayout?.dashboardPreview?.aside?.width

  if (typeof width === 'undefined') {
    return 300
  }
  return width
})
const onResized = e => {
  appStore.setLayout('dashboardPreview', { aside: { width: e.width } })
}
const onResizeDblclick = () => {
  if (asideWidth.value) {
    appStore.setLayout('dashboardPreview', { aside: { width: 0 } })
  } else {
    appStore.setLayout('dashboardPreview', { aside: { width: 300 } })
  }
}
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

.layout.layoutEditable {
  .layout-item-REPORT:hover {
    outline: 1px dashed #6363ff;
    :deep(.draggable-handler) {
      cursor: grab !important;
      &:active {
        cursor: grabbing !important;
      }
    }
  }

  .layout-item {
    cursor: unset !important;
    &:not(.layout-item-REPORT):hover {
      :deep(.draggable-handler) {
        cursor: grab !important;
        &:active {
          cursor: grabbing !important;
        }
      }
    }
  }
}
</style>
