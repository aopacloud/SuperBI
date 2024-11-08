<template>
  <a-popover
    trigger="click"
    arrowPointAtCenter
    placement="bottomLeft"
    v-model:open="open"
  >
    <slot>
      <a-button @dragover.stop size="small" :icon="h(PlusOutlined)" />
    </slot>

    <template #content>
      <div class="flex-column">
        <SelectList
          style="width: 240px; height: 400px"
          ref="selectListRef"
          key-field="name"
          label-field="displayName"
          :data-source="dataSource"
          v-model:value="modelValue"
        >
        </SelectList>
        <div style="display: flex; margin-top: 10px">
          <span style="color: #1677ff" @click="addAssemblyFilter">
            <PlusOutlined />
            <a> 组合过滤 </a>
          </span>
          <a-space style="margin-left: auto">
            <a-button size="small" @click="handleCancel">取消</a-button>
            <a-button size="small" type="primary" @click="handleOk">
              确认
            </a-button>
          </a-space>
        </div>
      </div>
    </template>
  </a-popover>
</template>

<script setup>
import { h, nextTick, ref, watch } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import SelectList from 'common/components/ExtendSelect'

const props = defineProps({
  dataSource: {
    type: Array,
    default: () => []
  },
  value: {
    type: Array,
    default: () => []
  }
})

const open = ref(false)
const modelValue = ref([])
const selectListRef = ref(null)
watch(open, op => {
  if (op) {
    modelValue.value = props.value

    selectListRef.value?.clearSearch?.()
  }
})

const handleCancel = () => {
  open.value = false
}
const emits = defineEmits(['ok', 'addAssembly'])
const handleOk = () => {
  handleCancel()

  emits('ok', modelValue.value)
}

const addAssemblyFilter = () => {
  handleCancel()

  setTimeout(() => {
    emits('addAssembly')
  }, 10)
}
</script>
