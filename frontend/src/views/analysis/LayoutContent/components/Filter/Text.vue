<template>
  <a-form
    style="width: 500px; margin-top: 20px"
    :label-col="{ span: 6 }"
    :wrapper-col="{ span: 17 }">
    <a-form-item label="过滤方式">
      <a-radio-group v-model:value="formState.mode" @change="onModeChange">
        <a-radio value="ENUM">枚举过滤</a-radio>
        <a-radio value="CONDITION">条件过滤</a-radio>
        <a-radio value="CUSTOM">自定义</a-radio>
      </a-radio-group>
    </a-form-item>

    <a-form-item label="过滤范围" v-if="formState.mode === 'CONDITION'">
      <a-radio-group v-model:value="formState.scope">
        <a-radio :disabled="scopeDisabled" :value="true">结果过滤</a-radio>
        <a-radio :value="false">明细过滤</a-radio>
      </a-radio-group>
    </a-form-item>

    <a-form-item label="过滤条件" v-bind="validateInfos.conditions">
      <template #extra v-if="formState.mode !== 'CONDITION'">
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
          <a-button
            v-if="formState.mode === 'CUSTOM'"
            size="small"
            type="text"
            title="清除内容"
            :icon="h(ClearOutlined)"
            @click="clearCustom"></a-button>
        </div>
      </template>

      <a-select
        v-if="formState.mode === 'ENUM'"
        mode="multiple"
        placeholder="请选择"
        allow-clear
        :loading="enumLoading"
        v-model:value="formState.conditions">
        <a-select-option v-for="item in enumList" :key="item">{{ item }}</a-select-option>
      </a-select>

      <Conditions
        v-else-if="formState.mode === 'CONDITION'"
        :dataType="field.dataType"
        :conditions="formState.conditions"
        v-model:relation="formState.relation">
      </Conditions>

      <a-textarea
        v-else
        placeholder="回车分隔"
        :rows="4"
        v-model:value="formState.conditions"></a-textarea>
    </a-form-item>

    <a-form-item label=" " style="margin: 50px 0 -10px; text-align: right" :colon="false">
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
    @change="onFileInputChange" />
</template>

<script setup>
import { h, ref, reactive, computed, watch, nextTick, inject, onMounted } from 'vue'
import { Form, message } from 'ant-design-vue'
import { ClearOutlined } from '@ant-design/icons-vue'
import { CATEGORY, RELATION } from '@/CONST.dict'
import { NOT_IN } from '@/views/dataset/config.field'
import Conditions from './Conditions.vue'
import { getRandomKey } from 'common/utils/help'
import { postAnalysisQuery } from '@/apis/analysis'
import { postParseExcel } from '@/apis/system/excel'

const emits = defineEmits(['ok', 'cancel'])
const props = defineProps({
  field: {
    type: Object,
    default: () => ({}),
  },
})

const indexInject = inject('index')

const scopeDisabled = computed(() => {
  const indexChoosed = indexInject.choosed.get(CATEGORY.INDEX)

  return indexChoosed.every(t => t.name !== props.field.name)
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
  exclude: false,
})

// 校验过滤条件
const validateConfitions = {
  ENUM: values => {
    return values?.length > 0
  },
  CONDITION: values => {
    return values.some(v => {
      const { operator, value } = v

      return (
        operator === 'IS_NOT_NULL' || operator === 'IS_NULL' || value?.trim().length > 0
      )
    })
  },
  CUSTOM: values => {
    return values?.trim().length > 0
  },
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
      },
    },
  ],
})

const useForm = Form.useForm
const { resetFields, clearValidate, validate, validateInfos } = useForm(
  formState,
  formRules
)

// 过滤方式改变
const onModeChange = e => {
  if (e.target.value === 'CONDITION') {
    formState.conditions = [{ _id: getRandomKey(), operator: RELATION.EQUAL, value: '' }]
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

    const dataset = indexInject.dataset.get()
    const paylaod = {
      type: 'SINGLE_FIELD',
      datasetId: dataset.id,
      fromSource: 'temporary', // dashboard 从看板查询, report 保存图表查询, temporary 临时查询
      dimensions: [{ ...props.field }],
    }

    const { rows } = await postAnalysisQuery(paylaod)

    enumList.value = rows.map(t => t[0]).filter(Boolean)
  } catch (error) {
    console.error('获取枚举值错误', error)
  } finally {
    enumLoading.value = false
  }
}

// 初始化
const init = () => {
  const { filterType = 'ENUM', having, logical, conditions = [] } = props.field

  formState.mode = filterType || 'ENUM'
  formState.scope = having || false
  formState.relation = logical ?? 'AND'

  formState.conditions =
    filterType === 'ENUM'
      ? conditions[0]?.args
      : filterType === 'CONDITION'
      ? conditions.map(({ _id, functionalOperator, args }) => {
          return {
            _id,
            operator: functionalOperator,
            value: args[0],
          }
        })
      : conditions[0].args?.join('\n')
  formState.exclude = conditions[0]?.functionalOperator === NOT_IN
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
  resetFields()
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
