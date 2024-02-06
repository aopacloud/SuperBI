<template>
  <a-modal
    :open="open"
    :title="title"
    :confirmLoading="confirmLoading"
    @cancel="cancel"
    @ok="ok">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="名称" v-bind="validateInfos.name">
        <a-input placeholder="请输入空间名称" v-model:value="formState.name" />
      </a-form-item>
      <a-form-item label="描述" v-bind="validateInfos.description">
        <a-textarea
          placeholder="请输入空间描述"
          v-model:value="formState.description"
          :rows="4" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch, toRaw } from 'vue'
import { Form, message } from 'ant-design-vue'
import { createWorkspace, updateWorkspace } from '@/apis/workspace'

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
  allList: {
    type: Array,
    default: () => [],
  },
})

const title = computed(() => (!!props.initData.id ? '编辑空间' : '创建空间'))

const formState = reactive({
  name: undefined,
  description: undefined,
})
// 校验名称重复
const hasExited = str => {
  return props.allList.some(t => t.id !== props.initData.id && t.name === str?.trim())
}

const formRules = reactive({
  name: [
    { required: true, message: '空间名称不能为空' },
    {
      validator: (rule, value) => {
        if (hasExited(value)) {
          return Promise.reject(rule.message)
        }

        return Promise.resolve()
      },
      message: '名称不能重复',
    },
  ],
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
      ? () => updateWorkspace(id, { ...props.initDta, ...payload })
      : () => createWorkspace(payload)

    const res = await fn()

    message.success(`空间${!!id ? '编辑' : '创建'}成功`)
    emits('ok', res)
    cancel()
  } catch (error) {
    console.error('空间创建\编辑失败', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
