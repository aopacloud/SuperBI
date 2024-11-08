<template>
  <a-form
    style="width: 500px; margin-top: 20px"
    :label-col="{ span: 6 }"
    :wrapper-col="{ span: 17 }"
  >
    <a-form-item label="过滤方式">
      <a-radio-group v-model:value="formState.mode" @change="onModeChange">
        <a-radio value="ENUM">枚举过滤</a-radio>
        <a-radio value="CONDITION">条件过滤</a-radio>
        <a-radio v-if="!single" value="CUSTOM">自定义</a-radio>
      </a-radio-group>
    </a-form-item>

    <a-form-item label="过滤范围" v-if="formState.mode === 'CONDITION'">
      <a-radio-group v-model:value="formState.scope">
        <a-radio v-if="!single" :disabled="scopeDisabled" :value="true">
          结果过滤
        </a-radio>
        <a-radio :value="false">明细过滤</a-radio>
      </a-radio-group>
    </a-form-item>

    <!-- 单条件无自定义、排除及上传 -->
    <a-form-item label="过滤条件" v-bind="validateInfos.conditions">
      <template #extra v-if="formState.mode !== 'CONDITION' && !single">
        <div class="extra-tools">
          <a-space>
            <a
              :disabled="uploadLoading"
              v-if="formState.mode === 'CUSTOM'"
              @click="upload"
              >上传
            </a>
            <a-checkbox v-model:checked="formState.exclude">排除</a-checkbox>
          </a-space>
          <a-tooltip
            v-if="formState.mode === 'ENUM'"
            title="下拉列表仅展示前1000条，若是搜索无数据可切换至条件过滤"
          >
            <InfoCircleOutlined style="font-size: 16px" />
          </a-tooltip>
          <a-button
            v-if="formState.mode === 'CUSTOM'"
            size="small"
            type="text"
            title="清除内容"
            :icon="h(ClearOutlined)"
            @click="clearCustom"
          ></a-button>
        </div>
      </template>

      <!-- 枚举过滤 -->
      <a-select
        v-if="formState.mode === 'ENUM'"
        placeholder="请选择"
        allow-clear
        :mode="!single ? 'multiple' : ''"
        :loading="enumLoading"
        :getPopupContainer="node => node.parentNode"
        v-model:value="formState.conditions"
      >
        <a-select-option v-for="item in enumList" :key="item">
          {{ item }}
        </a-select-option>
      </a-select>

      <!-- 条件过滤 -->
      <Conditions
        v-if="formState.mode === 'CONDITION'"
        :single="single"
        :dataType="field.dataType"
        :conditions="formState.conditions"
        v-model:relation="formState.relation"
      >
      </Conditions>

      <!-- 单条件无自定义 -->
      <a-textarea
        v-if="formState.mode === 'CUSTOM' && !single"
        placeholder="回车分隔"
        :rows="4"
        v-model:value.trim="formState.conditions"
      ></a-textarea>
    </a-form-item>

    <a-form-item
      label=" "
      style="margin: 50px 0 -10px; text-align: right"
      :colon="false"
    >
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleOk">确认</a-button>
      </a-space>
    </a-form-item>
  </a-form>

  <input
    ref="fileInputRef"
    type="file"
    hidden
    accept=".xls, .xlsx, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    @change="onFileInputChange"
  />
</template>

<script setup>
import {
  h,
  ref,
  reactive,
  computed,
  watch,
  nextTick,
  inject,
  onMounted
} from 'vue'
import { Form, message } from 'ant-design-vue'
import { ClearOutlined, InfoCircleOutlined } from '@ant-design/icons-vue'
import { CATEGORY, RELATION } from '@/CONST.dict'
import { NOT_IN, IS_NOT_NULL, IS_NULL } from '@/views/dataset/config.field'
import Conditions from './Conditions.vue'
import { getRandomKey } from 'common/utils/string'
import { postAnalysisQuery } from '@/apis/analysis'
import { postParseExcel } from '@/apis/system/excel'

const emits = defineEmits(['ok', 'cancel'])
const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({})
  },
  field: {
    type: Object,
    default: () => ({})
  },
  // 是否单条件
  single: { type: Boolean }
})

const { choosed: indexChoosed } = inject('index', {})

const scopeDisabled = computed(() => {
  if (typeof indexChoosed === 'undefined') return true

  const indexFields = indexChoosed.get(CATEGORY.INDEX) || []

  return indexFields.every(t => t.name !== props.field.name)
})

watch(scopeDisabled, disabled => {
  if (disabled) {
    formState.scope = false
  }
})

const formState = reactive({
  mode: 'ENUM',
  scope: false,
  relation: 'AND',
  conditions: [],
  exclude: false
})

// 校验过滤条件
const validateConfitions = {
  ENUM: values => {
    return values?.length > 0
  },
  CONDITION: values => {
    return values.every(v => {
      const { operator, value } = v

      return (
        operator === IS_NOT_NULL ||
        operator === IS_NULL ||
        value?.trim().length > 0
      )
    })
  },
  CUSTOM: values => {
    return values?.trim().length > 0
  }
}

const formRules = reactive({
  conditions: [
    {
      required: true,
      message: '请输入条件',
      trigger: 'blur',
      validator: (rule, value, cb) => {
        if (validateConfitions[formState.mode](value)) {
          return Promise.resolve()
        } else {
          return Promise.reject()
        }
      }
    }
  ]
})

const useForm = Form.useForm
const { clearValidate, validate, validateInfos } = useForm(formState, formRules)

// 过滤方式改变
const onModeChange = e => {
  if (e.target.value === 'CONDITION') {
    formState.conditions = [
      { _id: getRandomKey(), operator: RELATION.EQUAL, value: '' }
    ]
  } else {
    formState.conditions = undefined
  }

  nextTick(() => {
    clearValidate(['conditions'])
  })

  if (e.target.value === 'ENUM') {
    fetchEnumList()
  }
}

// 枚举值
const enumLoading = ref(false)
const enumList = ref([])
const fetchEnumList = async () => {
  try {
    enumLoading.value = true

    const {
      id: datasetId,
      config: datasetConfig,
      fields: datasetFields
    } = props.dataset
    // 从数据集中获取维度字段信息
    const dimensionField = datasetFields.find(t => t.name === props.field.name)

    const paylaod = {
      type: 'SINGLE_FIELD',
      datasetId,
      dataset: { config: datasetConfig },
      fromSource: 'temporary', // dashboard 从看板查询, report 保存图表查询, temporary 临时查询
      dimensions: [{ ...dimensionField, ...props.field }]
    }

    const { rows } = await postAnalysisQuery(paylaod)

    enumList.value = rows.map(t => t[0]).filter(t => t !== '')
  } catch (error) {
    console.error('获取枚举值错误', error)
  } finally {
    enumLoading.value = false
  }
}

// 初始化
const init = () => {
  const { filterType = 'ENUM', having, logical, conditions = [] } = props.field

  if (filterType === 'CUSTOM' && props.single) {
    formState.mode = 'ENUM'
  } else {
    formState.mode = filterType || 'ENUM'
  }
  formState.scope = having || false
  formState.relation = logical ?? 'AND'

  const conditionsRes =
    filterType === 'ENUM'
      ? conditions[0]?.args
      : filterType === 'CONDITION'
        ? conditions.map(
            ({ _id, functionalOperator = RELATION.EQUAL, args = [] }) => {
              return {
                _id,
                operator: functionalOperator,
                value: args[0]
              }
            }
          )
        : conditions[0].args?.join('\n')

  formState.conditions = props.single ? [conditionsRes[0]] : conditionsRes
  formState.exclude = conditions?.[0]?.functionalOperator === NOT_IN
}

defineExpose({ init })

onMounted(() => {
  init()
  if (formState.mode === 'ENUM') {
    fetchEnumList()
  }
})

// 上传控件
const fileInputRef = ref()
const uploadLoading = ref(false)
const upload = () => {
  if (uploadLoading.value) return

  fileInputRef.value.click()
}
const onFileInputChange = async e => {
  try {
    uploadLoading.value = true

    const form = new FormData()
    form.append('file', e.target.files[0])

    const res = await postParseExcel(form)

    if (!res.length) {
      message.warn('文件为空')
    } else {
      formState.conditions = !formState.conditions
        ? res.join('\n')
        : formState.conditions + '\n' + res.join('\n')
    }
  } catch (error) {
    console.error('上传失败', error)
  } finally {
    e.target.value = null
    uploadLoading.value = false
  }
}
const clearCustom = () => {
  formState.conditions = undefined
}

const handleOk = () => {
  validate()
    .then(res => {
      emits('ok', { ...formState })
    })
    .catch(err => {
      console.error(err)
    })
}
const handleCancel = () => {
  clearValidate()
  emits('cancel')
}
</script>

<style lang="scss" scoped>
.extra-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}
</style>
