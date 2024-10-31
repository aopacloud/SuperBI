<template>
  <div class="flex-column dir-tree">
    <div class="flex justify-between align-center" style="padding-right: 12px">
      <a-radio-group
        class="dir-type"
        button-style="solid"
        :value="type"
        @change="onTypeChange"
      >
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
        @click="add"
      />
    </div>

    <a-spin :spinning="loading">
      <a-directory-tree
        blockNode
        :showIcon="false"
        :draggable="addPermissions"
        :expandAction="false"
        :tree-data="treeData"
        :field-names="{
          ttile: 'name',
          key: 'id'
        }"
        v-model:expandedKeys="expandedKeys"
        v-model:selectedKeys="modelValue"
        :allowDrop="onAllowDrop"
        @select="onSelect"
        @drop="onDrop"
      >
        <template
          #title="{ id: treenodeId, name, parentId, resourceNum, _level }"
        >
          <a-dropdown :trigger="['contextmenu']">
            <div class="tree-title">
              <span
                :class="['tree-title--name', `tree-node-level-${_level}`]"
                >{{ name }}</span
              >
              <span class="tree-title--count" v-if="resourceNum > -1">
                {{ resourceNum }}
              </span>
            </div>
            <template #overlay v-if="addPermissions && treenodeId > 0">
              <a-menu
                @click="
                  ({ key: menuKey }) =>
                    onContextMenuClick(menuKey, {
                      id: treenodeId,
                      name,
                      parentId,
                      num
                    })
                "
              >
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
      @success="fetchData"
    />
  </div>
</template>
<script setup>
import { ref, computed, watch } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { FolderAddOutlined } from '@ant-design/icons-vue'
import { useRoute } from 'vue-router'
import useAppStore from '@/store/modules/app'
import ModifyModal from './components/ModifyModal.vue'
import { deepClone, deepFind, flat } from 'common/utils/help'
import {
  getDirectory,
  delDirectory,
  updateDirectoryList
} from '@/apis/directory'
import { clearQuerys } from 'common/utils/window'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const props = defineProps({
  type: {
    type: String,
    default: 'ALL',
    validator: val => ['ALL', 'PERSONAL'].includes(val)
  },
  position: {
    type: String,
    default: 'DASHBOARD'
  }
})

const addPermissions = computed(() => {
  const { type, position } = props

  const str = [
    position,
    'VIEW',
    type === 'PERSONAL' ? 'PRIVATE' : 'PUBLIC',
    'FOLDER'
  ].join(':')

  return userStore.hasPermission(str)
})

const workspaceId = computed(() => appStore.workspaceId)

const _setLevel = (list = [], level = 0) => {
  return list.map(item => {
    return {
      ...item,
      _level: level,
      children: _setLevel(item.children, level + 1)
    }
  })
}

const loading = ref(false)
const treeData = ref([])
const expandedKeys = ref([])
const fetchData = async cb => {
  try {
    loading.value = true

    const payload = {
      type: props.type,
      position: props.position,
      workspaceId: workspaceId.value
    }

    const res = await getDirectory(payload)

    if (cb) {
      cb(_setLevel(res.children))
      return
    }

    treeData.value = _setLevel(res.children)
    expandedKeys.value = flat(treeData.value, 'children').map(t => t.id)

    if (route.query.folderId) {
      redirect(route.query.folderId)

      clearQuerys(['type', 'folderId'])
    }
  } catch (error) {
    console.error('获取文件夹列表失败', error)
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
      content: '请先移除该文件夹下的内容后再进行文件夹删除'
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
        }
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
    immediate: true
  }
)

const findRootNode = item => {
  if (item.parentId === -1) return item
  const parent = flat(treeData.value).find(t => t.id === +item.parentId)
  return parent ? findRootNode(parent) : item
}

const onAllowDrop = e => {
  const {
    dragNode: { dataRef: startNode },
    dropNode,
    dropPosition
  } = e

  // 预置文件夹不能拖动、放置
  if (startNode.id < 0) return
  if (dropNode.id < 0) return

  // 拖动的是创建的目录
  if (dropNode.id > 0) {
    // 放置在目录下一位
    // if (dropPosition === 0) return
    // 放置在文件下
    // if (dropNode.parentId > 0) {
    //   // 放置节点是否是顶级节点
    //   const endRoot = findRootNode(dropNode)
    //   if (endRoot.parentId < 0) return
    // }
    const startRoot = findRootNode(startNode)
    const endRoot = findRootNode(dropNode)
    // 拖动节点的顶级节点和放置节点的顶级节点 是 预置的
    if (startRoot.id < 0 && endRoot.id < 0) {
      // 不可在不同的预置的顶级目录中拖动
      if (startRoot.id !== endRoot.id) return
    }
  }

  return true
}

const onDrop = ({ dragNode, node, dropPosition: position, dropToGap }) => {
  const { key: dropKey, pos: dropPos } = node
  const { key: dragKey } = dragNode
  const dropPosition = position - Number(dropPos[dropPos.length - 1])

  const loop = (data, key, callback) => {
    data.forEach((item, index) => {
      if (item.id === key) {
        return callback(item, index, data)
      }
      if (item.children) {
        return loop(item.children, key, callback)
      }
    })
  }
  const data = deepClone(treeData.value)

  // Find dragObject
  let dragObj
  loop(data, dragKey, (item, index, arr) => {
    arr.splice(index, 1)
    dragObj = item
  })

  if (!dropToGap) {
    // Drop on the content
    loop(data, dropKey, item => {
      item.children = item.children || []
      // where to insert 示例添加到尾部，可以是随意位置
      dropPosition === 1
        ? item.children.push(dragObj)
        : item.children.unshift(dragObj)
    })
  } else if (
    (node.children || []).length > 0 && // Has children
    node.expanded && // Is expanded
    dropPosition === 1 // On the bottom gap
  ) {
    loop(data, dropKey, item => {
      item.children = item.children || []
      // where to insert 示例添加到尾部，可以是随意位置
      item.children.unshift(dragObj)
    })
  } else {
    let ar
    let i
    loop(data, dropKey, (item, index, arr) => {
      ar = arr
      i = index
    })
    if (dropPosition === -1) {
      ar.splice(i, 0, dragObj)
    } else {
      ar.splice(i + 1, 0, dragObj)
    }
  }

  // treeData.value = data
  updateTreeList(_toUpdateDate(data))
}

const _toUpdateDate = (tree, pId = -1) => {
  return flat(
    tree.map((item, index) => {
      const { id, name, parentId } = item

      return {
        id,
        parentId: pId,
        name,
        sortId: typeof parentId !== 'undefined' ? index : undefined,
        children: _toUpdateDate(item.children, id)
      }
    })
  )
}

const updateTreeList = async payload => {
  try {
    loading.value = true
    await updateDirectoryList(payload)
    await fetchData(r => {
      treeData.value = r
    })
  } catch (e) {
    console.error('更新文件夹失败', e)
  } finally {
    loading.value = false
  }
}

defineExpose({
  reload: fetchData,
  add,
  set
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
    height: 100%;
    padding-right: 12px;
    overflow: auto;

    &.ant-tree-directory .ant-tree-treenode:before {
      top: 2px;
      bottom: 2px;
    }

    .ant-tree-indent-unit {
      width: 16px;
    }
    .ant-tree-switcher {
      line-height: 32px;
    }

    .ant-tree-drop-indicator {
      bottom: 0 !important;
      transform: translate(3px, -3px);
    }

    .ant-tree-treenode {
      overflow: hidden;
      padding: 0;
      &:not(.dragging) {
        .ant-tree-node-selected {
          .tree-title--name {
            color: #fff !important;
          }
        }
        .tree-title--name {
          color: #7f7f7f;
          @extend .ellipsis;
          &.tree-node-level-0 {
            color: #000;
            font-weight: 600;
          }
          &.tree-node-level-1 {
            color: #333;
          }
        }
      }

      .ant-tree-node-content-wrapper {
        display: flex;
        width: 100%;
        overflow: hidden;
        line-height: 32px;
        padding: 0;
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

    &--count {
      display: inline-block;
      min-width: 22px;
      height: 22px;
      line-height: 1;
      padding: 3px;
      margin-right: 3px;
      background-color: #f2f2f2;
      border-radius: 10px;
      text-align: center;
      color: #333;
    }
  }
}
</style>
