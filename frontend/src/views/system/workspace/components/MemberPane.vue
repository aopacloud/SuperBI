<template>
  <a-table
    :loading="loading"
    :columns="MEMBER_TABLE_COLUMNS"
    :dataSource="list"
    :pagination="pager"
    @change="onTableChange">
    <template #bodyCell="{ column, record }">
      <a-select
        v-if="column.dataIndex === 'sysRoleId'"
        style="width: 100%"
        placeholder="请选择角色"
        :disabled="!hasCurrentUserAdd"
        :loading="record.loading"
        :value="record.sysRoleId"
        @change="e => onRoleChange(e, record)">
        <a-select-option v-for="item in roles" :key="item.id">
          {{ item.name }}
        </a-select-option>
      </a-select>

      <template v-if="column.dataIndex === 'action'">
        <a-popconfirm v-if="hasCurrentUserAdd" title="确定删除" @confirm="del(record)">
          <a style="color: red">删除</a>
        </a-popconfirm>
        <span v-else>-</span>
      </template>
    </template>
  </a-table>

  <SelectListModal
    title="添加成员"
    keyField="username"
    labelField="aliasName"
    :data-source="sysUsers"
    v-model:open="selectListModalOpen"
    :confirmFn="onSelectModalOk" />
</template>

<script setup>
import { message } from 'ant-design-vue'
import { SelectListModal } from 'common/components/ExtendSelect'
import useTable from 'common/hooks/useTable'

import { getAllUser } from '@/apis/user'
import { getRoleList } from '@/apis/system/role'
import {
  getWorkspaceMembers,
  deleteWorkspaceMembers,
  putWorkspaceMembers,
  getWorkspaceAllMembers,
  postWorkspaceMembers,
} from '@/apis/workspace/member'

import { MEMBER_TABLE_COLUMNS } from './config'
import { onMounted, watch } from 'vue'

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

const hasCurrentUserAdd = computed(() => {
  return props.permissions.includes('WORKSPACE:VIEW:MANAGE:USER')
})

const { loading, pager, list, fetchList, onTableChange } = useTable(getWorkspaceMembers, {
  sorter: null,
  initQueryParams: () => ({
    workspaceId: props.workspaceId,
    keyword: props.keyword,
  }),
})

// 系统角色
const roles = ref([])
const fetchSysRoles = async () => {
  try {
    const res = await getRoleList()

    roles.value = res
  } catch (error) {
    console.error('系统角色获取失败', error)
  }
}

// 角色切换
const onRoleChange = async (e, row) => {
  try {
    row.loading = true

    const { sysRoleId } = await putWorkspaceMembers(
      row.id,
      { ...row, sysRoleId: e },
      { workspaceId: props.workspaceId }
    )

    row.sysRoleId = sysRoleId
    message.success('角色修改成功')
  } catch (error) {
    console.error('角色修改失败', error)
  } finally {
    row.loading = false
  }
}
// 删除用户
const del = async row => {
  try {
    await deleteWorkspaceMembers(row.id, { workspaceId: props.workspaceId })

    message.success('成员删除成功')
    fetchList()
  } catch (error) {
    console.error('成员删除失败', error)
  }
}

const fetchSysUsers = async () => {
  getAllUser({ pageSize: 10000 })
    .then(({ data = [] }) => {
      console.log('1', 1)

      sysUsers.value = data
    })
    .catch(e => {
      console.error('获取系统所有用户失败', e)
    })
}

// 系统用户
const sysUsers = ref([])
const selectListLoading = ref(false)
const selectListModalOpen = ref(false)
const insert = async () => {
  try {
    selectListModalOpen.value = true
    selectListLoading.value = true

    const { data = [] } = await getWorkspaceAllMembers({ workspaceId: props.workspaceId })

    sysUsers.value = sysUsers.value.map(user => {
      return {
        ...user,
        disabled: data.some(t => t.username === user.username),
      }
    })
  } catch (error) {
    console.error('获取空间内用户失败', error)
  } finally {
    selectListLoading.value = false
  }
}
const onSelectModalOk = async users => {
  try {
    if (users.length === 0) return

    const payload = users.map(user => {
      return {
        workspaceId: props.workspaceId,
        username: user,
      }
    })

    await postWorkspaceMembers(payload)

    selectListModalOpen.value = false
    fetchList()
  } catch (error) {
    console.error('添加用户失败', error)
  }
}

const init = () => {
  pager.current = 1

  fetchList()
}

defineExpose({ init, insert })

watch(() => props.workspaceId, fetchList)

onMounted(() => {
  fetchSysUsers()
  fetchSysRoles()
})
</script>

<style lang="scss" scoped></style>
