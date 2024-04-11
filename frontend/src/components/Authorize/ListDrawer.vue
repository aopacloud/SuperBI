<template>
  <a-drawer title="授权" width="900" :open="open" @close="close">
    <section>
      <header>
        数据集:
        <h3 style="display: inline-block">{{ initData.name }}</h3>
      </header>

      <a-tabs v-model:activeKey="activeKey" @change="onTabChange">
        <a-tab-pane key="USER" tab="用户"></a-tab-pane>
        <a-tab-pane key="GROUP" tab="用户组"></a-tab-pane>

        <template #rightExtra>
          <a-input-search
            style="width: 200px"
            placeholder="请输入关键字搜索"
            allow-clear
            v-model:value="keyword"
            @search="onSearch" />
          <a-button type="primary" style="margin-left: 10px" @click="insert">
            新增授权
          </a-button>
        </template>
      </a-tabs>

      <a-table
        size="small"
        class="authorize-list"
        rowKey="id"
        :loading="loading"
        :columns="columns"
        :data-source="list"
        :pagination="pager"
        :row-class-name="record => setRowClassName(record)"
        @change="onTableChange">
        <template #bodyCell="{ text, record, column }">
          <a-select
            v-if="column.dataIndex === 'permission'"
            size="small"
            style="width: 100px"
            :value="text"
            :loading="record.loading"
            :disabled="record.loading"
            @change="e => onRoleChange(record, e)">
            <a-select-option value="READ">查看者</a-select-option>
            <a-select-option value="WRITE">协作者</a-select-option>
          </a-select>

          <a-space v-if="column.dataIndex === 'action'">
            <a
              v-if="record.expired || record.isWillExpire"
              style="color: #5cdbd3"
              @click="edit({ ...record, _mode: 'RENEW' })">
              续期
            </a>
            <a v-else @click="edit(record)">编辑</a>

            <a-popconfirm title="确认删除该授权？" @confirm="del(record.id)">
              <a style="color: red">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </section>
  </a-drawer>

  <AuthorDrawer
    v-model:open="drawerOpen"
    :init-data="itemRecord"
    @success="onAuthorSuccess" />
</template>

<script setup>
import { ref, computed, watch, shallowReactive, shallowRef } from 'vue'
import { message } from 'ant-design-vue'
import useAppStore from '@/store/modules/app'
import AuthorDrawer from './AuthorDrawer.vue'
import {
  getAuthorizeUsers,
  getAuthorizeGroups,
  putAuthorizeRole,
  deleteAuthorize,
} from '@/apis/dataset/authorize'
import { tableColumns, isWillExpire, isExpired } from './config'

const appStore = useAppStore()
const workspaceId = computed(() => appStore.workspaceId)

const emits = defineEmits(['update:open'])
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

const activeKey = ref('USER')
const isUser = computed(() => activeKey.value === 'USER')
const onTabChange = () => {
  keyword.value = ''
  userPager.current = 1
  groupPager.current = 1

  if (isUser.value) {
    fetchAuthorizeUsers()
  } else {
    fetchAuthorizeGroups()
  }
}

const keyword = ref('')
const onSearch = () => {
  fetchData()
}
const columns = computed(() => {
  const item = reactive(tableColumns[0])

  if (activeKey.value === 'USER') {
    item.title = '用户'
    item.dataIndex = 'usernameAlias'
  } else {
    item.title = '用户组'
    item.dataIndex = 'roleName'
  }

  tableColumns[0] = item

  return tableColumns
})

const queryParams = computed(() => {
  const { current: pageNum, pageSize } = pager.value

  return {
    workspaceId: workspaceId.value,
    datasetId: props.initData.id,
    keyword: keyword.value,
    pageNum,
    pageSize,
  }
})

const userList = ref([])
const userPager = shallowReactive({
  current: 1,
  pageSize: 10,
  total: 0,
})
const fetchAuthorizeUsers = async () => {
  try {
    loading.value = true

    const { data = [], total = 0 } = await getAuthorizeUsers(queryParams.value)

    userPager.total = total
    userList.value = data.map(t => {
      const { startTime, expireDuration } = t
      const expired = isExpired(startTime, expireDuration)

      return {
        ...t,
        expired,
        isWillExpire: expired ? false : isWillExpire(startTime, expireDuration),
      }
    })
  } catch (error) {
    console.error('获取授权用户失败', error)
  } finally {
    loading.value = false
  }
}

const groupList = ref([])
const groupPager = shallowReactive({
  current: 1,
  pageSize: 10,
  total: 0,
})
const fetchAuthorizeGroups = async () => {
  try {
    loading.value = true

    const { data = [], total = 0 } = await getAuthorizeGroups(queryParams.value)

    groupPager.total = total
    groupList.value = data.map(t => {
      const { startTime, expireDuration } = t
      const expired = isExpired(startTime, expireDuration)

      return {
        ...t,
        expired,
        isWillExpire: expired ? false : isWillExpire(startTime, expireDuration),
      }
    })
  } catch (error) {
    console.error('获取授权用户组失败', error)
  } finally {
    loading.value = false
  }
}

const loading = ref(false)
// 渲染数据
const list = computed(() => {
  return isUser.value ? userList.value : groupList.value
})
// 渲染分页
const pager = computed(() => {
  return isUser.value ? userPager : groupPager
})
const setRowClassName = row => {
  return row.expired ? 'expired' : ''
}
const fetchData = async () => {
  try {
    const fn = isUser.value ? fetchAuthorizeUsers : fetchAuthorizeGroups

    await fn()
  } catch (error) {
    console.error('获取授权列表错误', error)
  }
}

const onTableChange = ({ current, pageSize }) => {
  if (isUser.value) {
    userPager.current = current
    userPager.pageSize = pageSize

    fetchAuthorizeUsers()
  } else {
    groupPager.current = current
    groupPager.pageSize = pageSize

    fetchAuthorizeGroups()
  }
}

const reset = () => {
  keyword.value = ''
  userPager.current = 1
  groupPager.current = 1
}

// 初始化
watch(
  () => props.open,
  visible => {
    if (visible) {
      activeKey.value = 'USER'

      fetchData()
    } else {
      reset()
    }
  },
  { immediate: true }
)

// 改变用户权限类型
const onRoleChange = async (row, permission) => {
  if (row.loading) return

  try {
    row.loading = true

    const { permission: n } = await putAuthorizeRole(row.id, { permission })

    row.permission = n
    message.success('修改成功')
  } catch (error) {
    console.error('修改角色错误', error)
  } finally {
    row.loading = false
  }
}

// 编辑
const edit = row => {
  itemRecord.value = { ...row, _type: activeKey.value }
  drawerOpen.value = true
}

// 删除授权
const del = async rowId => {
  try {
    await deleteAuthorize(rowId)

    message.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('删除授权错误', error)
  }
}

const itemRecord = shallowRef({})
const drawerOpen = ref(false)
const insert = () => {
  itemRecord.value = { datasetId: props.initData.id, _type: activeKey.value }
  drawerOpen.value = true
}
const onAuthorSuccess = () => {
  setTimeout(fetchData, 10)
}

const close = () => {
  emits('update:open', false)
}
</script>

<style lang="scss" scoped>
.authorize-list {
  :deep(.ant-table-row.expired) {
    .ant-badge-status-dot:before {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      border-radius: 50%;
      background-color: rgba(255, 255, 255, 0.65);
    }
    &,
    .ant-badge-status-text {
      color: #d9d9d9;
    }
  }
}
</style>
