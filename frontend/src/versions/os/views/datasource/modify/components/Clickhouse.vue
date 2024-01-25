<template>
  <a-form
    style="max-width: 1200px"
    :label-col="{ style: { width: '200px' } }"
    :wrapperCol="{ span: 14 }">
    <a-form-item label="数据库" v-bind="validateInfos.database">
      <a-input placeholder="数据库" v-model:value="formState.database" />
    </a-form-item>

    <a-form-item label="显示名称" v-bind="validateInfos.name">
      <a-input placeholder="显示名称" v-model:value="formState.name" />
    </a-form-item>

    <a-form-item label="主机" v-bind="validateInfos.host">
      <a-input placeholder="主机" v-model:value="formState.host" />
    </a-form-item>

    <a-form-item label="端口" v-bind="validateInfos.port">
      <a-input placeholder="端口" v-model:value.number="formState.port" />
    </a-form-item>

    <a-form-item label="数据库地址" v-bind="validateInfos.url">
      <a-input readonly placeholder="数据库地址" v-model:value="formState.url" />
    </a-form-item>

    <a-form-item label="用户名" v-bind="validateInfos.user">
      <a-input placeholder="登录数据集的用户名" v-model:value="formState.user" />
    </a-form-item>

    <a-form-item label="密码" v-bind="validateInfos.password">
      <a-input placeholder="登录数据集的密码" v-model:value="formState.password" />
      <template #help>
        <a :disabled="connectting" @click="emits('connect')">点击连接数据库</a>
      </template>
    </a-form-item>

    <a-form-item label="SSL" v-bind="validateInfos.sslEnable">
      <a-checkbox v-model:checked="formState.sslEnable" />
    </a-form-item>

    <a-form-item
      label="初始化SQL"
      v-bind="validateInfos.initSql"
      style="line-height: 32px">
      <a-checkbox v-model:checked="initSqlEnable" @change="onInitSqlEnableChange" />

      <a-textarea
        v-if="initSqlEnable"
        placeholder="请输入 set 开头的SQL语句"
        :rows="4"
        v-model:value="formState.initSql" />
    </a-form-item>
  </a-form>
</template>

<script setup>
import { reactive, toRaw, watch, onMounted, nextTick } from 'vue'
import { Form } from 'ant-design-vue'

const emits = defineEmits(['connect'])

const props = defineProps({
  connectting: {
    type: Boolean,
    default: false,
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
})

const formState = reactive({
  database: '',
  name: undefined,
  host: '',
  port: '',
  url: '',
  user: '',
  password: '',
  sslEnable: false,
  initSql: undefined,
})

// 初始化SQL复选框
const initSqlEnable = ref(false)
const onInitSqlEnableChange = e => {
  const checked = e.target.checked

  if (!checked) {
    formState.initSql = undefined
  }
}
watch(initSqlEnable, checked => {
  formRules.initSql[0]['required'] = checked

  if (!checked) formState.initSql = undefined
})

const formRules = reactive({
  database: [{ required: true, message: '数据库不能为空' }],
  name: [{ required: true, message: '显示名称不能为空' }],
  host: [{ required: true, message: '主机不能为空' }],
  port: [{ required: true, message: '端口号不能为空' }],
  user: [{ required: true, message: '用户名不能为空' }],
  password: [{ required: true, message: '密码不能为空' }],
  initSql: [{ required: false, message: '初始化SQL不能为空' }],
})

const initFormData = () => {
  for (const key in formState) {
    formState[key] = props.initData[key]

    if (key === 'initSql' && formState[key]) {
      initSqlEnable.value = true
    }
  }

  nextTick(() => {
    clearValidate()
  })
}

onMounted(() => {
  initFormData()
})

watch(
  () => [formState.host, formState.port],
  ([host, port]) => {
    if (host || port) {
      formState.url = (host ?? '') + ':' + (port ?? '')
    }
  }
)

watch(() => props.initData.id, initFormData)

const {
  validate: formValidate,
  clearValidate,
  validateInfos,
} = Form.useForm(formState, formRules)

const validate = () => {
  return formValidate()
    .then(() => {
      return Promise.resolve(toRaw(formState))
    })
    .catch(e => {
      return Promise.reject(e)
    })
}

defineExpose({
  validate,
})
</script>

<style lang="scss" scoped></style>
