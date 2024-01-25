<template>
  <a-modal
    okText="提交"
    :open="open"
    :title="title"
    :confirm-loading="confirmLoading"
    @cancel="cancel"
    @ok="ok">
    <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }" autocomplete="off">
      <a-form-item label="数据集">
        {{ initData.name || '-' }}
      </a-form-item>

      <a-form-item label="使用期限" v-bind="validateInfos.expireDuration">
        <a-radio-group v-model:value="formState.expireDuration">
          <a-radio v-for="time in times" :key="time.duration" :value="time.duration">
            {{ time.label }}
          </a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="申请理由" v-bind="validateInfos.reason">
        <a-textarea :rows="3" v-model:value="formState.reason" />

        <template #extra>
          <p style="position: absolute; left: -68px">申请流程:</p>
          <ul class="process">
            <li
              v-for="(tip, index) in versionJs.ComponentsApplyModal.stepTips"
              :key="index">
              {{ tip }}
            </li>
          </ul>
        </template>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="jsx">
import { ref, reactive, computed, toRaw, watch } from 'vue'
import { Form, message } from 'ant-design-vue'
import useAppStore from '@/store/modules/app'
import { postApply } from '@/apis/dataset/apply'
import { versionJs } from '@/versions'

const appStore = useAppStore()
const workspaceId = computed(() => appStore.workspaceId)

// utils - 天转秒
function dayToSec(d) {
  return d * 24 * 60 * 60
}
// vue3 - 定义 emits
const emits = defineEmits(['update:open', 'ok'])
// vue3 - 定义 props
const props = defineProps({
  open: {
    type: Boolean,
    default: true,
  },
  title: {
    type: String,
    default: '数据集权限申请',
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
})
// 渲染数据
const times = reactive([
  { label: '7天', value: '7', duration: dayToSec(7) },
  { label: '30天', value: '30', duration: dayToSec(30) },
  { label: '90天', value: '90', duration: dayToSec(90) },
  { label: '180天', value: '180', duration: dayToSec(180) },
])

// 表单数据
const formState = reactive({
  name: undefined,
  expireDuration: dayToSec(7),
  reason: undefined,
})
// 表单校验规则
const formRules = reactive({
  reason: [
    { required: true, message: '申请理由不能为空' },
    { max: 200, message: '最大不得超过200个字符' },
  ],
})
const { resetFields, validate, validateInfos } = Form.useForm(formState, formRules)

// vue3 - 注册一个监听器
watch(
  () => props.open,
  n => {
    if (!n) {
      resetFields()
    }
  }
)

// 按钮loading
const confirmLoading = ref(false)
// modal取消
const cancel = () => {
  emits('update:open', false)
}
// modal确认
const ok = () => {
  validate()
    .then(() => {
      submit(toRaw(formState))
    })
    .catch(err => {
      console.error('error', err)
    })
}
// 提交
const submit = async payload => {
  try {
    confirmLoading.value = true

    const res = await postApply({
      datasetId: props.initData.id,
      datasource: props.initData.source,
      workspaceId: workspaceId.value,
      ...payload,
    })

    message.success(versionJs.ComponentsApplyModal.successInfo)

    emits('ok', res)
    cancel()
  } catch (error) {
    console.error('授权错误', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.process {
  padding-left: 20px li {
    list-style: initial;
  }
}
</style>
