<template>
  <a-modal
    :open="open"
    :title="title"
    :confirmLoading="confirmLoading"
    @cancel="cancel"
    @ok="ok">
    <a-form :labelCol="{ span: 4 }" :wrapperCol="{ span: 20 }">
      <a-form-item label="名称" v-bind="validateInfos.name">
        <a-input placeholder="请输入角色名称" v-model:value="formState.name" />
      </a-form-item>
      <a-form-item label="描述" v-bind="validateInfos.description">
        <a-textarea
          placeholder="请输入角色描述"
          :rows="4"
          v-model:value="formState.description" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch, computed, toRaw } from 'vue'
import { Form, message } from 'ant-design-vue'
import { postRole, updateRole } from '@/apis/system/role'

const emits = defineEmits(['update:open', 'cancel', 'ok'])
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

const title = computed(() => (!!props.initData.id ? '编辑角色' : '新增角色'))

const formState = reactive({
  name: '',
  description: '',
})

// 校验名称重复
const hasExited = str => {
  return props.allList.some(t => t.id !== props.initData.id && t.name === str?.trim())
}
const formRules = reactive({
  name: [
    { required: true, message: '名称不能为空' },
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

const { validate, resetFields, validateInfos } = Form.useForm(formState, formRules)

const init = () => {
  for (const key in formState) {
    formState[key] = props.initData[key]
  }
}

watch(
  () => props.open,
  op => {
    if (op) {
      props.initData.id && init()
    } else {
      resetFields()
    }
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
    const fn = !!id ? () => updateRole(id, payload) : () => postRole(payload)

    const res = await fn()

    message.success(`${!!id ? '编辑' : '新增'}角色成功`)
    emits('ok', res)
    cancel()
  } catch (error) {
    console.error('角色保存错误', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
