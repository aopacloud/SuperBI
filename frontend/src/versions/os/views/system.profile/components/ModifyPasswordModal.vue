<template>
  <a-modal
    title="修改密码"
    width="420px"
    :open="open"
    :confirmLoading="submitLoading"
    @cancel="cancel"
    @ok="ok">
    <a-form :labelCol="{ span: 7 }" :wrapperCol="{ span: 187 }">
      <a-form-item label="原密码" v-bind="validateInfos.oldPassword">
        <a-input
          placeholder="请输入原密码"
          type="text"
          v-model:value="formState.oldPassword" />
      </a-form-item>

      <a-form-item label="新密码" v-bind="validateInfos.newPassword">
        <a-input placeholder="请输入新密码" v-model:value="formState.newPassword" />
      </a-form-item>

      <a-form-item label="密码确认密码" v-bind="validateInfos.confirmPassword">
        <a-input
          placeholder="请再次输入新密码"
          v-model:value="formState.confirmPassword" />
      </a-form-item>

      <a-form-item label="验证码" v-bind="validateInfos.code">
        <Compact style="width: 100%">
          <a-input placeholder="请输入验证码" v-model:value="formState.code" />
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
import { putPassword } from '@/apis/system/profile'
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
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  code: '',
  uuid: '',
})

const formRules = reactive({
  oldPassword: [{ required: true, message: '原密码不能为空' }],
  newPassword: [
    { required: true, message: '新密码不能为空' },
    { min: 8, max: 20, message: '密码长度不能小于8位', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: '密码只能由字母、数字、下划线组成',
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码' },
    {
      validator: (rule, value) => {
        if (value && value !== formState.newPassword) {
          return Promise.reject(rule.message)
        }

        return Promise.resolve()
      },
      message: '两次输入密码不一致',
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

    await putPassword(payload)

    message.success('邮箱绑定成功')
    emits('ok')
    cancel()
  } catch (error) {
    console.error('邮箱绑定失败', error)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
