<template>
  <a-drawer title="移动至" :maskClosable="false" :open="open" @close="close">
    <section class="flex-column">
      <header class="flex">
        <slot name="title">
          <template v-if="isMultiple">
            您正在移动
            <h3 style="display: inline; margin: 0 6px">{{ ids.length }}个</h3>
            资源
          </template>
          <template v-else>
            <span style="width: 45px">名称：</span>
            <h3 style="flex: 1; margin: 0">{{ initData.name }}</h3>
          </template>
        </slot>
      </header>

      <main class="content flex-1" style="margin-top: 24px">
        <a-spin :spinning="loading" style="height: 100%">
          <a-tree
            blockNode
            v-model:expandedKeys.sync="expandedKeys"
            v-model:selectedKeys.sync="modelValue"
            :field-names="{
              key: 'id',
              title: 'name'
            }"
            :tree-data="treeData"
          ></a-tree>
        </a-spin>
      </main>
    </section>

    <template #footer>
      <a-space style="float: right">
        <a-button @click="close">取消</a-button>
        <a-button
          type="primary"
          :disabled="disabled"
          @loading="confirmLoading"
          @click="ok"
          >保存
        </a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { getDirectoryWithoutCount, moveDirectory } from '@/apis/directory'
import { flat, deepFind } from 'common/utils/help'

const emits = defineEmits(['update:open', 'ok'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  initData: {
    type: Object,
    default: () => ({})
  },
  initParams: {
    type: Object,
    default: () => ({
      position: 'DASHBOARD',
      type: 'ALL',
      workspaceId: 7
    })
  },
  ids: {
    type: Array,
    default: () => []
  },
  customRequest: {
    type: Function
  }
})

const isMultiple = computed(() => props.ids.length)

const isPersonal = computed(() => props.initParams.type === 'PERSONAL')

const reset = () => {
  confirmLoading.value = false
  modelValue.value = []
}
const init = () => {
  modelValue.value = [props.initData.folderId].filter(
    t => typeof t !== 'undefined' && t !== null
  )

  fetchData()
}

watch(
  () => props.open,
  open => {
    if (!open) {
      reset()
    } else {
      init()
    }
  }
)

const modelValue = ref([])
const disabled = computed(() => {
  return (
    !modelValue.value.length || modelValue.value[0] === props.initData.folderId
  )
})

const loading = ref(false)
const treeData = ref([])
const expandedKeys = ref([])

const transformDisabled = (list = [], enabledKey = []) => {
  return list.map(item => {
    return {
      ...item,
      disabled: !enabledKey.includes(item.id),
      children: transformDisabled(item.children, enabledKey)
    }
  })
}

const fetchData = async () => {
  try {
    loading.value = true

    const res = await getDirectoryWithoutCount(props.initParams)

    if (!isPersonal.value) {
      treeData.value = [res]
    } else {
      if (!props.initData.folderId) {
        treeData.value = [res]
      } else {
        let parent = { parentId: props.initData.folderId }

        // 只能在当前顶层父级下进行移动
        do {
          parent = deepFind([res], item => item.id === parent.parentId)
        } while (parent.id > 0)

        const childrenKeys = flat(parent.children).map(child => child.id)

        treeData.value = transformDisabled([res], childrenKeys)
      }
    }
    expandedKeys.value = flat(treeData.value, 'children').map(t => t.id)
  } catch (error) {
    console.error('获取文件夹错误', error)
  } finally {
    loading.value = false
  }
}

const close = () => {
  emits('update:open', false)
}
const confirmLoading = ref(false)
const ok = async () => {
  try {
    confirmLoading.value = true

    const payload = {
      ...props.initParams,
      targetId: props.initData.id,
      folderId: modelValue.value[0]
    }

    const fn =
      typeof props.customRequest === 'function'
        ? props.customRequest
        : moveDirectory

    const res = await fn(payload)

    message.success('移动成功')
    emits('ok', res)
    close()
  } catch (error) {
    console.error('移动失败', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.content {
  :deep(.ant-spin-nested-loading) {
    height: 100%;
  }
}
</style>
