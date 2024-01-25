<template>
  <a-modal title="看板编辑" :maskClosable="false" :open="open" @cancel="cancel" @ok="ok">
    <a-form :labelCol="{ span: 4 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="名称" v-bind="validateInfos.name">
        <a-input placeholder="请输入名称" v-model:value="formState.name" />
      </a-form-item>
      <a-form-item label="备注">
        <a-input placeholder="请输入备注" />
      </a-form-item>
      <a-form-item label="位置" v-bind="validateInfos.folderId">
        <a-tree-select
          style="width: 100%"
          placeholder="请选择"
          :loading="loading"
          :field-names="{
            value: 'id',
            label: 'name',
          }"
          :tree-data="treeData"
          v-model:treeExpandedKeys="treeExpandedKeys"
          v-model:value="formState.folderId"></a-tree-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, toRaw, watch } from 'vue'
import { Form } from 'ant-design-vue'
import { getDirectory } from '@/apis/directory'
import { flat } from 'common/utils/help'

const emits = defineEmits(['update:open', 'ok', 'cancel'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  detail: {
    type: Object,
    default: () => ({}),
  },
  initParams: {
    type: Object,
  },
})

watch(
  () => props.open,
  open => {
    if (open) {
      init()
      fetchTreeData()
    } else {
      resetFields()
    }
  }
)

const loading = ref(false)
const treeData = ref([])
const treeExpandedKeys = ref([])
// 获取文件夹
const fetchTreeData = async () => {
  try {
    loading.value = true

    const res = await getDirectory(props.initParams)

    treeData.value = [res]
    treeExpandedKeys.value = flat([res]).map(t => t.id)
  } catch (error) {
    console.error('获取文件夹错误', error)
  } finally {
    loading.value = false
  }
}

// 初始化
const init = () => {
  for (const key in toRaw(formState)) {
    const val = props.detail[key]

    if (key === 'folderId') {
      formState[key] = val ?? -1
    } else {
      formState[key] = val
    }
  }
}

const formState = reactive({
  name: undefined,
  remark: undefined,
  folderId: undefined,
})
const formRuls = reactive({
  name: [{ required: true, message: '名称不能为空' }],
  folderId: [{ required: true, message: '位置不能为空' }],
})
const { validateInfos, resetFields, validate } = Form.useForm(formState, formRuls)

const cancel = () => {
  emits('update:open', false)
  emits('cancel')
}
const ok = () => {
  validate().then(res => {
    emits('ok', res)
    cancel()
  })
}
</script>
