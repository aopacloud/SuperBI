<template>
  <a-modal
    title="数据集编辑"
    :maskClosable="false"
    :open="open"
    @cancel="onCancel"
    @ok="onOk">
    <a-form :labelCol="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="数据集名称" v-bind="validateInfos.name">
        <a-input placeholder="请输入备注" v-model:value="formState.name" />
      </a-form-item>
      <a-form-item label="备注">
        <a-input placeholder="请输入备注" v-model:value="formState.description" />
      </a-form-item>
      <a-form-item label="文档链接" v-bind="validateInfos.docUrl">
        <a-input
          placeholder="请输入文档链接"
          v-model:value="formState.docUrl"
          @blur="validate('docUrl', { trigger: 'blur' }).catch(() => {})" />
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
  initData: {
    type: Object,
    default: () => ({}),
  },
  initParams: {
    type: Object,
    default: () => ({}),
  },
})

const loading = ref(false)
const treeData = ref([])
const treeExpandedKeys = ref([])
// 获取文件夹
const fetchData = async () => {
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

const formState = reactive({
  name: undefined,
  description: undefined,
  docUrl: undefined,
  folderId: -1,
})
const formRuls = reactive({
  name: [{ required: true, message: '名称不能为空' }],
  // folderId: [{ required: true, message: '位置不能为空' }],
  docUrl: [
    {
      pattern: /(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w\.-]*)*\/?/g,
      message: '链接格式不正确',
      trigger: 'blur',
    },
  ],
})
const { validateInfos, resetFields, validate, clearValidate } = Form.useForm(
  formState,
  formRuls
)

const init = () => {
  fetchData()

  for (const key in toRaw(formState)) {
    const value = props.initData[key]

    if (key === 'folderId') {
      formState[key] = value ?? -1
    } else {
      formState[key] = value
    }
  }
}

watch(
  () => props.open,
  open => {
    if (open) {
      init()
    } else {
      resetFields()
    }
  },
  { immediate: true }
)

const onCancel = () => {
  if (!props.initData.name) {
    window.close()
  } else {
    emits('update:open', false)
    emits('cancel')
  }
}

const onOk = () => {
  validate().then(() => {
    emits('update:open', false)
    emits('ok', toRaw(formState))
  })
}
</script>
