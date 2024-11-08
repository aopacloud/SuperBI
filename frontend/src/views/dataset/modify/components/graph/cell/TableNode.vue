<template>
  <div class="t-node">
    <div class="t-node-name" :title="displayName">{{ displayName }}</div>
    <div class="t-node-action" @mousedown.stop style="pointer-events: fill">
      <a-dropdown trigger="click">
        <a-button
          style="height: 100%"
          size="small"
          type="text"
          :icon="h(MoreOutlined)"
        />
        <template #overlay>
          <a-menu>
            <a-menu-item @click="handleCopy">复制</a-menu-item>
            <a-popconfirm
              title="确定删除该表？"
              okText="确认"
              cancelText="取消"
              @confirm="remove"
            >
              <a-menu-item style="color: red">移除</a-menu-item>
            </a-popconfirm>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
    <div class="t-alias">{{ tableAlias }}</div>
  </div>
</template>

<script setup>
import { h, inject, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { MoreOutlined } from '@ant-design/icons-vue'
import emittor from '@/common/plugins/emittor'
import { copy } from 'common/utils/help'

const getNode = inject('getNode')

const node = ref(null)
const data = ref()

const displayName = computed(() => {
  if (!data.value) return ' - '

  const { datasourceName, tableName } = data.value
  return datasourceName + '.' + tableName
})
const tableAlias = computed(() => data.value?.alias)

const remove = () => {
  const cNodes = (node.value.children || []).filter(c => c.isNode())
  if (cNodes.length > 0) return message.warn('请先删除子节点')

  emittor.emit('on-table-node-remove', node.value)
}

const handleCopy = () => {
  copy(displayName.value, () => {
    message.success('复制成功')
  })
}

onMounted(() => {
  node.value = getNode()
  data.value = node.value.data
  node.value.on('change:data', e => {
    data.value = e.data
  })
})
</script>

<style lang="scss" scoped>
.t-node {
  position: relative;
  display: flex;
  align-items: center;
  height: 100%;
  border: 1px solid #1677ff;
  padding-left: 10px;
  box-shadow: inset 2px 0 0 0 #4096ff;
  border-radius: 4px;
  &-name {
    flex: 1;
    @extend .ellipsis;
  }
  &-action {
    align-self: stretch;
    display: flex;
    align-items: center;
  }
}
.t-alias {
  position: absolute;
  bottom: -22px;
  color: #aaa;
  font-size: 16px;
}
</style>
