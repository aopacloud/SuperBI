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
            @search="fetchList"
          />
        </template>
      </a-tabs>
    </header>

    <main class="flex-1">
      <a-table
        style="margin: 0 10px 0"
        class="flex-1"
        size="small"
        rowKey="id"
        :sticky="{
          getContainer
        }"
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
            <div class="filter-action">
              <a-button
                size="small"
                style="width: 90px"
                @click="resetFilters(clearFilters, confirm)"
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
          </div>
        </template>

        <template #bodyCell="{ text, column, record }">
          <template v-if="column.dataIndex === 'action'">
            <a-space :size="16">
              <a @click="revertHandler(record)">恢复</a>
              <a style="color: red" @click="deleteHandler(record)"> 删除 </a>
            </a-space>
          </template>
        </template>

        <template #summary v-if="rowSelection.selectedRowKeys.length">
          <a-table-summary fixed="bottom">
            <a-table-summary-row>
              <a-table-summary-cell :index="0" :col-span="2">
                选中 {{ rowSelection.selectedRowKeys.length }} 项
                <a-button type="link" @click="resetRowSelectKeys"
                  >清空</a-button
                >
              </a-table-summary-cell>

              <a-table-summary-cell :col-span="4"></a-table-summary-cell>

              <a-table-summary-cell :index="6" fixed="right" align="right">
                <a-space :size="16">
                  <a @click="revertHandler()"> 恢复 </a>
                  <a style="color: red" @click="deleteHandler()"> 删除 </a>
                </a-space>
              </a-table-summary-cell>
            </a-table-summary-row>
          </a-table-summary>
        </template>
      </a-table>
    </main>

    <modalContext />
  </section>
</template>
<script setup lang="jsx">
import { computed, reactive, ref } from 'vue'
import { Modal, Switch as ASwitch } from 'ant-design-vue'
import useTable from '@/common/hooks/useTable/index'
import { resourceTypeMap, tableColumns } from './config.jsx'
import { getAllUser } from '@/apis/user'
import { getList, deleteByIds, postRevert } from '@/apis/system/recycleBin'

const getContainer = () => document.querySelector('#page-wrapper')

const [modal, modalContext] = Modal.useModal()

const tabActiveKey = ref('DATASET')
const onTabChange = () => {
  pager.current = 1
  filters.value = {}
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
} = useTable(getList, {
  pager: { showTotal: true },
  sorter: { field: 'updateTime', order: 'ascend' },
  initQueryParams: () => {
    return { position: tabActiveKey.value }
  },
  onSuccess() {
    rowSelection.selectedRowKeys = []
  }
})

const columns = computed(() => {
  const k = tabActiveKey.value,
    _columns = tableColumns.slice()

  if (k === 'DATASET') {
    _columns[0]['title'] = '数据集'
  } else if (k === 'DASHBOARD') {
    _columns[0]['title'] = '看板'
  } else if (k === 'REPORT') {
    _columns[0]['title'] = '图表'
  }

  const { field, order } = sorter.value || {}

  // filters.value[col.dataIndex] || null
  // 修复: Warning: [ant-design-vue: Table] Columns should all contain `filteredValue` or not contain `filteredValue`
  return _columns.map(col => {
    return {
      ...col,
      filteredValue:
        col.customFilterDropdown || col.filters
          ? filters.value[col.dataIndex] || null
          : undefined,
      sortOrder: col.dataIndex === field ? order : undefined,
      filters: typeof col.filters === 'function' ? col.filters() : col.filters
    }
  })
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

// 重置筛选
const resetFilters = (clearFilters, confirm) => {
  clearFilters()
  confirm && confirm()
}

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

// 恢复
const revertHandler = row => {
  const ids = row ? [row.id] : rowSelection.selectedRowKeys

  let modelObj = null
  // 是否关联相关的图表
  let isRevertReport = false
  const onRevertReportChange = e => {
    isRevertReport = e

    modelObj.update({ content: content() })
  }

  const resourceTypeLabel = resourceTypeMap[tabActiveKey.value]
  const content = () => (
    <div style='margin: 12px 0 16px'>
      {row?.id ? null : (
        <span>
          您正在批量恢复<b style='color: red'>{ids.length}个</b>
          {resourceTypeLabel}，
        </span>
      )}
      <span>{resourceTypeLabel}恢复后，用户权限也将自动恢复</span>
      {tabActiveKey.value === 'DATASET' ? (
        <div class='font-help' style='margin-top: 6px;'>
          同时恢复数据集相关的图表
          <ASwitch
            style='margin-left: 6px;vertical-align: bottom;'
            checked={isRevertReport}
            onChange={onRevertReportChange}
          />
        </div>
      ) : null}
    </div>
  )

  modelObj = modal.confirm({
    title: '确认恢复吗？',
    content,
    onOk() {
      return postRevert(
        { position: tabActiveKey.value, autoRestore: isRevertReport },
        ids
      )
        .then(fetchList)
        .catch(error => {
          console.error('恢复失败', error)
          return Promise.reject()
        })
    }
  })
}

// 删除
const deleteHandler = row => {
  const ids = row ? [row.id] : rowSelection.selectedRowKeys

  const resourceTypeLabel = resourceTypeMap[tabActiveKey.value]
  const content = (
    <div style='margin: 12px 0 16px'>
      您正在操作删除
      {row?.id ? (
        <span>
          {resourceTypeLabel}
          <b style='color: red'>[{row.name}]</b>
        </span>
      ) : (
        <span>
          <b style='color: red'>{ids.length}</b>个{resourceTypeLabel}
        </span>
      )}
      ，该操作<b style='color: red'>无法恢复</b>，是否确定删除该
      {resourceTypeLabel}？
    </div>
  )

  modal.confirm({
    title: '确认删除吗？',
    content,
    okType: 'danger',
    onOk() {
      return deleteByIds({ position: tabActiveKey.value }, ids)
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
</script>

<style lang="scss" scoped>
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
</style>
<style lang="scss">
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
</style>
