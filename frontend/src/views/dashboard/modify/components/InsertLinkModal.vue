<template>
  <a-form
    class="link-form"
    style="width: 260px"
    :label-col="{ span: 5 }"
    :wrapper-col="{ span: 18 }"
    @submit.prevent="handleSubmit">
    <a-form-item label="文本" v-bind="validateInfos.label">
      <a-input size="small" placeholder="请输入文本" v-model:value="formState.label" />
    </a-form-item>
    <a-form-item label="链接" v-bind="validateInfos.link">
      <a-input size="small" placeholder="请输入链接" v-model:value="formState.link" />
    </a-form-item>
    <a-form-item :wrapper-col="{ span: 23 }" style="text-align: right">
      <a-space>
        <a-button size="small" @click="handleCancel">取消</a-button>
        <a-button size="small" type="primary" html-type="submit">确认</a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script setup>
import { watchEffect, nextTick } from 'vue'
import { Form } from 'ant-design-vue'

const props = defineProps({
  initData: {
    type: Object,
    default: () => ({}),
  },
})

// useForm 用法
const useForm = Form.useForm
// 表单数据
const formState = reactive({
  label: '',
  link: '',
})
// 表单校验规则
const formRules = reactive({
  label: [{ max: 10, message: '文本最大不超过10个字符' }],
  link: [
    { required: true, message: '链接不能为空' },
    {
      pattern:
        /^https?:\/\/([\w\-]+(\.[\w\-]+)*\/)*[\w\-]+(\.[\w\-]+)*\/?(\?([\w\-\.,@?^=%&:\/~\+#]*)+)?/i,
      message: '请输入正确的链接地址',
    },
  ],
})
const { resetFields, validate, validateInfos } = useForm(formState, formRules)

const init = () => {
  nextTick(() => {
    ;['label', 'link'].forEach(k => {
      formState[k] = props.initData[k]
    })
  })
}

const reset = () => {
  resetFields()
}

watchEffect(() => {
  if (Object.keys(props.initData).length > 0) {
    init()
  } else {
    reset()
  }
})

const emits = defineEmits(['cancel', 'ok'])
const handleCancel = () => {
  emits('cancel')
}

const handleSubmit = () => {
  validate().then(r => {
    emits('ok', r)
  })
}
</script>

<style scoped lang="scss">
.link-form {
  :deep(.ant-form-item):not(:nth-child(2)) {
    margin-bottom: 0;
  }
}
</style>
