<template>
  <a-modal title="同环比设置" :open="open" :width="700" @cancel="cancel">
    <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
      <a-form-item label="对比日期" style="margin-bottom: 5px">
        <a-select
          placeholder="请选择对比日期"
          show-search
          optionLabelProp="label"
          :filterOption="filterOption"
          v-model:value="formState.timeField"
          @change="onTargetChange">
          <a-select-option
            v-for="t in dimensionsList"
            :key="t.name"
            :label="t.displayName">
            {{ t.displayName }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="对比维度">
        <a-select
          style="width: calc(100% - 70px)"
          placeholder="请选择对比维度"
          mode="multiple"
          show-search
          optionLabelProp="label"
          :filterOption="filterOption"
          v-model:value="dimensionsFields">
          <a-select-option
            v-for="t in dimensionsList"
            :key="t.name"
            :label="t.displayName"
            :disabled="t.disabled">
            {{ t.displayName + '(' + t.name + ')' }}
          </a-select-option>
        </a-select>

        <a-tooltip
          title="对比维度默认与计算原值的维度相同，若改变对比维度，可能会出现数据歧义，请谨慎操作">
          <InfoCircleFilled
            style="margin-left: 12px; font-size: 16px; color: rgba(0, 0, 0, 0.45)" />
        </a-tooltip>
      </a-form-item>

      <a-form-item label="对比字段" v-bind="validateInfos.measures">
        <a-button
          v-if="!formState.measures.length"
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
            v-for="(item, i) in formState.measures"
            :key="item._id">
            <div class="cell name" :title="item.label">
              <a-select
                placeholder="请选择字段"
                v-model:value="item.name"
                @change="e => onFieldNameChange(e, item)">
                <a-select-option v-for="f in measuresList" :key="f.name">
                  {{ f.displayName }}
                </a-select-option>
              </a-select>
            </div>
            <div class="cell val">
              <a-space-compact block>
                <a-select
                  style="width: 90px"
                  placeholder="请选择类型"
                  v-model:value="item.ratioType"
                  @change="onRatioTypeChange(item)">
                  <a-select-option v-for="t in options" :key="t.value">
                    {{ t.label }}
                  </a-select-option>
                </a-select>

                <a-select
                  style="flex: 1; overflow: hidden"
                  placeholder="请选择周期"
                  v-model:value="item.period">
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
import { MinusOutlined, PlusOutlined, InfoCircleFilled } from '@ant-design/icons-vue'
import {
  ratioOptions,
  dateGroupTypeMap,
  DEFAULT_DATE_GROUP,
  DEFAULT_RATIO_TYPE,
  GROUP_MINUTE,
} from '@/views/analysis/config'
import { getByIncludesKeys } from 'common/utils/help'
import { getRandomKey } from 'common/utils/string'
import { toContrastFiled } from '@/views/analysis/config'

const useForm = Form.useForm
const emits = defineEmits(['update:open', 'ok', 'close'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },

  dimensions: {
    type: Array,
    default: () => [],
  },
  // 可对比字段
  measures: {
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
  compare: {
    type: Object,
    default: () => ({}),
  },
})

// 可对比的维度
const dimensionsList = computed(() => {
  return props.dimensions.map(t => {
    return {
      ...t,
      disabled: formState.timeField === t.name,
    }
  })
})
const filterOption = (input, option) =>
  option.children()[0]['children'].indexOf(input.toLowerCase()) >= 0

// 可对比的指标
const measuresList = computed(() =>
  props.measures.filter(item => {
    if (
      (item.dataType === 'TEXT' || item.type === 'TIME') &&
      ['4', '5'].includes(item.summaryMethod)
    ) {
      return false
    }
    return true
  })
)

// 根据对比日期的聚合方式筛选对比类型
const options = computed(() => {
  const { dateTrunc = '' } = formState.target
  const _dateTrunc = dateTrunc.startsWith(GROUP_MINUTE) ? GROUP_MINUTE : dateTrunc
  const optionKeys = Object.keys(dateGroupTypeMap[_dateTrunc || DEFAULT_DATE_GROUP])

  return ratioOptions.filter(o => optionKeys.includes(o.value))
})

const getOptions2 = field => {
  const { ratioType = DEFAULT_RATIO_TYPE } = field
  const { dateTrunc = DEFAULT_DATE_GROUP } = formState.target
  const _dateTrunc = dateTrunc.startsWith(GROUP_MINUTE) ? GROUP_MINUTE : dateTrunc

  return dateGroupTypeMap[_dateTrunc][ratioType] || []
}

const init = () => {
  const {
    timeField,
    type = DEFAULT_RATIO_TYPE,
    measures = [],
    dimensions = [],
  } = props.compare

  formState.target =
    props.dimensions.find(t => t.name === timeField) ||
    props.dimensions.find(toContrastFiled)
  formState.timeField = formState.target.name
  formState.type = type
  formState.measures = measures.map(t => {
    const ratioType = t.ratioType || type
    const opts = getOptions2({ ratioType })
    return {
      ...t,
      ratioType,
      period: t.period || opts[0]?.value,
    }
  })
  if (typeof props.compare.dimensions === 'undefined') {
    formState.dimensions = [...props.dimensions]
  } else {
    formState.dimensions = dimensions
  }
}

watch(
  () => props.open,
  b => {
    b && init()
  }
)

const onFieldNameChange = (name, field) => {
  const item = measuresList.value.find(t => t.name === name)

  field.aggregator = item.aggregator
}

const onRatioTypeChange = item => {
  const { period } = item
  if (!period) return

  const options2 = getOptions2(item)
  if (options2.every(t => t.value !== period)) {
    item.period = options2[0]?.value
  }
}

const formState = reactive({
  type: DEFAULT_RATIO_TYPE,
  target: {},
  timeField: undefined,
  dimensions: [],
  measures: [],
})

const onTargetChange = e => {
  formState.target = dimensionsList.value.find(t => t.name === e)
  // 重置对比字段配置
  formState.measures.forEach(item => {
    // 更新类型
    if (options.value.every(t => t.value !== item.ratioType)) {
      item.ratioType = DEFAULT_RATIO_TYPE
    }
    // 更新周期
    const periodOptions = getOptions2({ ratioType: DEFAULT_RATIO_TYPE })
    item.period = periodOptions[0]?.value
  })
}

const dimensionsFields = computed({
  get() {
    return formState.dimensions.map(t => t.name)
  },
  set(values = []) {
    formState.dimensions = values.map(t => props.dimensions.find(f => f.name === t))
  },
})

const formRules = reactive({
  measures: [
    {
      required: true,
      message: '对比字段不能为空',
      validator: (rule, value) => {
        if (!value?.length) {
          return Promise.reject(rule.message)
        } else {
          if (value.every(t => !t.name || !t.period)) {
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
    const periodOptions = getOptions2({ ratioType: DEFAULT_RATIO_TYPE })

    formState.measures.push({
      ...measuresList.value[0],
      name: undefined,
      ratioType: DEFAULT_RATIO_TYPE,
      period: periodOptions[0]?.value,
      _id: getRandomKey(6),
    })
  } else {
    const item = formState.measures[i]

    formState.measures.splice(i + 1, 0, { ...item, _id: getRandomKey(6) })
  }
}

const del = i => {
  formState.measures.splice(i, 1)
}

const ok = () => {
  validate()
    .then(() => {
      const dimensions = formState.dimensions.map(t =>
        getByIncludesKeys(t, ['name', 'dataType'])
      )

      const measures = formState.measures
        .filter(t => !!t.name)
        .map(t =>
          getByIncludesKeys(t, ['name', 'aggregator', 'ratioType', 'period'])
        )

      emits('ok', { ...toRaw(formState), dimensions, measures })
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
    overflow: auto;
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
