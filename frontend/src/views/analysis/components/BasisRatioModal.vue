<template>
  <a-modal title="同环比设置" :open="open" :width="500" @cancel="cancel">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="对比日期" style="margin-bottom: 5px">
        {{ target.name || '-' }}
      </a-form-item>
      <a-form-item label="对比字段" v-bind="validateInfos.fields">
        <a-select
          mode="multiple"
          allow-clear
          placeholder="请选择对比字段"
          v-model:value="formState.fields"
          @change="onFieldsChange">
          <a-select-option v-for="t in fieldList" :key="t.name">
            {{ t.displayName }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item v-show="choiceList.length" label=" " :colon="false">
        <div class="fields-setting" style="max-height: 500px">
          <div class="field-title field-item">
            <div class="name">字段名</div>
            <div class="val">对比类型</div>
            <div></div>
          </div>
          <div class="field-item" v-for="(d, i) in choiceList" :key="d.name">
            <span class="name" :title="d.label">{{ d.displayName }}</span>
            <div class="val custom-select small">
              <select v-model="compareType">
                <option v-for="o in options" :key="o.value" :value="o.value">
                  {{ o.label }}
                </option>
              </select>
            </div>
            <div class="action" style="text-align: center">
              <MinusCircleOutlined @click="handleDel(i)" />
            </div>
          </div>
        </div>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button danger @click="close">关闭同环比</a-button>
      <a-button @click="cancel">取消</a-button>
      <a-button type="primary" @click="ok">确认</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { Form } from 'ant-design-vue'
import { MinusCircleOutlined } from '@ant-design/icons-vue'
import { dateGroupTypeMap } from './config'
import {
  ratioOptions,
  DEFAULT_DATEGROUP,
  DEAULT_RATIO_TYPE,
} from '@/views/analysis/config'

const useForm = Form.useForm
const emits = defineEmits(['update:open', 'update:value', 'ok', 'close'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  // 被对比字段
  target: {
    type: Object,
    default: () => ({}),
  },
  // 可对比字段
  dataSource: {
    type: Array,
    default: () => [],
  },
  // 同环比类型
  type: {
    type: String,
    default: DEAULT_RATIO_TYPE,
    validator: s =>
      [DEAULT_RATIO_TYPE, 'WEEK_ON_WEEK', 'MONTH_ON_MONTH', 'YEAR_ON_YEAR'].includes(s),
  },
  value: {
    type: Array,
    default: () => [],
  },
})

// 可对比的字段
const fieldList = computed(() =>
  props.dataSource.filter(item => {
    if (
      (item.dataType === 'TEXT' || item.type === 'TIME') &&
      ['4', '5'].includes(item.summaryMethod)
    ) {
      return false
    }
    return true
  })
)

// 当前同环比
const compareType = ref('')

// 选中的列表
const choiceList = ref([])

// 根据对比日期的聚合方式筛选对比类型
const options = computed(() => {
  const optionKeys = dateGroupTypeMap[props.target.dateTrunc || DEFAULT_DATEGROUP]

  return ratioOptions.filter(o => optionKeys.includes(o.value))
})

const init = () => {
  compareType.value = props.type

  const fileds = fieldList.value
    .filter(t => props.value.some(v => v.name === t.name))
    .map(t => t.name)

  formState.fields = fileds
  onFieldsChange(fileds)
}

watch(
  () => props.open,
  b => {
    b && init()
  }
)

const formState = reactive({
  fields: [],
})
const formRules = reactive({
  fields: [{ required: true, message: '对比字段不能为空' }],
})
const { resetFields, validate, validateInfos } = useForm(formState, formRules)

const onFieldsChange = (values = []) => {
  choiceList.value = values
    .map(v => fieldList.value.find(f => f.name === v))
    .filter(Boolean)
}

const handleDel = i => {
  formState.fields.splice(i, 1)
  choiceList.value.splice(i, 1)
}

const ok = () => {
  validate()
    .then(() => {
      const payload = {
        target: props.target,
        fields: choiceList.value,
        type: compareType.value,
      }

      emits('ok', { ...payload })
      cancel()
    })
    .catch(error => {
      console.error(error)
    })
}
const cancel = () => {
  resetFields()
  emits('update:open', false)
}
const close = () => {
  emits('close')
  cancel()
}
</script>

<style scoped lang="scss">
$table-color: #e8e8e8;

.fields-setting {
  border: 1px solid $table-color;
  line-height: initial;
  border-radius: 2px;
}
.field-item {
  display: table;
  width: 100%;

  .name,
  .val,
  .action {
    display: table-cell;
    padding: 4px 6px;
  }

  .name {
    width: 180px;
    max-width: 180px;
    border-right: 1px solid $table-color;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .val {
    width: 120px;
  }
  &:not(:last-child) {
    border-bottom: 1px solid $table-color;
  }
}
.field-title {
  position: sticky;
  top: 0;
  background-color: #fafafa;
  z-index: 1;
}
</style>
