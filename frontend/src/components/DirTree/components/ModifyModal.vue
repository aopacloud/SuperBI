<template>
  <a-modal
    :open="open"
    :title="title"
    :confirm-loading="confirmLoading"
    @cancel="onCancel"
    @ok="onOk">
    <slot name="alert"></slot>
    <a-spin :spinning="loading">
      <a-form
        class="ant-form--small"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 18 }">
        <a-form-item label="名称" v-bind="validateInfos.name">
          <a-input
            autocomplete="off"
            placeholder="请输入文件夹名称"
            v-model:value="formState.name" />
        </a-form-item>

        <a-form-item label="位置" v-bind="validateInfos.parentId">
          <a-tree-select
            popupClassName="dir-select-tree"
            placeholder="请选择位置"
            tree-default-expand-all
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            :tree-data="treeData"
            :field-names="{
              children: 'children',
              label: 'name',
              value: 'id',
            }"
            tree-node-filter-prop="name"
            v-model:treeExpandedKeys="expandedKeys"
            v-model:value="formState.parentId" />
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script setup>
import { ref, computed, reactive, watch, nextTick } from 'vue'
import { Form, message } from 'ant-design-vue'
import { getDirectory, postDirectory, putDirectory } from '@/apis/directory'
import { flat } from 'common/utils/help'

const emits = defineEmits(['update:open', 'success'])
const props = defineProps({
  open: {
    type: Boolean,
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
  initParams: {
    type: Object,
    default: () => ({
      position: 'DASHBOARD',
      type: 'ALL',
      workspaceId: 7,
    }),
  },
})

const isPersonal = computed(() => props.initParams.type === 'PERSONAL')

const title = computed(() => {
  return !!props.initData.id ? '编辑文件夹' : '新建文件夹'
})

const loading = ref(false)

const useForm = Form.useForm
const formState = reactive({
  name: undefined,
  parentId: undefined,
})
const formRules = reactive({
  name: [
    { required: true, message: '请输入文件夹名称' },
    { max: 20, message: '文件夹名称最多不超过20个字符' },
  ],
  parentId: [{ required: true, message: '请选择位置' }],
})

const { validate, validateInfos, resetFields } = useForm(formState, formRules)

const initFormState = () => {
  for (const key in formState) {
    const val = props.initData[key]

    formState[key] = key === 'parentId' ? val ?? (isPersonal.value ? undefined : -1) : val
  }
}

const treeData = ref([])
const expandedKeys = ref([])

const _toDisabled = list => {
  if (!Array.isArray(list) || !list.length) return

  return list.map(item => {
    return {
      ...item,
      disabled: item.id === -1,
    }
  })
}
const fetchData = async () => {
  try {
    loading.value = true

    const res = await getDirectory(props.initParams)

    treeData.value = isPersonal.value ? _toDisabled([res]) : [res]
    expandedKeys.value = flat(treeData.value, 'children').map(t => t.id)
  } catch (error) {
    console.error('获取文件夹错误', error)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  visible => {
    if (visible) {
      fetchData()
      initFormState()
    } else {
      nextTick(() => {
        resetFields()
      })
    }
  }
)

const onOk = () => {
  validate().then(res => {
    submit(res)
  })
}

const confirmLoading = ref(false)
const submit = async payload => {
  try {
    confirmLoading.value = true

    const isEdit = !!props.initData.id
    const params = { ...props.initParams, ...payload }
    const fn = isEdit
      ? () => putDirectory(props.initData.id, params)
      : () => postDirectory(params)

    await fn(params)

    message.success(`${isEdit ? '编辑' : '新建'}文件夹成功`)
    emits('update:open', false)
    emits('success')
  } catch (error) {
    console.error('新建/编辑文件夹错误', error)
  } finally {
    confirmLoading.value = false
  }
}

const onCancel = () => {
  emits('update:open', false)
}
</script>

<style lang="scss">
.dir-select-tree {
  .ant-select-tree-treenode {
    width: calc(100% - 4px);
  }
}
</style>
