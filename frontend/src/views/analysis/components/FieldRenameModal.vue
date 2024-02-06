<template>
  <a-modal title="指标重命名" :width="400" :open="open" @cancel="cancel">
    <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 17 }">
      <a-form-item label="字段名">{{ field.name || '-' }}</a-form-item>
      <a-form-item label="展示名称" v-bind="validateInfos.displayName">
        <a-input v-model:value="formState.displayName" />
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button danger @click="reset">恢复默认</a-button>
      <a-button @click="cancel">取消</a-button>
      <a-button type="primary" @click="ok">确认</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { Form } from 'ant-design-vue'

const emits = defineEmits(['update:open', 'ok', 'reset'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  field: {
    type: Object,
    default: () => ({}),
  },
})

watch(
  () => props.open,
  b => {
    b && initForm()
  }
)

const formState = reactive({
  displayName: '',
})
const formRules = reactive({
  displayName: [{ required: true, message: '展示名称不能为空' }],
})
const useForm = Form.useForm
const { resetFields, validate, validateInfos } = useForm(formState, formRules)

const initForm = () => {
  formState.displayName = props.field.displayName
}

const cancel = () => {
  resetFields()
  emits('update:open', false)
}
const ok = () => {
  validate()
    .then(res => {
      emits('ok', props.field, res)
      cancel()
    })
    .catch(err => {
      console.error(err)
    })
}
const reset = () => {
  emits('reset', props.field)
  cancel()
}
</script>
