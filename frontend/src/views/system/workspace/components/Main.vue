<template>
  <section class="section">
    <header>
      <a-descriptions :column="2">
        <template #title>
          <span>空间信息</span>

          <a-space v-if="userStore.isSuperAdmin" style="margin-left: 20px">
            <a @click="emits('edit')">编辑</a>
            <a style="color: red" @click="emits('del')">删除</a>
          </a-space>
        </template>
        <a-descriptions-item
          v-for="(value, key) in descriptionMap"
          :key="key"
          :label="value">
          {{ detail[key] }}
        </a-descriptions-item>
      </a-descriptions>
    </header>

    <main class="flex-1 scroll">
      <a-tabs class="main-tabs" v-model:activeKey="activeKey" @change="onActiveKeyChange">
        <a-tab-pane tab="成员" key="member">
          <MemberPane
            ref="memberPaneRef"
            :workspaceId="info.id"
            :permissions="permissions"
            :keyword="keyword" />
        </a-tab-pane>
        <a-tab-pane tab="成员组" key="role">
          <MemberGroupPane
            ref="memberGroupPaneRef"
            :workspaceId="info.id"
            :permissions="permissions"
            :keyword="keyword" />
        </a-tab-pane>

        <SystemWorkspaceSetting
          v-if="versionJs.ViewsSystemWorkspace.hasSettingTabPane"
          tab="分析设置"
          key="setting"
          ref="settingPaneRef"
          :config="detail.config" />

        <template #rightExtra>
          <a-space>
            <a-input-search
              v-if="activeKey !== 'setting'"
              placeholder="请输入检索"
              allow-clear
              v-model:value="keyword"
              @search="onKeywordSearch" />

            <a-button
              v-if="activeKey === 'member' && hasCurrentUserAdd"
              type="primary"
              @click="insertMember">
              新增成员
            </a-button>

            <a-button
              v-if="activeKey === 'role' && hasCurrentGruopAdd"
              type="primary"
              @click="insertMemberGroup">
              新增成员组
            </a-button>

            <a-button
              v-if="activeKey === 'setting'"
              type="primary"
              :loading="settingConfirmLoading"
              :icon="h(SaveOutlined)"
              @click="saveSetting">
              保存设置
            </a-button>
          </a-space>
        </template>
      </a-tabs>
    </main>
  </section>
</template>

<script setup>
import { h, ref, reactive, watch, shallowRef, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { SaveOutlined } from '@ant-design/icons-vue'
import useUserStore from '@/store/modules/user'
import MemberPane from './MemberPane.vue'
import MemberGroupPane from './MemberGroupPane.vue'
import {
  getOneWorkspace,
  updateWorkspace,
  getPermissionByWorkspaceId,
} from '@/apis/workspace'
import { versionVue, versionJs } from '@/versions'

const { SystemWorkspaceSetting } = versionVue

const userStore = useUserStore()

const emits = defineEmits(['edit', 'delete'])

const props = defineProps({
  info: {
    type: Object,
    default: () => [],
  },
})

// 渲染描述信息的映射
const descriptionMap = reactive({
  name: '空间',
  memberCount: '成员数量',
  roleCount: '成员组数量',
  createTime: '创建时间',
  updateTime: '更新时间',
  description: '空间描述',
})

const loading = ref(false)
const detail = ref({})

const fetchDetail = async id => {
  try {
    loading.value = true

    const res = await getOneWorkspace(id)
    detail.value = res
  } catch (error) {
    console.error('获取空间详情失败', error)
  } finally {
    loading.value = false
  }
}

// 当前空间权限code
const permissions = shallowRef([])

// 成员管理权限
const hasCurrentUserAdd = computed(() => {
  return permissions.value.includes('WORKSPACE:VIEW:MANAGE:USER')
})
// 成员组管理权限
const hasCurrentGruopAdd = computed(() => {
  return permissions.value.includes('WORKSPACE:VIEW:MANAGE:ROLE')
})

// 获取当前空间的资源权限
const fetchDetailPermission = async id => {
  const { resourceCodes = [] } = await getPermissionByWorkspaceId(id)
  permissions.value = resourceCodes
}

watch(
  () => props.info.id,
  id => {
    if (!id) return

    keyword.value = ''

    fetchDetailPermission(id)
    fetchDetail(id)
  }
)

defineExpose({
  update: e => {
    detail.value = { ...detail.value, ...e }
  },
})

// tabs
const activeKey = ref('member')
const onActiveKeyChange = e => {
  keyword.value = ''
}

// 成员Pane
const memberPaneRef = ref()
// 成员组Pane
const memberGroupPaneRef = ref()
// 设置Pane
const settingPaneRef = ref()

// 关键字
const keyword = ref('')
const onKeywordSearch = () => {
  if (activeKey.value === 'setting') return

  nextTick(() => {
    memberPaneRef.value?.init()
    memberGroupPaneRef.value?.init()
  })
}

// 添加成员
const insertMember = () => {
  memberPaneRef.value?.insert()
}
// 添加成员组
const insertMemberGroup = () => {
  memberGroupPaneRef.value?.insert()
}
// 保存设置
const settingConfirmLoading = ref(false)
const saveSetting = async () => {
  try {
    settingConfirmLoading.value = true
    const data = settingPaneRef.value.getData?.()

    if (!data) return

    const payload = {
      ...detail.value,
      config: {
        ...detail.value.config,
        selectedTimezone: data.value,
        activeTimezone: data.activeValue,
      },
    }

    await updateWorkspace(props.info.id, payload)

    message.success('保存成功')
  } catch (error) {
    console.error('保存空间设置失败', error)
  } finally {
    settingConfirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  background-color: #f5f5f5;
}
.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  @extend .ellipsis;
  .title {
    color: rgba(0, 0, 0, 0.88);
    margin-right: 12px;
    font-weight: 600;
    font-size: 16px;
    line-height: 1.5;
  }
  .header-a {
    margin-left: 8px;
  }
}
.main-tabs {
  height: 100%;
  :deep(.ant-tabs-content-holder) {
    overflow: auto;
    & > .ant-tabs-content {
      overflow: hidden;
    }
  }
}
</style>
