<template>
  <a-modal
    :open="open"
    :title="title"
    :confirmLoading="confirmLoading"
    @cancel="cancel"
    @ok="ok">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="名称" v-bind="validateInfos.name">
        <a-input placeholder="请输入成员组名称" v-model:value="formState.name" />
      </a-form-item>
      <a-form-item label="描述" v-bind="validateInfos.description">
        <a-textarea
          placeholder="请输入成员组描述"
          v-model:value="formState.description"
          :rows="4" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch, toRaw } from 'vue'
import { Form, message } from 'ant-design-vue'
import { createRole, updateRole } from '@/apis/workspace/role'

const emits = defineEmits(['update:open', 'ok'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
  workspaceId: Number,
})

const title = computed(() => (!!props.initData.id ? '编辑成员组' : '创建成员组'))

const formState = reactive({
  name: undefined,
  description: undefined,
})
const formRules = reactive({
  name: [{ required: true, message: '成员组名称不能为空' }],
  description: [{ max: 200, message: '描述最大不能超过200字符' }],
})
const { validate, validateInfos, resetFields } = Form.useForm(formState, formRules)
const initForm = () => {
  for (const key in formState) {
    formState[key] = props.initData[key]
  }
}

watch(
  () => props.open,
  op => {
    if (op) {
      initForm()
    } else [resetFields()]
  }
)

const cancel = () => {
  emits('update:open', false)
}

const ok = () => {
  validate().then(() => {
    submit(toRaw(formState))
  })
}

const confirmLoading = ref(false)
const submit = async payload => {
  try {
    confirmLoading.value = true

    const id = props.initData.id
    const fn = !!id
      ? () =>
          updateRole(id, { ...props.initDta, ...payload, workspaceId: props.workspaceId })
      : () => createRole({ ...payload, workspaceId: props.workspaceId })

    const res = await fn()

    message.success(`成员组${!!id ? '编辑' : '创建'}成功`)
    emits('ok', res)
    cancel()
  } catch (error) {
    console.error('成员组创建\编辑失败', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
