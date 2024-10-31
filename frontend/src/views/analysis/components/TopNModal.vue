<template>
  <a-modal title="TopN排序" :open="open" @cancel="cancel">
    <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="计算类型">
        <a-radio-group
          v-model:value="formState.computeType"
          @change="onComputeType"
        >
          <a-radio value="all">全部结果取TopN</a-radio>
          <a-radio value="group">分组内取TopN</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item
        v-if="formState.computeType === 'group'"
        label="分组选择"
        v-bind="validateInfos.sortGroup"
      >
        <a-select
          placeholder="选择分组"
          show-search
          mode="multiple"
          allow-clear
          :field-names="{ label: 'displayName', value: 'name' }"
          :options="dimensions"
          :filterOption="onFilterOption"
          v-model:value="formState.sortGroup"
        ></a-select>
      </a-form-item>

      <a-form-item label="排序依据" v-bind="validateInfos.sortField">
        <a-input-group compact style="display: flex">
          <a-select
            style="flex: 1; overflow: hidden"
            placeholder="指标名称"
            allow-clear
            show-search
            :filterOption="onFilterOption"
            v-model:value="formState.sortField"
          >
            <a-select-option
              v-for="field in measures"
              :key="field.name + '.' + field.aggregator"
              :label="field.name + field.displayName"
            >
              {{ field.displayName }}
            </a-select-option>
          </a-select>
          <a-select style="width: 80px" v-model:value="formState.sortType">
            <a-select-option value="asc">升序</a-select-option>
            <a-select-option value="desc">降序</a-select-option>
          </a-select>
        </a-input-group>
      </a-form-item>

      <a-form-item label="TopN数目" v-bind="validateInfos.limit">
        <a-input-number
          v-model:value="formState.limit"
          :precision="0"
          :min="1"
          :max="1000"
          :step="10"
        />
        <span style="margin-left: 6px; vertical-align: sub; color: #aaa"
          >不超过1000</span
        >
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
    default: false
  },
  dimensions: {
    type: Array,
    default: () => []
  },
  measures: {
    type: Array,
    default: () => []
  },
  value: {
    type: Object,
    default: () => ({})
  }
})

const onFilterOption = (input, option) => {
  const str =
    (option.name ?? '') + (option.displayName ?? '') + (option.label ?? '')

  return str.toLowerCase().includes(input.toLowerCase())
}

// 表单数据
const formState = reactive({
  computeType: undefined, // 计算类型
  sortGroup: undefined, // 分组
  sortField: undefined, // 排序字段
  sortType: 'desc', // 排序类型
  limit: 50 // 限制数目
})
// 表单校验规则
const formRules = reactive({
  sortGroup: [
    { required: formState.computeType === 'group', message: '分组不能为空' }
  ],
  sortField: [{ required: true, message: '排序字段不能为空' }]
})
const { resetFields, validate, validateInfos } = Form.useForm(
  formState,
  formRules
)

const onComputeType = e => {
  const value = e.target.value

  formRules.sortGroup[0]['required'] = value === 'group'
  if (value === 'group') {
    const defaultItem = props.dimensions[0]?.name
    formState.sortGroup = defaultItem ? [defaultItem] : undefined
  } else {
    formState.sortGroup = undefined
  }
}

const init = () => {
  const {
    sortField,
    limit = 50,
    computeType = 'all',
    sortGroup,
    sortType = 'desc'
  } = props.value

  formState.computeType = computeType
  formState.sortGroup = sortGroup
  formState.sortType = sortType
  if (sortField) {
    formState.sortField = sortField
  } else {
    const first = props.measures[0]

    formState.sortField = first.name + '.' + first.aggregator
  }
  formState.limit = limit
}

watch(
  () => props.open,
  op => {
    op && init()
  }
)

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
