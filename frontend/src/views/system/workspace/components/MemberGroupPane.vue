<template>
  <a-table
    :scroll="{ x: 1200 }"
    :loading="loading"
    :columns="ROLE_TABLE_COLUMNS"
    :dataSource="list"
    :pagination="pager"
    @change="onTableChange">
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'action'">
        <a-space v-if="hasCurrentGruopAdd">
          <a @click="edit(record)">编辑</a>
          <a @click="onUserManage(record)">成员管理</a>
          <a-popconfirm title="确定删除" @confirm="del(record)">
            <a style="color: red">删除</a>
          </a-popconfirm>
        </a-space>
        <span v-else>-</span>
      </template>
    </template>
  </a-table>

  <!-- 成员组新增、编辑 -->
  <GroupModifyModal
    v-model:open="groupModifyModalOpen"
    :workspace-id="workspaceId"
    :initData="groupInfo"
    @ok="onGroupModifyModalOk" />

  <SelectListModal
    title="成员管理"
    keyField="username"
    labelField="aliasName"
    class="role-member-select-modal"
    width="600px"
    :loading="userSelectLoading"
    :data-source="alluers"
    :value="groupInfo.users"
    v-model:open="userSelectModalOpen"
    :confirmFn="onGroupUserModalOk">
    <template #default="{ value, setValue }">
      <div class="select-preview">
        <a-tag
          class="preview-tag"
          color="blue"
          closable
          v-for="v in value"
          :key="v"
          @close="setValue(value.filter(t => t !== v))"
          >{{ displaySelectedTag(v) }}</a-tag
        >
      </div>
    </template>
  </SelectListModal>
</template>

<script setup>
import { watch } from 'vue'
import { message } from 'ant-design-vue'
import useTable from 'common/hooks/useTable'
import GroupModifyModal from './GroupModifyModal.vue'
import { SelectListModal } from 'common/components/ExtendSelect'
import { getRoleList, deleteRole } from '@/apis/workspace/role'
import { getUserByRoleId, posUsersByRoleId } from '@/apis/role'
import { getUserByWorkspaceId } from '@/apis/user'
import { ROLE_TABLE_COLUMNS } from './config'
import { findBy } from '@/common/utils/help'

const props = defineProps({
  workspaceId: {
    type: Number,
  },
  keyword: {
    type: String,
  },
  permissions: {
    type: Array,
    default: () => [],
  },
})

// 成员组管理权限
const hasCurrentGruopAdd = computed(() => {
  return props.permissions.includes('WORKSPACE:VIEW:MANAGE:ROLE')
})

const { loading, pager, list, fetchList, onTableChange } = useTable(getRoleList, {
  keyword: props.keyword,
  initQueryParams: () => ({
    workspaceId: props.workspaceId,
    keyword: props.keyword,
  }),
})

// 成员组编辑
const groupModifyModalOpen = ref(false)
const groupInfo = ref({})
const onGroupModifyModalOk = e => {
  if (!groupInfo.value.id) {
    fetchList()
  } else {
    let index = list.value.findIndex(t => t.id === e.id)

    if (index > -1) {
      const item = list.value[index]

      list.value[index] = { ...item, ...e }
    }
  }
}
// 创建成员组
const insert = () => {
  groupInfo.value = {}
  groupModifyModalOpen.value = true
}
// 编辑成员组
const edit = row => {
  groupInfo.value = { ...row }
  groupModifyModalOpen.value = true
}
// 删除成员组
const del = async row => {
  try {
    await deleteRole(row.id, { workspaceId: props.workspaceId })

    message.success('成员组删除成功')
    fetchList()
  } catch (error) {
    console.error('成员删除失败', error)
  }
}

// 获取空间内所有用户
const alluers = ref([])
const fetchWorkspaceUsers = async () => {
  try {
    const { data = [] } = await getUserByWorkspaceId({
      workspaceId: props.workspaceId,
      pageSize: 10000,
    })

    alluers.value = data
  } catch (error) {
    console.error('获取空间用户失败', error)
  }
}

const displaySelectedTag = e => {
  const item = findBy(alluers.value, t => t.username === e)

  return item ? item.aliasName : e
}

const userSelectLoading = ref(false)
const userSelectModalOpen = ref(false)

/**
 * 用户组成员管理
 * @param {*} row
 */
const onUserManage = async row => {
  try {
    userSelectLoading.value = true

    const [, r2] = await Promise.all([
      fetchWorkspaceUsers(),
      getUserByRoleId(row.id, {
        workspaceId: props.workspaceId,
      }),
    ])

    groupInfo.value = { ...row, users: r2 }
    userSelectModalOpen.value = true
  } catch (error) {
    console.error('获取成员组成员失败', error)
  } finally {
    userSelectLoading.value = false
  }
}

// 成员管理ok
const onGroupUserModalOk = async e => {
  try {
    const res = await posUsersByRoleId(groupInfo.value.id, e, {
      workspaceId: props.workspaceId,
    })

    const item = list.value.find(t => t.id === groupInfo.value.id)
    if (!item) return

    item.userNum = res.length
  } catch (error) {
    console.error('空间内用户组添加用户失败', error)
  }
}

const init = () => {
  pager.current = 1
  fetchList()
}

defineExpose({ insert, init })

watch(
  () => props.workspaceId,
  id => {
    if (!id) return

    fetchList()
  },
  { immediate: true }
)
</script>

<style lang="scss">
.role-member-select-modal {
  .ant-modal-body {
    display: flex;
  }
  .select-preview {
    width: 320px;
    max-height: 520px;
    margin-left: 10px;
    overflow: auto;
    .preview-tag {
      margin-bottom: 6px;
    }
  }
}
</style>
