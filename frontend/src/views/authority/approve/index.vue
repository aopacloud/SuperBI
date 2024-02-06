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
        :columns="myApproveUnderwayApplyColumns"
        @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <a v-if="column.key === 'datasetName'" @click="openDetailDrawer(record)">
            {{ record.datasetName }}
          </a>

          <a-space :size="12" v-if="column.key === 'action'">
            <a @click="openDetailDrawer(record)">查看</a>

            <a
              v-if="
                record.approveStatus === 'UNDER_REVIEW' &&
                record.authorizeStatus === 'AUTHORIZED'
              "
              @click="pass(record.id)"
              >通过
            </a>

            <a
              v-if="
                record.approveStatus === 'UNDER_REVIEW' &&
                (record.authorizeStatus === 'NOT_AUTHORIZED' ||
                  record.authorizeStatus === 'EXPIRED')
              "
              @click="passAndAuth(record)"
              >通过并授权
            </a>

            <a
              v-if="
                record.approveStatus === 'PASSED' &&
                record.authorizeStatus === 'AUTHORIZED'
              "
              @click="editAuth(record)"
              >修改
            </a>

            <a
              v-if="record.approveStatus === 'UNDER_REVIEW'"
              style="color: red"
              @click="reject(record)"
              >驳回
            </a>
          </a-space>

          <template v-if="column.key === 'expireDuration'">
            {{ computeExpireDuration(record.expireDuration) }}
          </template>
          <template v-if="column.key === 'approveStatus'">
            <a-badge v-bind="displayApproveStatus(record.approveStatus)" />
          </template>
          <template v-if="column.key === 'authorizeStatus'">
            <a-badge v-bind="displayAuthorizeStatus(record.authorizeStatus)" />
          </template>
        </template>
      </a-table>
    </a-tab-pane>
    <a-tab-pane key="finished" tab="已完成">
      <a-table
        :dataSource="finishedApplies"
        :pagination="pager"
        :columns="myApproveFinishApplyColumns"
        @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <a v-if="column.key === 'datasetName'" @click="openDetailDrawer(record)">
            {{ record.datasetName }}
          </a>

          <template v-if="column.key === 'action'">
            <a @click="openDetailDrawer(record)">查看</a>
            <!-- <template v-if="record.approveStatus === 'PASSED' && (record.authorizeStatus === 'AUTHORIZED')">
                            <a-divider type="vertical"/>
                            <a >修改</a>
                        </template> -->
          </template>
          <template v-if="column.key === 'expireDuration'">
            {{ computeExpireDuration(record.expireDuration) }}
          </template>
          <template v-if="column.key === 'approveStatus'">
            <a-badge v-bind="displayApproveStatus(record.approveStatus)" />
          </template>
          <template v-if="column.key === 'authorizeStatus'">
            <a-badge v-bind="displayAuthorizeStatus(record.authorizeStatus)" />
            <a-tooltip
              v-if="record.authorizeStatus === 'REJECTED'"
              :title="record.reason">
              <InfoCircleOutlined style="margin-left: 4px" />
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

  <!-- 详情 -->
  <DetailDrawer v-model:open="detailDrawerOpen" :initData="rowInfo" from="approve" />

  <!-- 驳回 -->
  <RejectModal v-model:open="rejectModalOpen" :initData="rowInfo" @success="applyList" />

  <!-- 授权 -->
  <AuthorDrawer
    v-model:open="authorDrawerOpen"
    :selectable="false"
    :initData="rowInfo"
    :beforeSubmit="pass"
    @success="applyList" />
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { InfoCircleOutlined } from '@ant-design/icons-vue'
import {
  myApproveUnderwayApplyColumns,
  myApproveFinishApplyColumns,
  computeExpireDuration,
  displayApproveStatus,
  displayAuthorizeStatus,
} from '../config'
import { myApproveList, pass as postPass } from '@/apis/apply'
import DetailDrawer from '../components/DetailDrawer.vue'
import AuthorDrawer from '@/components/Authorize/AuthorDrawer.vue'
import RejectModal from '@/components/RejectModal/index.vue'

const tabActiveKey = ref('underway')
const searchKeyWord = ref('')
const underwayQueryParam = computed(() => {
  const { current, pageSize } = pager.value
  const { authorizeStatus } = filter.value

  return {
    approveStatusList: 'INIT,UNDER_REVIEW',
    authorizeStatusList: authorizeStatus ? authorizeStatus.join(',') : '',
    pageSize: pageSize,
    pageNum: current,
    keyword: searchKeyWord.value,
    sortField: sorted.value.field || 'createTime',
    sortType: (sorted.value.order || 'descend').slice(0, -3),
  }
})
const finishedStatusList = 'PASSED,REJECTED,DELETE'
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
    myApproveList(underwayQueryParam.value).then(response => {
      underwayApplies.value = response.data
      pager.value.total = response.total
    })
  } else {
    // 已完成的列表
    myApproveList(finishedQueryParam.value).then(response => {
      finishedApplies.value = response.data
      pager.value.total = response.total
    })
  }
}

const onTableChange = (pagination, filters, sorter) => {
  pager.value.current = pagination.current
  pager.value.pageSize = pagination.pageSize

  sorted.value = sorter
  filter.value = filters

  applyList()
}

const onTabChange = activeKey => {
  pager.value = JSON.parse(JSON.stringify(defaultPager))
  sorted.value = {}
  filter.value = {}
  applyList()
}

const rowInfo = ref({})

// 申请详情
const detailDrawerOpen = ref(false)
const openDetailDrawer = row => {
  rowInfo.value = { ...row }
  detailDrawerOpen.value = true
}

// 通过
const pass = () => postPass(rowInfo.value._id)
// 授权
const authorDrawerOpen = ref(false)
// 通过并授权
const passAndAuth = async row => {
  rowInfo.value = {
    ...row,
    _id: row.id,
    id: undefined,
    datasetId: row.datasetId,
  }

  authorDrawerOpen.value = true
}

// 修改授权
const editAuth = row => {
  rowInfo.value = { ...row, authorized: true }
  authorDrawerOpen.value = true
}

// 驳回
const rejectModalOpen = ref(false)
const reject = row => {
  rowInfo.value = { ...row }
  rejectModalOpen.value = true
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
