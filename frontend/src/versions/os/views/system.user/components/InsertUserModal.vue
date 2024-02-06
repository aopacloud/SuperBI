<template>
  <a-modal
    title="添加成员"
    ok-text="创建并导出"
    :width="700"
    :open="open"
    :confirmLoading="submitLoading"
    @cancel="cancel"
    @ok="ok">
    <a-form
      ref="formRef"
      :labelCol="{ span: 0 }"
      :wrapperCol="{ span: 24 }"
      :model="formState"
      @finish="submit">
      <a-space
        style="width: 100%"
        align="start"
        v-for="(user, index) in formState.users"
        :key="user._id">
        <a-form-item :name="['users', index, 'username']" :rules="formRules.username">
          <a-input placeholder="请输入账号，必填" v-model:value="user.username" />
        </a-form-item>

        <a-form-item :name="['users', index, 'aliasName']" :rules="formRules.aliasName">
          <a-input placeholder="请输入显示名" v-model:value="user.aliasName" />
        </a-form-item>

        <a-form-item>
          <a-input placeholder="请输入手机号" v-model:value="user.mobile" />
        </a-form-item>

        <a-form-item>
          <a-input placeholder="请输入邮箱" v-model:value="user.email" />
        </a-form-item>

        <a-button
          v-if="formState.users.length > 1"
          :icon="h(MinusCircleOutlined)"
          @click="removeUser(index)" />
      </a-space>
      <a-form-item>
        <a-button block type="dashed" :icon="h(PlusOutlined)" @click="insert">
          成员
        </a-button>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { h, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { addUsers } from '@/apis/system/user'
import { downloadWithBlob } from '@/common/utils/file'
import dayjs from 'dayjs'

const defaultItem = () => ({
  _id: Date.now(),
  username: '',
  aliasName: '',
  mobile: undefined,
  email: undefined,
})

const emits = defineEmits(['update:open', 'cancel', 'ok'])

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
})

const formState = reactive({
  users: [defaultItem()],
})

// 添加成员全部为空
const isAllEmpty = () => {
  return formState.users.every(t => !t.username.trim().length)
}
// 添加成员当前行为空
const isItemEmpty = i => {
  const item = formState.users[i]

  return !item.username.trim().length && !item.aliasName.trim().length
}

const customValidator = (rule, value) => {
  if (isAllEmpty()) {
    return Promise.reject(rule.message)
  } else {
    const index = rule.field.split('.').slice(1, 2)[0]
    if (isItemEmpty(index)) {
      return Promise.resolve()
    } else {
      return value.trim().length ? Promise.resolve() : Promise.reject(rule.message)
    }
  }
}

const formRef = ref()

const formRules = reactive({
  username: [
    {
      message: '账号不能为空',
      trigger: 'blur',
      validator: customValidator,
    },
    {
      pattern: /^[A-Za-z0-9_-]+$/,
      message: '用户名只能包含字母、数字、下划线和中划线',
      trigger: 'blur',
    },
  ],
  aliasName: [
    {
      message: '显示名不能为空',
      trigger: 'blur',
      validator: customValidator,
    },
  ],
})

const insert = () => {
  formState.users.push(defaultItem())
}

const removeUser = index => {
  formState.users.splice(index, 1)
}

const cancel = () => {
  emits('update:open', false)
  formState.users = [defaultItem()]
  formRef.value.resetFields()
}

const ok = () => {
  formRef.value
    .validate()
    .then(r => {
      const payload = toRaw(formState).users.filter(
        t => t.username.trim().length && t.aliasName.trim().length
      )

      submit(payload)
    })
    .catch(e => {
      console.error('e', e)
    })
}

const submitLoading = ref(false)
const submit = async payload => {
  try {
    submitLoading.value = true

    const res = await addUsers(payload)

    await downloadWithBlob(res, `用户信息-${dayjs().format('YYYY-MM-DD HH-mm-ss')}.xlsx`)

    message.success('创建并导出成功')

    emits('ok')
    cancel()
  } catch (error) {
    console.error('添加用户员失败', error)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
