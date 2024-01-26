<template>
  <a-drawer title="共享设置" width="700" :open="open" @close="close">
    <section>
      <header>
        <div class="item">
          <div class="item-label">看板：</div>
          <div class="item-content">
            <b>{{ initData.name }}</b>
          </div>
        </div>
        <div class="item">
          <div class="item-label">共享范围：</div>
          <div class="item-content">
            <a-radio-group v-model:value="rangeValue">
              <a-radio :value="1">指定用户</a-radio>
              <a-radio :value="0">仅自己可见</a-radio>
            </a-radio-group>
          </div>
        </div>
      </header>
      <main style="margin-top: 16px" v-if="rangeValue === 1">
        <a-tabs v-model:activeKey="activeKey">
          <a-tab-pane key="USER" tab="用户" />
          <a-tab-pane key="GROUP" tab="用户组" />
          <template #rightExtra>
            <a-input-search
              style="width: 240px"
              placeholder="请输入搜索"
              allow-clear
              v-model:value="keyword"
              @search="onKeywordSearch" />
            <a-button
              style="margin-left: 8px"
              type="primary"
              :icon="h(PlusOutlined)"
              @click="add">
              授权{{ isUserKey ? '用户' : '用户组' }}
            </a-button>
          </template>
        </a-tabs>

        <a-table
          size="small"
          rowKey="id"
          :loading="loading"
          :columns="columns"
          :data-source="list"
          :pagination="pager"
          @change="onTableChange">
          <template #bodyCell="{ text, column, record }">
            <a-select
              v-if="column.dataIndex === 'permission'"
              size="small"
              style="width: 90px"
              :value="text"
              :loading="record.loading"
              @change="e => onRoleChange(e, record)">
              <a-select-option value="READ">查看者</a-select-option>
              <a-select-option value="WRITE">协作者</a-select-option>
            </a-select>

            <a-button
              v-if="column.dataIndex === 'action'"
              size="small"
              type="text"
              :icon="h(record.deleteLoading ? LoadingOutlined : MinusCircleOutlined)"
              @click="deleteRecord(record)" />
          </template>
        </a-table>
      </main>
    </section>

    <template #footer>
      <a-space style="float: right">
        <a-button @click="close">退出</a-button>
        <a-button v-if="rangeValue === 0" type="primary" @click="onOnlyOwnOk">
          确认
        </a-button>
      </a-space>
    </template>

    <SelectListModal
      v-model:open="selectModalOpen"
      :title="`添加${isUserKey ? '用户' : '用户组'}`"
      :loading="selectListLoading"
      :data-source="selectListDataSource"
      :keyField="isUserKey ? 'username' : 'id'"
      :labelField="isUserKey ? 'aliasName' : 'name'"
      :confirmFn="selectModalConfirm" />
  </a-drawer>
</template>

<script setup>
import { h, ref, reactive, shallowRef, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { LoadingOutlined, MinusCircleOutlined, PlusOutlined } from '@ant-design/icons-vue'
import useAppStore from '@/store/modules/app'
import { SelectListModal } from 'common/components/ExtendSelect'
import {
  getSharedUser,
  getSharedGroup,
  postShare,
  putShare,
  deleteShareRecordById,
  deleteShareById,
} from '@/apis/dashboard'
import { getUserByWorkspaceId } from '@/apis/user'
import { getRoleByWorkspaceId } from '@/apis/role'

const appStore = useAppStore()
const workspaceId = computed(() => appStore.workspaceId)

// 表格列配置
const tableColumns = [
  { title: '权限类型', dataIndex: 'permission', width: 150 },
  { title: '添加时间', dataIndex: 'createTime', width: 200 },
  { title: '操作', dataIndex: 'action', width: 60, align: 'right' },
]
// 用户列
const userColumn = { title: '用户', dataIndex: 'usernameAlias' }
// 用户组列
const gruopColumn = { title: '用户组', dataIndex: 'roleName' }

const emits = defineEmits(['update:open', 'ok'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
})

const rangeValue = ref(0) // 0 仅自己可见，1 指定用户
const activeKey = ref('USER') // 'USER' 用户、 'GROUP' 用户组

const isUserKey = computed(() => activeKey.value === 'USER')

const reset = () => {
  keyword.value = ''
  activeKey.value = 'USER'
}
const init = async () => {
  try {
    loading.value = true

    userPager.current = 1
    groupPager.current = 1

    await Promise.all([fetchSharedUser(), fetchSharedGroup()]).then(res => {
      rangeValue.value = Math.max(...res) > 0 ? 1 : 0
    })
  } catch (error) {
    console.error('获取看板共享列表错误', error)
  } finally {
    loading.value = false
  }
}
watch(
  () => props.open,
  open => {
    if (open) {
      init()
    } else {
      reset()
    }
  }
)

// 关键字
const keyword = ref('')
const onKeywordSearch = () => {
  if (isUserKey.value) {
    userPager.current = 1
    fetchSharedUser()
  } else {
    groupPager.current = 1
    fetchSharedGroup()
  }
}

// 查询参数
const queryParams = computed(() => {
  const { current: pageNum, pageSize } = pager.value
  const kw = keyword.value?.trim()

  return {
    dashboardId: props.initData.id,
    workspaceId: workspaceId.value,
    pageNum,
    pageSize,
    keyword: kw,
  }
})

// 用户
const userList = ref([])
const userPager = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})
// 获取共享的用户列表
const fetchSharedUser = async () => {
  try {
    loading.value = true

    const { total = 0, data = [] } = await getSharedUser(queryParams.value)

    userPager.total = total
    userList.value = data
    return total
  } catch (error) {
    console.error('获取共享用户列表错误', error)
  } finally {
    loading.value = false
  }
}

// 用户组
const groupList = ref([])
const groupPager = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})
// 获取共享的用户组
const fetchSharedGroup = async () => {
  try {
    loading.value = true

    const { total = 0, data = [] } = await getSharedGroup(queryParams.value)

    groupPager.total = total
    groupList.value = data

    return total
  } catch (error) {
    console.error('获取共享用户组列表错误', error)
  } finally {
    loading.value = false
  }
}
const loading = ref(false)
// 渲染列
const columns = computed(() => {
  return isUserKey.value ? [userColumn, ...tableColumns] : [gruopColumn, ...tableColumns]
})
// 渲染列表
const list = computed(() => {
  return isUserKey.value ? userList.value : groupList.value
})
// 渲染分页
const pager = computed(() => {
  return isUserKey.value ? userPager : groupPager
})

const onTableChange = ({ current, pageSize }) => {
  if (isUserKey.value) {
    userPager.current = current
    userPager.pageSize = pageSize

    fetchSharedUser()
  } else {
    groupPager.current = current
    groupPager.pageSize = pageSize

    fetchSharedGroup()
  }
}

const selectListLoading = ref(false)
const selectListUsers = shallowRef([])
const selectListGroup = shallowRef([])
const fetchUsers = async () => {
  try {
    selectListLoading.value = true
    const { data = [] } = await getUserByWorkspaceId({
      workspaceId: workspaceId.value,
      pageSize: 10000,
    })

    selectListUsers.value = data
  } catch (error) {
    console.error('获取用户错误', error)
  } finally {
    selectListLoading.value = false
  }
}
const fetchGroups = async () => {
  try {
    selectListLoading.value = true
    const { data = [] } = await getRoleByWorkspaceId({
      workspaceId: workspaceId.value,
      pageSize: 10000,
    })

    selectListGroup.value = data
  } catch (error) {
    console.error('获取用户组错误', error)
  } finally {
    selectListLoading.value = false
  }
}

const selectListDataSource = computed(() => {
  if (isUserKey.value) {
    return selectListUsers.value.map(t => {
      return {
        ...t,
        disabled: useListAll.value.some(v => v.username === t.username),
      }
    })
  } else {
    return selectListGroup.value.map(t => {
      return {
        ...t,
        disabled: groupListAll.value.some(v => v.roleId === t.id),
      }
    })
  }
})

const selectModalOpen = ref(false)
const add = () => {
  selectModalOpen.value = true

  if (isUserKey.value) {
    if (!selectListUsers.value.length) {
      fetchUsers()
    }

    fetchUserAll()
  } else {
    if (!selectListGroup.value.length) {
      fetchGroups()
    }

    fetchGroupAll()
  }
}

// 已经共享的所有用户
const useListAll = shallowRef([])
const fetchUserAll = () => {
  getSharedUser({ dashboardId: props.initData.id, pageSize: 10000 }).then(r => {
    useListAll.value = r.data
  })
}
// 已经共享的所有用户组
const groupListAll = shallowRef([])
const fetchGroupAll = () => {
  getSharedGroupgetSharedUser({ workspaceId: workspaceId.value, pageSize: 10000 }).then(
    r => {
      groupListAll.value = r.data
    }
  )
}

// 选择确认
const selectModalConfirm = async e => {
  try {
    const payload = {
      dashboardId: props.initData.id,
      [isUserKey.value ? 'usernames' : 'roleIds']: e,
    }

    const res = await postShare(payload)

    if (isUserKey.value) {
      userList.value = [...res, ...userList.value]
    } else {
      groupList.value = [...res, ...groupList.value]
    }

    return res
  } catch (error) {
    console.error('共享错误', error)
  }
}

// 角色权限切换
const onRoleChange = async (permission, row) => {
  if (row.loading) return

  try {
    row.loading = true

    const { permission: pms } = await putShare(row.id, { permission })

    row.permission = pms

    message.success('共享权限修改成功')
  } catch (error) {
    console.error('共享权限修改错误', error)
  } finally {
    row.loading = false
  }
}

// 删除共享权限
const deleteRecord = async row => {
  if (row.deleteLoading) return

  try {
    row.deleteLoading = true

    await deleteShareRecordById(row.id)

    if (isUserKey.value) {
      const index = userList.value.find(t => t.username === row.username)

      userList.value.splice(index, 1)
    } else {
      const index = groupList.value.find(t => t.id === row.id)

      groupList.value.splice(index, 1)
    }

    message.success('共享删除成功')
  } catch (error) {
    console.error('共享移除错误', error)
  } finally {
    row.deleteLoading = false
  }
}

const close = () => {
  emits('update:open', false)
  reset()
}

const onlyLoading = ref(false)
const onOnlyOwnOk = async () => {
  try {
    onlyLoading.value = true
    await deleteShareById({ dashboardId: props.initData.id })

    message.success('取消共享成功')
    close()
  } catch (error) {
    console.error('取消共享错误', error)
  } finally {
    onlyLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.item {
  display: flex;
  margin-bottom: 16px;
  &-label {
    width: 100px;
    text-align: right;
  }
  &-content {
    flex: 1;
  }
}
</style>
