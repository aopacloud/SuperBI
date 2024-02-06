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
        :columns="myApplyUnderwayApplyColumns"
        @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <a v-if="column.key === 'datasetName'" @click="openDetailDrawer(record)">
            {{ record.datasetName }}
          </a>

          <template v-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" @click="openDetailDrawer(record)">查看</a>
              <a-divider type="vertical" />

              <!-- <a-popconfirm
                title="确定撤回当前申请?"
                ok-text="是"
                cancel-text="否"
                @confirm="handleApplyCancel(record)">
                <a href="#">撤回</a>
              </a-popconfirm> -->
            </a-space>
          </template>
          <template v-if="column.key === 'approveStatus'">
            <a-badge dot="true" v-bind="displayApproveStatus(record.approveStatus)" />
          </template>
        </template>
      </a-table>
    </a-tab-pane>
    <a-tab-pane key="finished" tab="已完成">
      <a-table
        :dataSource="finishedApplies"
        :pagination="pager"
        :columns="myApplyFinishApplyColumns"
        @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <a v-if="column.key === 'datasetName'" @click="openDetailDrawer(record)">
            {{ record.datasetName }}
          </a>

          <template v-if="column.key === 'action'">
            <a @click="openDetailDrawer(record)">查看</a>
          </template>
          <template v-if="column.key === 'approveStatus'">
            <a-badge dot="true" v-bind="displayApproveStatus(record.approveStatus)" />
          </template>
          <template v-if="column.key === 'authorizeStatus'">
            <a-badge dot="true" v-bind="displayAuthorizeStatus(record.authorizeStatus)" />
          </template>
        </template>
      </a-table>
    </a-tab-pane>

    <template #rightExtra>
      <a-space>
        <span
          v-if="
            tabActiveKey === 'underway' ||
            versionJs.ViewsAuthorityApply.appliedKeywordLeftTip
          "
          style="color: #bfbfbf">
          <ExclamationCircleFilled style="color: #1890ff; vertical-align: -2px" />
          {{
            tabActiveKey === 'underway'
              ? versionJs.ViewsAuthorityApply.applyingKeywordLeftTip
              : versionJs.ViewsAuthorityApply.appliedKeywordLeftTip
          }}
        </span>

        <a-input-search
          style="width: 280px"
          placeholder="请输入搜索"
          allow-clear
          v-model:value="searchKeyWord"
          @search="onSearch"></a-input-search>
      </a-space>
    </template>
  </a-tabs>

  <DetailDrawer v-model:open="detailDrawerOpen" :initData="applyDetail" from="apply" />
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { CheckCircleOutlined, ExclamationCircleFilled } from '@ant-design/icons-vue'
import {
  myApplyUnderwayApplyColumns,
  myApplyFinishApplyColumns,
  computeExpireDuration,
  displayApproveStatus,
  displayAuthorizeStatus,
} from '../config'
import { myApplyList, revoke } from '@/apis/apply'
import DetailDrawer from '../components/DetailDrawer.vue'
import { versionJs } from '@/versions'

const tabActiveKey = ref('underway')
const searchKeyWord = ref('')
const underwayQueryParam = computed(() => {
  const { current, pageSize } = pager.value

  return {
    approveStatusList: 'INIT,UNDER_REVIEW',
    pageSize: pageSize,
    pageNum: current,
    keyword: searchKeyWord.value,
    sortField: sorted.value.field || 'createTime',
    sortType: (sorted.value.order || 'descend').slice(0, -3),
  }
})
const finishedQueryParam = computed(() => {
  const { current, pageSize } = pager.value

  return {
    approveStatusList: 'PASSED,REJECTED,DELETE',
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
    myApplyList(underwayQueryParam.value).then(response => {
      underwayApplies.value = response.data
      pager.value.total = response.total
    })
  } else {
    // 已完成的列表
    myApplyList(finishedQueryParam.value).then(response => {
      finishedApplies.value = response.data
      pager.value.total = response.total
    })
  }
}

const onTableChange = (pagination, filters, sorter) => {
  pager.value.current = pagination.current
  pager.value.pageSize = pagination.pageSize

  sorted.value = sorter

  applyList()
}

const onTabChange = activeKey => {
  pager.value = JSON.parse(JSON.stringify(defaultPager))
  sorted.value = {}
  applyList()
}

// 申请详情
const applyDetail = ref({})
const detailDrawerOpen = ref(false)
const openDetailDrawer = apply => {
  applyDetail.value = { ...apply }
  detailDrawerOpen.value = true
}

const handleApplyCancel = apply => {
  revoke(apply.id).then(response => {
    applyList()
    message.success('撤回成功')
  })
}

// 生命周期
onMounted(() => {
  applyList()
})
</script>
<style lang="scss" scoped></style>
