<template>
  <a-modal
    title="绑定邮箱"
    width="420px"
    :open="open"
    :confirmLoading="submitLoading"
    @cancel="cancel"
    @ok="ok">
    <a-form :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }">
      <a-form-item label="原邮箱">
        {{ initData.email }}
      </a-form-item>

      <a-form-item label="新邮箱" name="email" v-bind="validateInfos.email">
        <a-input
          name="email"
          placeholder="请输入新邮箱"
          v-model:value="formState.email" />
      </a-form-item>

      <a-form-item label="验证码" name="code" v-bind="validateInfos.code">
        <Compact style="width: 100%">
          <a-input
            name="code"
            placeholder="请输入验证码"
            v-model:value="formState.code" />
          <img
            style="height: 32px; margin-left: 6px; cursor: pointer"
            :src="codeImgUrl"
            @click="getCode" />
        </Compact>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, toRaw, watch } from 'vue'
import { Form, Compact, message } from 'ant-design-vue'
import { putEmail } from '@/apis/system/profile'
import { getCodeImg } from '@/apis/login'

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
})

const formState = reactive({
  email: '',
  code: '',
  uuid: '',
})

const formRules = reactive({
  email: [
    { required: true, message: '请输入邮箱' },
    {
      pattern: /\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*/,
      message: '请输入正确的邮箱',
    },
  ],
  code: [{ required: true, message: '请输入验证码' }],
})

const { validate, validateInfos, resetFields } = Form.useForm(formState, formRules)

// 验证码
const BASE64_PREFIX = 'data:image/gif;base64,'
const codeLoading = ref(false)
const codeImgUrl = ref('')
const getCode = async () => {
  try {
    codeLoading.value = true
    const { img, uuid } = await getCodeImg()

    codeImgUrl.value = BASE64_PREFIX + img
    formState.uuid = uuid
  } catch (error) {
    console.error('获取验证码失败', error)
  } finally {
    codeLoading.value = false
  }
}

watch(
  () => props.open,
  b => {
    if (b) {
      getCode()
    }
  }
)

const cancel = () => {
  resetFields()
  emits('update:open', false)
}
const ok = () => {
  validate().then(() => {
    submit(toRaw(formState))
  })
}

const submitLoading = ref(false)
const submit = async payload => {
  try {
    submitLoading.value = true

    await putEmail(payload)

    message.success('邮箱绑定成功')
    emits('ok', formState.email)
    cancel()
  } catch (error) {
    console.error('邮箱绑定失败', error)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
