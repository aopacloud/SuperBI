<template>
  <a-drawer
    rootClassName="fullscreen-search-drawer"
    :contentWrapperStyle="{ paddingTop: drawerPaddingTop }"
    :width="drawerWidth"
    :open="open"
    :title="null"
    :keyboard="false"
    :closable="false"
    :maskClosable="false">
    <template #title>
      <a-button
        type="text"
        style="padding-left: 10px; padding-right: 10px"
        :icon="h(LeftOutlined)"
        @click="toBack"
        >返回列表
      </a-button>
    </template>
    <template #extra>
      <div :class="{ errorSearched }">
        <a-input-search
          ref="keywordRef"
          style="width: 240px"
          placeholder="输入搜索"
          allow-clear
          v-model:value="keyword"
          @input="e => onKeywordInput(e.target.value)"
          @search="onSearch" />
      </div>
    </template>

    <a-table
      rowKey="id"
      size="small"
      class="list-table"
      :loading="loading"
      :columns="columns"
      :data-source="list"
      :scroll="{ x: 1200, y: 'auto' }"
      :pagination="pager"
      :row-class-name="setRowClassName"
      @change="onTableChange"
      @showSizeChange="onShowSizeChange">
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
                    v-if="
                      record.status === 'UN_PUBLISH' || record.status === 'OFFLINE'
                    "
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

        <template v-if="column.dataIndex === 'folder'">
          <span v-if="!text">-</span>
          <a v-else style="color: inherit" @click="openWindowWithParams(record)">
            {{ text.absolutePath }}
          </a>
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
              v-if="hasWritePermission(record) || hasManagePermission(record)"
              :trigger="['click']">
              <a-button size="small" type="text" :icon="h(MoreOutlined)" />

              <template #overlay>
                <a-menu @click="e => onMenuClick(e, record)">
                  <a-menu-item v-if="hasWritePermission(record)" key="edit">
                    编辑
                  </a-menu-item>

                  <template v-if="hasManagePermission(record)">
                    <template v-if="hasManagePermission(record)">
                      <a-menu-item key="authorize">授权</a-menu-item>
                      <a-menu-item key="export">导出</a-menu-item>
                    </template>

                    <a-menu-item v-if="hasManagePermission(record)" key="move">
                      移动至
                    </a-menu-item>

                    <template v-if="hasManagePermission(record)">
                      <a-menu-item
                        v-if="record.status === 'UN_PUBLISH'"
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
  </a-drawer>

  <!-- 权限申请 -->
  <ApplyModal v-model:open="applyDrawerOpen" :init-data="rowInfo" />

  <!-- 移动至 -->
  <MoveDrawer
    v-model:open="moveDrawerOpen"
    :init-data="rowInfo"
    :init-params="{ position: 'DATASET', type: 'ALL', workspaceId }" />

  <!-- 授权 -->
  <AuthorizeDrawer v-model:open="authorizeDrawerOpen" :init-data="rowInfo" />
</template>

<script setup>
import { h, ref, reactive, computed, watch } from 'vue'
import {
  LeftOutlined,
  StarFilled,
  LockOutlined,
  StarOutlined,
  LineChartOutlined,
  MoreOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { tableColumns } from './config'
import { getDatasetList } from '@/apis/dataset'
import ApplyModal from '@/components/ApplyModal/index.vue'
import { MoveDrawer } from '@/components/DirTree'
import AuthorizeDrawer from '@/components/Authorize/ListDrawer.vue'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useMenus from './useMenus.jsx'
import { UIStyle } from '@/settings'

const {
  getAnalysisHref,
  openWindowWithParams,
  toAnalysis,
  edit,
  favor,
  handleExport,
  publish,
  offline,
  del,
} = useMenus()

const userStore = useUserStore()
const appStore = useAppStore()
// 空间ID
const workspaceId = computed(() => appStore.workspaceId)

const emits = defineEmits(['update:open', 'close'])
const props = defineProps({
  open: {
    type: Boolean,
  },
  initParams: {
    type: Object,
    default: () => ({}),
  },
})

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

const drawerWidth = computed(() => {
  return `calc(100% - ${UIStyle.sidebar.width})`
})
const drawerPaddingTop = computed(() => {
  return UIStyle.navbar.height
})

const keywordRef = ref()
watch(
  () => props.open,
  op => {
    if (op) {
      setTimeout(() => {
        keywordRef.value.focus()
      })

      init()
      fetchData()
    } else {
      keyword.value = ''
      errorSearched.value = false
    }
  }
)

const init = () => {
  keyword.value = props.initParams.keyword
  pager.current = 1
}

const toBack = () => {
  emits('update:open', false)
  emits('close')
}

const errorSearched = ref(false)
const keyword = ref('')
const onKeywordInput = e => {
  errorSearched.value = e.trim().length === 0
}
const onSearch = e => {
  if (!e.trim().length) {
    errorSearched.value = true

    message.warn('请输入关键字搜索')
  } else {
    errorSearched.value = false

    fetchData()
  }
}
const loading = ref(false)
const columns = computed(() => {
  const [a, ...res] = tableColumns

  res[res.length - 1]['width'] = 110

  return [
    a,
    {
      title: '文件夹路径',
      dataIndex: 'folder',
      width: 150,
    },
    ...res,
  ]
})
const list = ref([])
const pager = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
})

const queryParams = computed(() => {
  const { current: pageNum, pageSize } = pager
  const kw = keyword.value.trim()

  return {
    workspaceId: workspaceId.value,
    pageNum,
    pageSize,
    keyword: kw,
    folderType: 'ALL',
  }
})
const fetchData = async () => {
  try {
    loading.value = true

    const { total = 0, data = [] } = await getDatasetList(queryParams.value)

    list.value = data
    pager.total = total
  } catch (error) {
    console.error('获取看板列表失败', error)
  } finally {
    loading.value = false
  }
}

const setRowClassName = ({ permission, status }) => {
  return permission === 'NONE' || permission === 'EXPIRED' || status === 'OFFLINE'
    ? 'no-permission'
    : ''
}

const onTableChange = ({ current, pageSize }) => {
  pager.current = current
  pager.pageSize = pageSize

  fetchData()
}

const onShowSizeChange = (current, pageSize) => {
  pager.current = 1
  pager.pageSize = pageSize

  fetchData()
}

const onMenuClick = ({ key }, row) => {
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
    case 'publish':
      publish(row)
      break
    case 'offline':
      offline(row)
      break
    case 'delete':
      del(row, fetchData)
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

// 移动
const moveDrawerOpen = ref(false)
const move = row => {
  rowInfo.value = { ...row }
  moveDrawerOpen.value = true
}
</script>

<style lang="scss" scoped>
.errorSearched {
  :deep(.ant-input-affix-wrapper) {
    border-color: #ff4d4f !important;
  }
  :deep(.ant-input-group-addon) .ant-btn {
    border-color: #ff4d4f !important;
  }
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
