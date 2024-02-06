<template>
  <a-modal
    title="绑定手机号"
    width="420px"
    :open="open"
    :confirmLoading="submitLoading"
    @cancel="cancel"
    @ok="ok">
    <a-form :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }">
      <a-form-item label="原手机号">
        {{ initData.mobile }}
      </a-form-item>

      <a-form-item label="新手机号" v-bind="validateInfos.mobile">
        <a-input placeholder="请输入新手机号" v-model:value="formState.mobile" />
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
import { putMobile } from '@/apis/system/profile'
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
  mobile: '',
  code: '',
  uuid: '',
})

const formRules = reactive({
  mobile: [
    { required: true, message: '手机号不能为空' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入有效的手机号',
      trigger: 'blur',
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

    await putMobile(payload)

    message.success('邮箱绑定成功')
    emits('ok', formState.mobile)
    cancel()
  } catch (error) {
    console.error('邮箱绑定失败', error)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
