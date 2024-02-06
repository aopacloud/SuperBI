<template>
  <a-modal
    title="驳回"
    okText="驳回"
    :open="open"
    :confirmLoading="confirmLoading"
    @cancel="cancel"
    @ok="ok">
    <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="数据集">
        {{ initData.datasetName }}
      </a-form-item>
      <a-form-item label="申请人">
        {{ initData.aliasName }}
      </a-form-item>
      <a-form-item label="驳回理由" v-bind="validateInfos.authorizeRemark">
        <a-textarea
          placeholder="请输入驳回理由"
          v-model:value="formState.authorizeRemark"
          :autoSize="{ minRows: 3, maxRows: 6 }"></a-textarea>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Form, message } from 'ant-design-vue'
import { postApplyReject, postAuthorizeReject } from '@/apis/apply'

const emits = defineEmits(['update:open', 'success'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
  mode: {
    type: String,
    default: 'APPLY',
    validator: str => ['APPLY', 'AUTHORIZE'].includes(str),
  },
})

const formState = reactive({
  authorizeRemark: '',
})
const formRules = reactive({
  authorizeRemark: [{ required: true, message: '请输入驳回理由' }],
})

const { validate, resetFields, validateInfos } = Form.useForm(formState, formRules)

const cancel = () => {
  emits('update:open', false)

  resetFields()
}
const ok = () => {
  validate().then(res => {
    submit(res)
  })
}

const confirmLoading = ref(false)
const submit = async params => {
  try {
    confirmLoading.value = true

    const fn = props.mode === 'AUTHORIZE' ? postAuthorizeReject : postApplyReject
    const res = await fn(props.initData.id, params)

    message.success('驳回成功')
    emits('success', res)
    cancel()
  } catch (error) {
    console.error('驳回失败', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
