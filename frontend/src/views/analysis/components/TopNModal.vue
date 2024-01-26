<template>
  <a-modal title="TopN排序" :width="400" :open="open" @cancel="cancel">
    <a-form :label-col="{ span: 7 }" :wrapper-col="{ span: 15 }">
      <a-form-item label="排序字段" v-bind="validateInfos.sortField">
        <a-select placeholder="选择字段" allow-clear v-model:value="formState.sortField">
          <a-select-option
            v-for="field in dataSource"
            :key="field.name + '.' + field.aggregator">
            {{ field.displayName }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="TopN数目" v-bind="validateInfos.value">
        <a-input-number
          v-model:value="formState.limit"
          :precision="0"
          :min="1"
          :max="1000"
          :step="10" />
        <span style="margin-left: 6px; vertical-align: sub; color: #aaa">不超过1000</span>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button danger @click="close">关闭功能</a-button>
      <a-button @click="cancel">取消</a-button>
      <a-button type="primary" @click="ok">确认</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, watch, toRaw } from 'vue'
import { Form } from 'ant-design-vue'

const emits = defineEmits(['ok', 'close', 'update:open'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
  value: {
    type: Object,
    default: () => ({}),
  },
})

watch(
  () => props.open,
  op => {
    if (op) {
      const { sortField, limit = 50 } = props.value

      if (sortField) {
        formState.sortField = sortField
      } else {
        const first = props.dataSource[0]

        formState.sortField = first.name + '.' + first.aggregator
      }
      formState.limit = limit
    }
  }
)

// useForm 用法
const useForm = Form.useForm
// 表单数据
const formState = reactive({
  sortField: undefined,
  limit: 50,
})
// 表单校验规则
const formRules = reactive({
  sortField: [{ required: true, message: '排序字段不能为空' }],
})
const { resetFields, validate, validateInfos } = useForm(formState, formRules)

const cancel = () => {
  emits('update:open', false)
  resetFields()
}
const ok = () => {
  validate().then(() => {
    emits('ok', toRaw(formState))
    cancel()
  })
}
const close = () => {
  emits('close')
  cancel()
}
</script>
