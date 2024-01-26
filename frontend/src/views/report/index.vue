<template>
  <section class="page flex-column">
    <header>
      <a-tabs
        v-model:activeKey="tabActiveKey"
        type="card"
        @change="onTabChange"
        style="margin: 20px 20px 0">
        <a-tab-pane key="CREATE" tab="我创建的" />
        <a-tab-pane key="PRIVILEGE" tab="我有权限的" />
        <a-tab-pane key="FAVORITE" tab="我收藏的" />

        <template #rightExtra>
          <a-input-search
            style="width: 300px"
            placeholder="请输入搜索"
            allow-clear
            v-model:value="keyword"
            @search="fetchList" />
          <a-button
            v-permission="'REPORT:VIEW:CREATE'"
            style="margin-left: 8px"
            type="primary"
            :disabled="createDisabled"
            :loading="datasetListLoading"
            @click="toCreate">
            新建图表
          </a-button>
        </template>
      </a-tabs>
    </header>

    <main class="flex-1">
      <a-table
        size="small"
        :loading="loading"
        :columns="columns"
        :data-source="list"
        :pagination="pager"
        :scroll="{ x: 1200, y: 'auto' }"
        @change="onTableChange"
        style="margin: 0 10px 0">
        <template #bodyCell="{ text, column, record }">
          <a
            v-if="column.dataIndex === 'name'"
            :href="hasReadPermission(record) ? getDetailHref(record) : undefined"
            target="_blank">
            {{ text }}
          </a>

          <template v-if="column.dataIndex === 'dashboardCount'">
            <span v-if="!record.dashboardCount">-</span>
            <a-popover
              v-else
              placement="rightTop"
              overlayClassName="dashboardList-popover"
              @openChange="e => onDashboardCountOpenChange(e, record)">
              <template #content>
                <a-spin :spinning="record._dashboardLoading">
                  <span v-for="dashboard in record.dashboards" :key="dashboard.id">
                    <a :href="getDashboardUrl(dashboard.id)" target="_blank">
                      {{ dashboard.name }}
                    </a>
                  </span>
                </a-spin>
              </template>
              <a>{{ record.dashboardCount }}</a>
            </a-popover>
          </template>

          <template v-if="column.dataIndex === 'action'">
            <a-space style="line-height: 1">
              <a-tooltip title="收藏">
                <a-button
                  size="small"
                  type="text"
                  :style="{ color: record.favorite ? '#e6a23c' : '' }"
                  :icon="h(record.favorite ? StarFilled : StarOutlined)"
                  @click="favor(record)" />
              </a-tooltip>

              <a-tooltip v-if="hasWritePermission(record)" title="编辑" placement="top">
                <a-button
                  size="small"
                  type="text"
                  :icon="h(EditOutlined)"
                  @click="edit(record)" />
              </a-tooltip>

              <a-tooltip v-if="hasManagePermission(record)" title="删除" placement="top">
                <a-button
                  size="small"
                  type="text"
                  :icon="h(DeleteOutlined)"
                  @click="del(record)" />
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </main>
  </section>
</template>
<script setup>
import { h, ref, computed, onMounted, shallowRef, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  StarFilled,
  StarOutlined,
  EditOutlined,
  DeleteOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { getDatasetList } from '@/apis/dataset'
import { getReportList, deleteById } from '@/apis/report'
import { getDasboardsByReportId } from '@/apis/dashboard'
import { postFavorite, postUnFavorite } from '@/apis/favorite'
import { tableColumns } from './config'

import useTable from 'common/hooks/useTable'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

// 分析权限
const hasReadPermission = row => {
  if (userStore.hasPermission('REPORT:READ:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('REPORT:READ:HAS:PRIVILEGE')) {
    return row.permission === 'READ' || row.permission === 'WRITE'
  } else {
    return false
  }
}

// 编辑权限
const hasWritePermission = row => {
  if (userStore.hasPermission('REPORT:WRITE:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('REPORT:WRITE:CREATE')) {
    return row.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('REPORT:WRITE:HAS:PRIVILEGE')) {
    return row.permission === 'WRITE'
  } else {
    return false
  }
}

// 管理权限（发布，移动，下线，删除，共享，推送）
const hasManagePermission = row => {
  if (userStore.hasPermission('REPORT:MANAGE:ALL:WORKSPACE')) {
    return true
  } else if (userStore.hasPermission('REPORT:MANAGE:CREATE')) {
    return row.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('REPORT:MANAGE:HAS:PRIVILEGE')) {
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

const tabActiveKey = ref('CREATE')
const onTabChange = () => {
  reset()
  fetchList()
}

const initQueryParams = computed(() => {
  let payload = {}

  if (tabActiveKey.value === 'CREATE') {
    payload = { creator: userStore.username }
  } else if (tabActiveKey.value === 'PRIVILEGE') {
    payload = { hasPermission: 1 }
  } else if (tabActiveKey.value === 'FAVORITE') {
    payload = { favorite: 1 }
  }

  return {
    ...payload,
    workspaceId: workspaceId.value,
  }
})

const { loading, sorter, keyword, pager, list, fetchList } = useTable(getReportList, {
  keyword: '',
  sorter: { field: 'createTime', order: 'descend' },
  initQueryParams,
})

const columns = computed(() => {
  const { field, order } = sorter.value || {}

  return tableColumns.map(col => {
    return {
      ...col,
      sortOrder: col.dataIndex === field && order,
    }
  })
})

// 重置
const reset = () => {
  keyword.value = ''
  pager.current = 1
}

// 数据集列表
const datasetListLoading = ref(false)
const datasetList = shallowRef([])
const fetchDatasetList = async () => {
  try {
    datasetListLoading.value = true

    const { data = [] } = await getDatasetList({ pageSize: 10000 })

    datasetList.value = data
  } catch (error) {
    console.error('获取数据集列表错误', error)
  } finally {
    datasetListLoading.value = false
  }
}
const createDisabled = computed(() => datasetList.value.length === 0)
const toCreate = () => {
  const first = datasetList.value[0]
  if (!first) return

  const routeRes = router.resolve({ name: 'DatasetAnalysis', params: { id: first.id } })
  if (!routeRes) return

  window.open(routeRes.href, '_blank')
}

const onTableChange = ({ current, pageSize }, filters, sort) => {
  pager.current = current
  pager.pageSize = pageSize

  sorter.value = sort

  fetchList()
}

onMounted(() => {
  fetchDatasetList()
  fetchList()
})

// 收藏\取消收藏
const favor = async row => {
  if (row.loading) return

  try {
    const isFavorite = row.favorite
    const fn = isFavorite ? postUnFavorite : postFavorite

    row.loading = true
    await fn({ position: 'REPORT', targetId: row.id })

    row.favorite = !isFavorite
    message.success(`${isFavorite ? '取消收藏' : '收藏'}成功`)
  } catch (error) {
    console.error('收藏\取消收藏错误', error)
  } finally {
    row.loading = false
  }
}

// 编辑
const edit = row => {
  const url = getDetailHref(row)

  window.open(url, '_blank')
}

// 删除
const del = row => {
  Modal.confirm({
    title: '提示',
    content: '确定删除图表【 ' + row.name + ' 】?',
    okType: 'danger',
    async onOk() {
      await deleteById(row.id)

      message.success('删除成功')
      fetchList()
    },
  })
}

// 获取详情href
const getDetailHref = row => {
  const routeRes = router.resolve({ name: 'ReportDetail', params: { id: row.id } })
  if (!routeRes) return '/'

  return routeRes.href
}

// 被应用的看板
const onDashboardCountOpenChange = async (visible, row) => {
  if (!visible || row._dashboardLoading || row._dashboardLoaded) return

  try {
    row._dashboardLoading = true

    const res = await getDasboardsByReportId({ reportId: row.id })

    row.dashboards = res || []
    row._dashboardLoaded = true
  } catch (error) {
    console.error('图表应用看板获取错误', error)
  } finally {
    row._dashboardLoading = false
  }
}

// 获取看板地址
const getDashboardUrl = id => {
  if (!id) return '/'

  const routeRes = router.resolve({ name: 'DashboardPreview', params: { id } })
  if (!routeRes) return '/'

  return routeRes.href
}
</script>

<style lang="scss">
.dashboardList-popover {
  .ant-popover-content {
    max-height: 600px;
  }
}
</style>
