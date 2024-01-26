<template>
  <section class="page section">
    <header class="header flex justify-between">
      <h2 class="title">成员列表</h2>

      <a-space>
        <a-input-search
          v-model:value="keyword"
          placeholder="请输入搜索"
          style="width: 240px"
          allow-clear
          @search="fetchList" />
        <a-button
          type="primary"
          :icon="h(PlusOutlined)"
          @click="addUsersModalOpen = true">
          成员
        </a-button>
      </a-space>
    </header>

    <main style="margin-top: 24px">
      <a-table
        bordered
        size="middle"
        :pagination="pager"
        :columns="columns"
        :data-source="list"
        :loading="loading"
        :scroll="{ x: 1200 }"
        @change="onTableChange">
        <template #bodyCell="{ text, record, column }">
          <template v-if="column.dataIndex === 'aliasName'">
            <a-input
              v-if="editableInfo.id === record.id"
              v-model:value="editableInfo.aliasName"
              style="margin: -5px 0" />
            <template v-else>
              {{ text }}
            </template>
          </template>

          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <span v-if="editableInfo.id === record.id">
                <a @click="saveAliasModify(record.id)">保存</a>
                <a-popconfirm
                  title="是否确定取消?"
                  @confirm="cancelAliasModify(record.id)">
                  <a style="margin-left: 8px">取消</a>
                </a-popconfirm>
              </span>
              <span v-else>
                <span v-if="record.username === 'Admin'">-</span>
                <a v-else @click="editAliasName(record)">编辑</a>
              </span>

              <a-divider type="vertical" />

              <a-dropdown :trigger="['click']">
                <a style="margin-right: 6px">更多 <DownOutlined /></a>
                <template #overlay>
                  <a-menu @click="e => onMoreClick(e, record)">
                    <a-menu-item key="resetPassword">重置密码</a-menu-item>
                    <a-menu-item key="transferResource">移交资源</a-menu-item>
                    <a-menu-item
                      v-if="record.username !== 'Admin'"
                      key="del"
                      style="color: red">
                      删除用户
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </main>
  </section>

  <!-- 移交资源 -->
  <TransferModal
    :mode="transferModalMode"
    :initData="rowInfo"
    v-model:open="transferModalOpen" />

  <!-- 新增成员 -->
  <InsertUserModal v-model:open="addUsersModalOpen" @ok="fetchList" />
</template>

<script setup lang="jsx">
import { h, ref, computed, onMounted } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { PlusOutlined, DownOutlined } from '@ant-design/icons-vue'
import TransferModal from './components/TransferModal.vue'
import InsertUserModal from './components/InsertUserModal.vue'
import {
  searchSysUser,
  checkExistResources,
  deleteUser,
  updateUser,
  resetPassword,
} from '@/apis/system/user'
import { tableColumns } from './config'
import useTable from '@/common/hooks/useTable'
import { downloadWithBlob } from '@/common/utils/file'
import dayjs from 'dayjs'

const { loading, keyword, pager, sorter, list, fetchList, onTableChange } = useTable(
  searchSysUser,
  {
    sorter: {
      field: 'createTime',
      order: 'descend',
    },
    pager: {
      pageSize: 10,
      showTotal: total => `总共 ${total} 项`,
      showQuickJumper: true,
    },
  }
)

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

// 生命周期
onMounted(fetchList)

// 编辑中的信息
const editableInfo = ref({})
// 修改显示名称
const editAliasName = row => {
  editableInfo.value = { ...row }
}
// 取消修改显示名称
const cancelAliasModify = id => {
  editableInfo.value = {}
}
// 保存显示名称
const saveAliasModify = async id => {
  try {
    const data = await updateUser({ ...editableInfo.value })

    let originItem = list.value.find(t => t.id === id)
    originItem.aliasName = data.aliasName
    message.success('显示名修改成功')
  } catch (error) {
    console.error('修改用户别名错误', error)
  } finally {
    editableInfo.value = {}
  }
}

const onMoreClick = ({ key }, row) => {
  switch (key) {
    case 'resetPassword':
      resetPwd(row)
      break
    case 'transferResource':
      openTransferModal(row)
      break
    case 'del':
      handleDeleteUser(row)
      break
    default:
      break
  }
}

// 重置密码
const resetPwd = record => {
  Modal.confirm({
    title: '重置密码',
    type: 'warning',
    content: <div style='color: red'>确定重置密码？</div>,
    async onOk() {
      const res = await resetPassword(record)
      await downloadWithBlob(
        res,
        `用户信息-${dayjs().format('YYYY-MM-DD HH-mm-ss')}.xlsx`
      )
      message.success('重置密码成功')
    },
  })
}

const rowInfo = ref({})

// 移交资源弹窗
const transferModalOpen = ref(false)
const transferModalMode = ref('transfer')
// 移交
const openTransferModal = row => {
  rowInfo.value = { ...row }
  transferModalMode.value = 'transfer'
  transferModalOpen.value = true
}

// 删除用户
const handleDeleteUser = async row => {
  const username = row.username
  // 检查是否存在资源
  const hasRes = await checkExistResources({ username })

  if (hasRes) {
    // 用户下有资源，弹窗提示，可将资源移交给其他用户
    rowInfo.value = { ...row }
    transferModalMode.value = 'delete'
    transferModalOpen.value = true
  } else {
    // 用户下没有资源，直接删除
    Modal.confirm({
      title: '删除成员',
      type: 'warning',
      content: <div style='color: red'>删除后不可访问系统，确定删除该成员？</div>,
      onOk: () =>
        deleteUser({ username }).then(() => {
          message.success('删除成功')
          fetchList()
        }),
    })
  }
}

// 新增成员弹窗
const addUsersModalOpen = ref(false)
</script>

<style scoped lang="scss">
.section {
  padding: 20px;
}
.header {
  // padding: 20px 24px;
  .title {
    margin: 0;
  }
}
.editable-row-operations a {
  margin-right: 8px;
}
</style>
