<template>
  <a-modal title="同环比设置" :open="open" :width="700" @cancel="cancel">
    <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
      <a-form-item label="对比日期" style="margin-bottom: 5px">
        {{ target.name || '-' }}
      </a-form-item>
      <a-form-item label="对比字段" v-bind="validateInfos.fields">
        <a-button
          v-if="!formState.fields.length"
          block
          type="dashed"
          style="height: 30px; padding: 0"
          :icon="h(PlusOutlined)"
          @click="insert()" />

        <div
          v-else
          class="fields-setting"
          style="max-height: 500px; margin-right: 70px">
          <div class="field-title field-item">
            <div class="cell name">字段名</div>
            <div class="cell val">对比类型</div>
          </div>
          <div
            class="field-item"
            v-for="(item, i) in formState.fields"
            :key="item._id">
            <span class="cell name" :title="item.label">
              <a-select
                placeholder="请选择字段"
                v-model:value="item.name"
                @change="e => onFieldNameChange(e, item)">
                <a-select-option v-for="f in fields" :key="f.name">
                  {{ f.displayName }}
                </a-select-option>
              </a-select>
            </span>
            <div class="cell val">
              <a-space-compact block>
                <a-select style="width: 90px" v-model:value="item.ratioType">
                  <a-select-option v-for="t in options" :key="t.value">
                    {{ t.label }}
                  </a-select-option>
                </a-select>

                <a-select style="flex: 1" v-model:value="item.period">
                  <a-select-option v-for="t in getOptions2(item)" :key="t.value">
                    {{ t.label }}
                  </a-select-option>
                </a-select>
              </a-space-compact>
            </div>
            <a-space class="action" style="margin: 4px 0 0 10px">
              <a-button
                type="text"
                size="small"
                :icon="h(PlusOutlined)"
                @click="insert(i)" />

              <a-button
                type="text"
                size="small"
                :icon="h(MinusOutlined)"
                @click="del(i)" />
            </a-space>
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
import { h, ref, reactive, watch, computed, toRaw } from 'vue'
import { Form } from 'ant-design-vue'
import { MinusOutlined, PlusOutlined } from '@ant-design/icons-vue'
import {
  ratioOptions,
  dateGroupTypeMap,
  DEFAULT_DATE_GROUP,
  DEFAULT_RATIO_TYPE,
  COMPARE_RATIO_PERIOD,
} from '@/views/analysis/config'
import { getRandomKey, deepClone, getByInlcudesKeys } from 'common/utils/help'

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
    default: DEFAULT_RATIO_TYPE,
    validator: s =>
      [
        DEFAULT_RATIO_TYPE,
        'WEEK_ON_WEEK',
        'MONTH_ON_MONTH',
        'YEAR_ON_YEAR',
      ].includes(s),
  },
  value: {
    type: Array,
    default: () => [],
  },
})

// 可对比的字段
const fields = computed(() =>
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
const list = ref([])

// 根据对比日期的聚合方式筛选对比类型
const options = computed(() => {
  const optionKeys = Object.keys(
    dateGroupTypeMap[props.target.dateTrunc || DEFAULT_DATE_GROUP]
  )

  return ratioOptions.filter(o => optionKeys.includes(o.value))
})

const getOptions2 = field => {
  const { ratioType = DEFAULT_RATIO_TYPE } = field
  const dateTrunc = props.target.dateTrunc || DEFAULT_DATE_GROUP

  return dateGroupTypeMap[dateTrunc][ratioType]
}

const init = () => {
  const oldCompareType = props.type || DEFAULT_RATIO_TYPE

  formState.type = oldCompareType
  formState.target = props.target
  formState.fields = props.value.map(t => {
    return {
      ...t,
      ratioType: t.ratioType || oldCompareType,
      period: t.period || COMPARE_RATIO_PERIOD.DEFAULT,
    }
  })
}

watch(
  () => props.open,
  b => {
    b && init()
  }
)

const onFieldNameChange = (name, field) => {
  const item = fields.value.find(t => t.name === name)

  field.aggregator = item.aggregator
}

const formState = reactive({
  type: props.type,
  target: props.target,
  fields: [],
})

const getUniqueByKey = (list = [], key) => {
  return [...new Set(list.map(t => t[key]))]
}

const formRules = reactive({
  fields: [
    {
      required: true,
      message: '对比字段不能为空',
      validator: (rule, value) => {
        if (!value?.length) {
          return Promise.reject(rule.message)
        } else {
          if (value.every(t => !t.name)) {
            return Promise.reject(rule.message)
          } else {
            return Promise.resolve()
          }
        }
      },
    },
    {
      message: '对比配置重复',
      validator: (rule, value) => {
        console.log('0', 0)
        if (!value.length) return Promise.resolve()

        const strArr = value
          .filter(v => !!v.name)
          .map(val => {
            const { name, ratioType, period } = val

            return name + '.' + ratioType + '.' + period
          })
        const uniqueArr = [...new Set(strArr)]

        if (strArr.length !== uniqueArr.length) {
          return Promise.reject(rule.message)
        } else {
          return Promise.resolve()
        }
      },
    },
  ],
})
const { resetFields, validate, validateInfos } = useForm(formState, formRules)

const insert = i => {
  if (typeof i === 'undefined') {
    formState.fields.push({
      ...fields.value[0],
      name: undefined,
      ratioType: DEFAULT_RATIO_TYPE,
      period: COMPARE_RATIO_PERIOD.DEFAULT,
      _id: getRandomKey(6),
    })
  } else {
    const item = formState.fields[i]

    formState.fields.splice(i + 1, 0, { ...item, _id: getRandomKey(6) })
  }
}

const del = i => {
  formState.fields.splice(i, 1)
}

const ok = () => {
  validate()
    .then(() => {
      const fields = formState.fields
        .filter(t => !!t.name)
        .map(t =>
          getByInlcudesKeys(t, ['name', 'aggregator', 'ratioType', 'period'])
        )
      console.log('fields', fields)

      emits('ok', { ...toRaw(formState), fields })
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
  position: relative;
  display: flex;
  width: 100%;

  .cell {
    flex: 1;
    padding: 6px 12px;
  }

  .name {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .val {
    border-left: 1px solid $table-color;
  }

  .action {
    position: absolute;
    top: 50%;
    left: 100%;
    transform: translateY(-50%);
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
