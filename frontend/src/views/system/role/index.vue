<template>
  <section class="page flex">
    <aside class="left-area">
      <RoleList ref="roleListRef" @click="onRoleSelected" />
    </aside>
    <main class="right-area">
      <section class="flex-column">
        <header>
          <div class="title">
            <h3 style="margin: 0">角色信息</h3>

            <a-space v-if="!actionDisabled" style="margin-left: 24px">
              <a @click="edit">编辑</a>
              <a style="color: red" @click="del">删除</a>
            </a-space>
          </div>

          <div class="desc-p">
            <span>说明:</span>
            <div class="desc-content">{{ rowInfo.name }}</div>
          </div>
          <div class="desc-p">
            <span>描述:</span>
            <div class="desc-content">{{ rowInfo.description || '-' }}</div>
          </div>
        </header>

        <main class="resource-main">
          <a-spin :spinning="loading">
            <a-tabs class="resource-tabs" v-model:activeKey="activeKey">
              <a-tab-pane tab="菜单权限" key="menu">
                <MenuPermission
                  ref="menuPermissionRef"
                  :disabled="actionDisabled"
                  :data-source="menuResource"
                  :value="menuValue" />
              </a-tab-pane>

              <a-tab-pane tab="功能权限" key="function">
                <FuncPermission
                  :disabled="actionDisabled"
                  :data-source="funcResource"
                  v-model:value="otherValue" />
              </a-tab-pane>

              <a-tab-pane tab="分析权限" key="analyse">
                <ScopePermission
                  :disabled="actionDisabled"
                  :data-source="scopeResource"
                  v-model:value="otherValue" />
              </a-tab-pane>

              <template #rightExtra v-if="!actionDisabled">
                <keep-alive>
                  <a-button
                    type="primary"
                    style="margin-left: auto"
                    :icon="h(SaveOutlined)"
                    :loading="confirmLoading"
                    @click="save"
                    >{{ activeKey === 'menu' ? '保存菜单权限' : '保存功能、分析权限' }}
                  </a-button>
                </keep-alive>
              </template>
            </a-tabs>
          </a-spin>
        </main>
      </section>
    </main>
  </section>
</template>

<script setup>
import { h, ref, onMounted, computed } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { SaveOutlined } from '@ant-design/icons-vue'

import {
  getResource,
  getMenuResource,
  getRoleResourceByRoleId,
  getRoleMenuByRoleId,
  postRoleResource,
  postRoleMenu,
} from '@/apis/system/resource'
import RoleList from './components/RoleList.vue'
import MenuPermission from './components/MenuPermission.vue'
import FuncPermission from './components/FuncPermission.vue'
import ScopePermission from './components/ScopePermission.vue'

const roleListRef = ref()
const edit = () => {
  roleListRef.value.edit(rowInfo.value)
}
const del = () => {
  Modal.confirm({
    content: '确定删除该角色？',
    onOk: () => {
      return roleListRef.value.del(rowInfo.value.id)
    },
  })
}

const activeKey = ref('menu')

// 角色基本信息
const rowInfo = ref({})
const onRoleSelected = row => {
  rowInfo.value = { ...row }

  fetchDetailByRoleId(row.id)
}

const actionDisabled = computed(() => rowInfo.value.roleType === 'SYSTEM')

// 值
const menuValue = ref([])
const otherValue = ref([])

const confirmLoading = ref(false)
const save = async () => {
  try {
    confirmLoading.value = true

    if (activeKey.value === 'menu') {
      await saveMenu()
    } else {
      await saveOther()
    }
  } catch (error) {
    console.error('权限保存失败', error)
  } finally {
    confirmLoading.value = false
  }
}

const menuPermissionRef = ref(null)
// 菜单权限保存
const saveMenu = async () => {
  try {
    const menuValues = menuPermissionRef.value.getData()
    const params = menuValues.map(t => {
      return {
        roleId: rowInfo.value.id,
        menuId: t,
      }
    })

    await postRoleMenu(params)

    message.success('菜单权限保存成功')
  } catch (error) {
    console.error('角色权限保存失败', error)
  }
}
// 其他权限保存
const saveOther = async () => {
  try {
    const params = {
      roleId: rowInfo.value.id,
      resourceCode: otherValue.value,
    }

    await postRoleResource(params)
    message.success('功能、分析权限保存成功')
  } catch (error) {
    console.error('角色权限保存失败', error)
  }
}

const loading = ref(false)

// 获取该角色的权限
const fetchDetailByRoleId = async roleId => {
  try {
    loading.value = true

    await Promise.all([
      getRoleMenuByRoleId({ roleId }),
      getRoleResourceByRoleId({ roleId }),
    ])
      .then(([menuRes = [], res = []]) => {
        menuValue.value = menuRes.map(t => t.id)
        otherValue.value = res.resourceCode
      })
      .catch(error => {
        console.error('角色权限获取失败', error)
      })
  } catch (error) {
    console.error('角色权限获取失败', error)
  } finally {
    loading.value = false
  }
}

// 菜单资源
const menuResource = ref([])
// 功能资源
const funcResource = ref([])
// 分析资源
const scopeResource = ref([])

// 获取菜单资源
const fetchMenuResource = async () => {
  try {
    const res = await getMenuResource()

    menuResource.value = res
  } catch (error) {
    console.error('所有菜单资源获取失败', error)
  }
}

// 获取功能资源、分析资源
const fetchAllResource = async () => {
  try {
    const res = await getResource()

    funcResource.value = res.filter(t => t.resourceType === 'FUNCTION')
    scopeResource.value = res.filter(t => t.resourceType === 'SCOPE')
  } catch (error) {
    console.error('所有权限资源获取失败', error)
  }
}

onMounted(() => {
  fetchMenuResource()
  fetchAllResource()
})
</script>

<style lang="scss" scoped>
.left-area {
  width: 300px;
  background-color: #ffffff;
  padding: 16px;
}

.right-area {
  flex: 1;
  padding: 16px;
}
.title {
  display: flex;
  align-items: center;
  height: 24px;
  margin-bottom: 24px;
}
.desc-p {
  display: flex;
  margin: 0 0 22px;
}
.desc-content {
  flex: 1;
  margin-left: 8px;
}
.resource-main {
  flex: 1;
  overflow: hidden;
  .ant-spin-nested-loading {
    height: 100%;
    :deep(.ant-spin-container) {
      height: 100%;
    }
  }
}
.resource-tabs {
  display: flex;
  flex-direction: column;
  height: 100%;
  :deep(.ant-tabs-content-holder) {
    flex: 1;
    overflow: auto;
  }
}
</style>
