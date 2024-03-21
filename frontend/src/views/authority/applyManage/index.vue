<template>
  <a-tabs
    v-model:activeKey="tabActiveKey"
    type="card"
    @change="onTabChange"
    style="margin: 20px">
    <a-tab-pane key="underway" tab="进行中">
      <a-table
        :dataSource="underwayApplies"
        :pagination="pager"
        :columns="applyManageUnderwayApplyColumns"
        @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <a v-if="column.key === 'datasetName'" @click="openDetailDrawer(record)">
            {{ record.datasetName }}
          </a>

          <template v-if="column.key === 'action'">
            <a @click="openDetailDrawer(record)">查看</a>
          </template>
          <template v-if="column.key === 'approveStatus'">
            <a-badge v-bind="displayApproveStatus(record.approveStatus)" />
          </template>
        </template>
      </a-table>
    </a-tab-pane>
    <a-tab-pane key="finished" tab="已完成">
      <a-table
        :dataSource="finishedApplies"
        :pagination="pager"
        :columns="applyManageFinishApplyColumns"
        :scroll="{ x: 1200, y: 'auto' }"
        @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <a v-if="column.key === 'datasetName'" @click="openDetailDrawer(record)">
            {{ record.datasetName }}
          </a>

          <template v-if="column.key === 'action'">
            <a @click="openDetailDrawer(record)">查看</a>

            <template
              v-if="
                record.authorizeStatus === 'AUTHORIZED' ||
                (record.approveStatus === 'PASSED' &&
                  record.authorizeStatus === 'EXPIRED')
              ">
              <a-divider type="vertical" />
              <a @click="authorize({ ...record, authorized: true })">修改 </a>
            </template>

            <template
              v-if="
                record.approveStatus === 'PASSED' &&
                record.authorizeStatus === 'NOT_AUTHORIZED'
              ">
              <a-divider type="vertical" />
              <a @click="authorize(record)">授权</a>

              <a-divider type="vertical" />
              <a style="color: red" @click="reject(record)">驳回</a>
            </template>
          </template>

          <template v-if="column.key === 'approveStatus'">
            <a-badge v-bind="displayApproveStatus(record.approveStatus)" />
          </template>

          <template v-if="column.key === 'authorizeStatus'">
            <a-badge v-bind="displayAuthorizeStatus(record.authorizeStatus)" />

            <a-tooltip
              v-if="record.authorizeStatus === 'REJECTED'"
              :title="record.authorizeRemark">
              <InfoCircleOutlined style="margin-left: 6px" />
            </a-tooltip>
          </template>
        </template>
      </a-table>
    </a-tab-pane>

    <template #rightExtra>
      <a-input-search
        placeholder="请输入搜索"
        allow-clear
        v-model:value="searchKeyWord"
        @search="onSearch"></a-input-search>
    </template>
  </a-tabs>

  <DetailDrawer from="manage" v-model:open="detailDrawerOpen" :initData="rowInfo">
    <template #footer>
      <a-button
        v-if="rowInfo.approveStatus === 'PASSED'"
        style="float: right"
        type="primary"
        @click="authorizeFromDrawer"
        >去授权
      </a-button>
    </template>
  </DetailDrawer>

  <!-- 授权 -->
  <AuthorDrawer
    v-model:open="authorDrawerOpen"
    :workspaceId="rowInfo.workspaceId"
    :selectable="false"
    :datasetIds="[rowInfo.datasetId]"
    :initData="rowInfo"
    @success="applyList" />

  <!-- 驳回 -->
  <RejectModal
    mode="AUTHORIZE"
    v-model:open="rejectModalOpen"
    :initData="rowInfo"
    @success="applyList"></RejectModal>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { InfoCircleOutlined } from '@ant-design/icons-vue'
import {
  applyManageUnderwayApplyColumns,
  applyManageFinishApplyColumns,
  displayApproveStatus,
  displayAuthorizeStatus,
} from '../config'
import { applyManageList } from '@/apis/apply'
import DetailDrawer from '../components/DetailDrawer.vue'
import AuthorDrawer from '@/components/Authorize/AuthorDrawer/index.vue'
import RejectModal from '@/components/RejectModal/index.vue'

const tabActiveKey = ref('finished')
const searchKeyWord = ref('')

const finishedStatusList = 'PASSED,REJECTED,DELETE'

const underwayQueryParam = computed(() => {
  const { current, pageSize } = pager.value
  return {
    approveStatusList: 'INIT,UNDER_REVIEW',
    pageSize: pageSize,
    pageSize: pageSize,
    pageNum: current,
    keyword: searchKeyWord.value,
    sortField: sorted.value.field || 'createTime',
    sortType: (sorted.value.order || 'descend').slice(0, -3),
  }
})
const finishedQueryParam = computed(() => {
  const { current, pageSize } = pager.value
  const { approveStatus, authorizeStatus } = filter.value
  return {
    approveStatusList: approveStatus ? approveStatus.join(',') : finishedStatusList,
    authorizeStatusList: authorizeStatus ? authorizeStatus.join(',') : '',
    pageSize: pageSize,
    pageNum: current,
    keyword: searchKeyWord.value,
    sortField: sorted.value.field || 'createTime',
    sortType: (sorted.value.order || 'descend').slice(0, -3),
  }
})
const defaultPager = { pageSize: 10, current: 1, total: 0, showSizeChanger: true }

// 分页
const pager = ref({ pageSize: 10, current: 1, total: 0, showSizeChanger: true })

// 排序
const sorted = ref({})

// 过滤
const filter = ref({})

// 进行中
const underwayApplies = ref([])

// 已完成
const finishedApplies = ref([])

const onSearch = () => {
  if (tabActiveKey.value === 'underway') {
    underwayQueryParam.value.keyword = searchKeyWord.value
  } else {
    finishedQueryParam.value.keyword = searchKeyWord.value
  }
  applyList()
}

const applyList = () => {
  // 进行中的列表
  if (tabActiveKey.value === 'underway') {
    applyManageList(underwayQueryParam.value).then(response => {
      underwayApplies.value = response.data
      pager.value.total = response.total
    })
  } else {
    // 已完成的列表
    applyManageList(finishedQueryParam.value).then(response => {
      finishedApplies.value = response.data
      pager.value.total = response.total
    })
  }
}

const onTableChange = (pagination, filters, sorter) => {
  pager.value.current = pagination.current
  pager.value.pageSize = pagination.pageSize

  sorted.value = sorter

  if (tabActiveKey.value === 'finished') {
    filter.value = filters
  }
  applyList()
}

const onTabChange = activeKey => {
  pager.value = JSON.parse(JSON.stringify(defaultPager))
  sorted.value = {}

  applyList()
}

// 申请详情
const rowInfo = ref({})
const detailDrawerOpen = ref(false)
const openDetailDrawer = row => {
  rowInfo.value = { ...row }
  detailDrawerOpen.value = true
}

// 授权驳回
const rejectModalOpen = ref(false)
const reject = row => {
  rowInfo.value = { ...row }
  rejectModalOpen.value = true
}

// 授权
const authorDrawerOpen = ref(false)
const authorize = row => {
  const { authorized, datasetId, username, datasetName } = row

  rowInfo.value = { authorized, datasetId, username, name: datasetName }
  authorDrawerOpen.value = true
}

const authorizeFromDrawer = () => {
  detailDrawerOpen.value = false
  authorize(rowInfo.value)
}

// 生命周期
onMounted(() => {
  applyList()
})
</script>
<style lang="scss" scoped>
.info-list {
  .info-item {
    display: flex;
    margin-bottom: 8px;
    &--name:after {
      content: ':';
    }
    &--text {
      flex: 1;
      margin-left: 10px;
    }
  }
}
</style>
