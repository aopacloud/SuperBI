<template>
  <div class="flex-column dir-tree">
    <div class="flex justify-between align-center">
      <a-radio-group
        class="dir-type"
        button-style="solid"
        :value="type"
        @change="onTypeChange">
        <a-radio-button title="公共" value="ALL">
          <span style="font-size: 14px; font-weight: 600">公共</span>
        </a-radio-button>
        <a-radio-button title="我的" value="PERSONAL">
          <span style="font-size: 14px; font-weight: 600">我的</span>
        </a-radio-button>
      </a-radio-group>

      <FolderAddOutlined
        class="pointer"
        style="font-size: 30px; color: #1677ff"
        v-permission="addPermissions"
        @click="add" />
    </div>

    <a-spin :spinning="loading">
      <a-directory-tree
        :expandAction="false"
        :tree-data="treeData"
        :field-names="{
          ttile: 'name',
          key: 'id',
        }"
        v-model:expandedKeys="expandedKeys"
        v-model:selectedKeys="modelValue"
        @select="onSelect">
        <template #title="{ id: treenodeId, name, parentId, resourceNum }">
          <a-dropdown :trigger="['contextmenu']">
            <div class="tree-title">
              <span class="tree-title--name">{{ name }}</span>
              <span class="tree-title--count" v-if="resourceNum > -1">
                {{ resourceNum }}
              </span>
            </div>
            <template #overlay v-if="addPermissions && treenodeId > 0">
              <a-menu
                @click="
                  ({ key: menuKey }) =>
                    onContextMenuClick(menuKey, { id: treenodeId, name, parentId, num })
                ">
                <a-menu-item key="edit">编辑</a-menu-item>
                <a-menu-item key="del">删除</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
      </a-directory-tree>
    </a-spin>

    <ModifyModal
      :init-params="{ type, position, workspaceId }"
      :init-data="nodeInfo"
      v-model:open="modalOpen"
      @success="fetchData" />
  </div>
</template>
<script setup>
import { ref, computed, watch } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { FolderAddOutlined } from '@ant-design/icons-vue'
import { useRoute } from 'vue-router'
import useAppStore from '@/store/modules/app'
import ModifyModal from './components/ModifyModal.vue'
import { deepFind, flat } from 'common/utils/help'
import { getDirectory, delDirectory } from '@/apis/directory'
import { clearQuerys } from 'common/utils/window'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const props = defineProps({
  type: {
    type: String,
    default: 'ALL',
    validator: val => ['ALL', 'PERSONAL'].includes(val),
  },
  position: {
    type: String,
    default: 'DASHBOARD',
  },
})

const addPermissions = computed(() => {
  const { type, position } = props

  const str = [
    position,
    'VIEW',
    type === 'PERSONAL' ? 'PRIVATE' : 'PUBLIC',
    'FOLDER',
  ].join(':')

  return userStore.hasPermission(str)
})

const workspaceId = computed(() => appStore.workspaceId)

const loading = ref(false)
const treeData = ref([])
const expandedKeys = ref([])
const fetchData = async () => {
  try {
    loading.value = true

    const payload = {
      type: props.type,
      position: props.position,
      workspaceId: workspaceId.value,
    }

    const res = await getDirectory(payload)

    treeData.value = res.children
    expandedKeys.value = flat(treeData.value, 'children').map(t => t.id)

    if (route.query.folderId) {
      redirect(route.query.folderId)

      clearQuerys(['type', 'folderId'])
    }
  } catch (error) {
  } finally {
    loading.value = false
  }
}

const nodeInfo = ref({})
const modalOpen = ref(false) // modal 显隐状态
// 添加
const add = () => {
  nodeInfo.value = {}
  modalOpen.value = true
}
// 右键菜单
const onContextMenuClick = (menuKey, treeInfo) => {
  if (menuKey === 'edit') {
    edit(treeInfo)
  } else {
    del(treeInfo)
  }
}
// 编辑
const edit = row => {
  nodeInfo.value = { ...row }
  modalOpen.value = true
}
// 删除
const del = ({ id, name, num }) => {
  if (num) {
    Modal.warning({
      title: '提示',
      content: '请先移除该文件夹下的内容后再进行文件夹删除',
    })
  } else {
    try {
      Modal.confirm({
        title: '删除',
        content: `确定删除【${name}】`,
        okText: '确定',
        cancelText: '取消',
        onOk: async () => {
          await delDirectory(id)

          message.success('删除成功')
          fetchData()
        },
      })
    } catch (error) {
      console.error('删除文件夹错误', error)
    }
  }
}

const emits = defineEmits(['update:type', 'select', 'reload'])
const onSelect = (ids = []) => {
  const item = deepFind(treeData.value, item => item.id === ids[0])

  emits('select', item)
}
const modelValue = ref([])
const onTypeChange = e => {
  const value = e.target.value

  emits('update:type', value)
  onSelect()
}
// 设置
const set = (ids = []) => {
  modelValue.value = ids

  onSelect(ids)
}

// 定位到指定文件夹id并展开其父级节点
const redirect = id => {
  const treeList = flat(treeData.value)
  const item = treeList.find(t => t.id === +id)

  if (!item) return

  set([item.id])

  // 展开父级
  let parent = item,
    parentId = []
  while (parent) {
    parentId.push(parent.id)

    parent = treeList.find(t => t.id === parent.parentId)
  }

  expandedKeys.value = parentId
}

watch(
  [() => props.type, workspaceId],
  () => {
    modelValue.value = []

    fetchData()
  },
  {
    immediate: true,
  }
)

defineExpose({
  reload: fetchData,
  add,
  set,
})
</script>

<style lang="scss" scoped>
.dir-type {
  :deep(.ant-segmented-item) {
    background-color: #f2f2f2;
    &.ant-segmented-item-selected {
      background-color: #1677ff;
      color: #fff;
    }
  }
}
.dir-tree {
  height: 100%;
  white-space: nowrap;
  :deep(.ant-spin-nested-loading) {
    flex: 1;
    margin-top: 10px;
    overflow: hidden;
    .ant-spin-container {
      height: 100%;
    }
  }
  :deep(.ant-tree) {
    $treenodeHeight: 28px;

    height: 100%;
    overflow: auto;

    .ant-tree-switcher {
      line-height: $treenodeHeight;
    }
    .ant-tree-treenode {
      .ant-tree-node-content-wrapper {
        display: flex;
        width: 100%;
        min-height: $treenodeHeight;
        padding-left: 0;
        overflow: hidden;
        & > * {
          line-height: $treenodeHeight;
        }
      }
      .ant-tree-title {
        flex: 1;
        overflow: hidden;
      }
    }
  }
  .tree-title {
    display: flex;
    width: 100%;
    justify-content: space-between;
    align-items: center;
    &--name {
      flex: 1;
      @extend .ellipsis;
    }
    &--count {
      display: inline-block;
      min-width: 22px;
      height: 22px;
      line-height: 1;
      padding: 3px;
      background-color: #f2f2f2;
      border-radius: 10px;
      text-align: center;
      color: initial;
    }
  }
}
</style>
