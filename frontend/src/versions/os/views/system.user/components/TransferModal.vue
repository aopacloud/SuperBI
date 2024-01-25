<template>
  <a-modal
    :open="open"
    :title="title"
    :width="560"
    :confirmLoading="submitLoading"
    @cancel="cancel"
    @ok="ok">
    <a-alert
      message="该用户下有负责的看板、数据集、数据源和图表，可将该用户的资源进行移交">
    </a-alert>

    <a-form :labelCol="{ span: 4 }" :wrapperCol="{ span: 20 }">
      <a-form-item label="移交给" v-bind="validateInfos.user">
        <a-select
          placeholder="搜索用户名"
          :loading="usrLoading"
          show-search
          :filter-option="filterOption"
          v-model:value="formState.user">
          <a-select-option v-for="user in selectUsers" :key="user.username">
            {{ user.aliasName }}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { Form, message } from 'ant-design-vue'
import {
  searchSysUser,
  transferResources,
  transferResourcesAndDeleteUser,
} from '@/apis/system/user'

const emits = defineEmits(['update:open', 'cancel', 'ok'])

const props = defineProps({
  initData: {
    type: Object,
    default: () => ({}),
  },
  mode: {
    type: String,
    default: 'transfer',
    validator: s => s === 'transfer' || s === 'delete',
  },
})

const title = computed(() => (props.mode === 'delete' ? '删除成员' : '移交资源'))

// 系统所有用户
const usrLoading = ref(false)
const users = shallowRef([])
// 获取系统所有用户
const fetchSysAllUsers = async () => {
  try {
    usrLoading.value = true
    const { data = [] } = await searchSysUser({ pageSize: 10000 })

    users.value = data.map(user => ({
      ...user,
      aliasName: `${user.aliasName}(${user.username})`,
    }))
  } catch (error) {
    console.error('获取系统用户列表失败', error)
  } finally {
    usrLoading.value = false
  }
}

const filterOption = (input, option) => {
  const label = option.children()[0].children

  return label ? label.toLowerCase().indexOf(input.toLowerCase()) >= 0 : false
}

onMounted(fetchSysAllUsers)

// 可选择的列表
const selectUsers = computed(() =>
  users.value.filter(t => t.username !== props.initData.username)
)

const formState = reactive({ user: undefined })
const formRules = reactive({
  user: [{ required: true, message: '请选择用户' }],
})

const { validate, validateInfos, resetFields } = Form.useForm(formState, formRules)

const cancel = () => {
  emits('update:open', false)
  resetFields()
}

const submitLoading = ref(false)

const ok = () => {
  validate().then(submit)
}

const submit = async payload => {
  try {
    submitLoading.value = true

    const params = {
      fromUsername: props.initData.username,
      toUsername: payload.user,
    }
    const fn =
      props.mode === 'delete' ? transferResourcesAndDeleteUser : transferResources

    await fn(params)

    message.success(props.mode === 'delete' ? '移交并删除成功' : '移交成功')

    emits('ok')
    cancel()
  } catch (error) {
    console.error('移交资源失败', error)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
