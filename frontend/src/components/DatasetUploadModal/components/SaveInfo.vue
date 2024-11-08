<template>
  <a-form
    style="width: 600px; margin: 0 auto"
    :labelCol="{ style: { width: '100px' } }"
  >
    <a-form-item label="数据集名称" v-bind="validateInfos.name">
      <a-input placeholder="请输入名称" v-model:value="formState.name" />
    </a-form-item>

    <a-form-item label="备注">
      <a-input placeholder="请输入备注" v-model:value="formState.description" />
    </a-form-item>

    <a-form-item label="空间">
      <a-select
        placeholder="请选择空间"
        :disabled="!isFromOutApp"
        :options="workspaceList"
        :field-names="{ label: 'name', value: 'id' }"
        v-model:value="formState.workspaceId"
        @change="onWorkspaceChange"
      ></a-select>
    </a-form-item>

    <a-form-item label="位置">
      <a-tree-select
        popupClassName="dir-select-tree"
        placeholder="请选择位置"
        tree-default-expand-all
        :tree-data="treeData"
        :field-names="{
          children: 'children',
          label: 'name',
          value: 'id'
        }"
        tree-node-filter-prop="name"
        v-model:value="formState.folderId"
      ></a-tree-select>
    </a-form-item>
  </a-form>
</template>

<script setup>
import { computed, reactive, toRaw, onActivated, onDeactivated } from 'vue'
import { Form } from 'ant-design-vue'
import useAppStore from '@/store/modules/app'
import { useRoute } from 'vue-router'
import { getDirectory } from '@/apis/directory'

const route = useRoute()

const appStore = useAppStore()

const { detail } = defineProps({
  detail: {
    type: Object,
    default: () => ({})
  },
  isFromOutApp: Boolean // 从外部应用跳转过来的，不可选择空间
})

const workspaceList = computed(() => appStore.workspaceList)

const formState = reactive({
  name: undefined,
  description: undefined,
  workspaceId: undefined,
  folderId: undefined
})

const formRules = reactive({
  name: [
    {
      required: true,
      message: '请输入名称'
    }
  ]
})

const initForm = () => {
  const {
    name,
    description,
    workspaceId = defaultWorkspaceId,
    folderId = -7
  } = detail

  formState.name = name
  formState.description = description
  formState.workspaceId = workspaceId
  formState.folderId = folderId
}

const {
  validateInfos,
  resetFields,
  validate: validateForm
} = Form.useForm(formState, formRules)

const validate = () =>
  validateForm().then(() => Promise.resolve(toRaw(formState)))

defineExpose({ validate })

// 位置树数据
const treeData = shallowRef([])
const fetchDirectory = payload => {
  const params = {
    position: 'DATASET',
    type: 'ALL',
    ...payload
  }
  return getDirectory(params).then(r => {
    treeData.value = r.children
  })
}

const defaultWorkspaceId = appStore.workspaceId || appStore.workspaceList[0]?.id

fetchDirectory({ workspaceId: defaultWorkspaceId })

const onWorkspaceChange = workspaceId => {
  fetchDirectory({ workspaceId }).then(() => {
    formState.folderId = -7 // 默认未分组
  })
}

onDeactivated(() => {
  resetFields()
})
onActivated(() => {
  initForm()
})
</script>

<style lang="scss">
.dir-select-tree {
  .ant-select-tree-treenode {
    position: relative;
    width: 100%;
  }
}
</style>
