<template>
  <a-modal
    :open="open"
    :title="title"
    :width="1000"
    :confirm-loading="confirmLoading"
    :maskClosable="false"
    :keyboard="false"
    @cancel="cancel"
    @ok="ok"
  >
    <a-form
      class="field-insert-form"
      :label-col="{ style: { width: '120px' } }"
    >
      <a-form-item label="字段名称" v-bind="validateInfos.name">
        <a-input
          placeholder="名称只支持字母、下划线、数字的组合，最长50个字符"
          :disabled="initData._mode === 'edit'"
          v-model:value="formState.name"
        />
      </a-form-item>
      <a-form-item label="显示名称" v-bind="validateInfos.displayName">
        <a-input
          placeholder="最长50个字符"
          v-model:value="formState.displayName"
        />
      </a-form-item>
      <a-form-item label="字段逻辑" v-bind="validateInfos.expression">
        <div class="expression flex" style="height: 412px">
          <SelectList
            class="fields-select-list"
            style="width: 200px"
            keyField="name"
            labelField="displayName"
            :bordered="false"
            :multiple="false"
            :data-source="fields"
            :filterOption="
              (s, t) => t.name?.includes(s) || t.displayName?.includes(s)
            "
          >
            <template #prefix>
              <div style="padding: 2px 10px; color: #bbb">点击引用字段</div>
            </template>

            <template #item="{ item }">
              <div
                class="select-item disabled"
                v-if="
                  item.id === CATEGORY.PROPERTY || item.id === CATEGORY.INDEX
                "
              >
                <b style="font-size: 16px">{{ item.name }}</b>
              </div>

              <a-tooltip v-else placement="right" :title="item.name">
                <div class="select-item flex" @click="insertField(item)">
                  <span
                    v-if="multipleTable"
                    class="table-alias"
                    :class="{ empty: !item.tableAlias }"
                  >
                    {{ item.tableAlias }}
                  </span>
                  <i
                    :class="[
                      'iconfont',
                      getIconByFieldType(item.dataType)['icon']
                    ]"
                    :style="{
                      marginRight: '8px',
                      color: getIconByFieldType(item.dataType)['color']
                    }"
                  ></i>
                  <div class="flex-1 ellipsis">
                    {{ item.displayName }}
                  </div>
                </div>
              </a-tooltip>
            </template>
          </SelectList>
          <SelectList
            style="width: 200px; border-left: 1px solid #e8e8e8"
            keyField="name"
            labelField="expression"
            :bordered="false"
            :multiple="false"
            :data-source="functions"
          >
            <template #prefix>
              <div style="padding: 2px 10px; color: #bbb">点击引用函数</div>
            </template>
            <template #item="{ item }">
              <div class="select-item flex" @click="insertFunc(item)">
                <div class="flex-1 ellipsis">{{ item.name }}</div>
              </div>
            </template>
          </SelectList>

          <ExpressTextarea
            ref="expressTextarea"
            style="
              flex: 1;
              font-size: 14px;
              border: none;
              border-left: 1px solid #e8e8e8;
            "
            placeholder="请输入字段逻辑"
            v-model:value="formState.expression"
          />
        </div>
      </a-form-item>
      <a-form-item label="数据类型">
        <a-radio-group
          :options="dataTypeOptions"
          v-model:value="formState.dataType"
          @change="onDataTypeChange"
        />
      </a-form-item>
      <a-form-item label="字段类型">
        <a-radio-group
          :options="categoryOptions"
          v-model:value="formState.category"
        />
      </a-form-item>
      <a-form-item
        label="数据格式"
        v-if="
          formState.category === CATEGORY.INDEX &&
          formState.dataType === 'NUMBER'
        "
      >
        <a-select
          style="width: 180px"
          placeholder="请选择"
          :value="formState.dataFormat"
          @change="onDataFormatChange"
        >
          <a-select-option
            v-for="item in formatterOptions"
            :key="item.value"
            @click="onFormatterOptionClick(item)"
          >
            {{
              item.value === FORMAT_CUSTOM_CODE
                ? formState.customFormatterLabel || item.label
                : item.label
            }}
          </a-select-option>
        </a-select>
        <CustomFormatter
          style="position: absolute; bottom: 100%"
          v-model:open="dataFormatOpen"
          :value="JSON.parse(formState.customFormatConfig || '{}')"
          @ok="onFormatOk"
        />
      </a-form-item>
      <a-form-item
        label="字段计算"
        v-if="formState.category === CATEGORY.INDEX"
      >
        <FieldCalculation
          style="width: 180px"
          placeholder="请选择"
          v-model:value="formState.computeExpression"
        />
      </a-form-item>
      <a-form-item label="字段说明">
        <a-input
          placeholder="请输入字段名"
          v-model:value="formState.description"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="jsx">
import { ref, computed, watch, reactive, shallowRef, toRaw, inject } from 'vue'
import { Form, message, Tooltip } from 'ant-design-vue'
import { InfoCircleOutlined } from '@ant-design/icons-vue'
import {
  formatterOptions,
  dataTypeOptions,
  categoryOptions,
  FORMAT_DEFAULT_CODE,
  FORMAT_CUSTOM_CODE,
  SUMMARY_DEFAULT,
  SUMMARY_PROPERTY_DEFAULT,
  SUMMARY_INDEX_DEFAULT
} from '@/views/dataset/config.field'
import {
  getIconByFieldType,
  displayCustomFormatterLabel
} from '@/views/dataset/utils'
import FieldCalculation from './FieldCalculation.vue'
import CustomFormatter from '@/components/CustomFormatter/index.vue'
import SelectList from 'common/components/ExtendSelect'
import ExpressTextarea from '@/components/ExpressTextarea/index.vue'
import { CATEGORY } from '@/CONST.dict.js'
import { getFunctionList } from '@/apis/function'
import { validateDatasetField } from '@/apis/dataset'
import ErrorSuggestion from '@/components/ErrorSuggestion/index.jsx'
import useError from '@/hooks/useError'
import { getRandomKey } from 'common/utils/string'

const { show } = ErrorSuggestion()

const { fetchReason, reason: errorReason, reasonLoading } = useError()

const { getDatasetConfig } = inject('index')

const emits = defineEmits(['update:open', 'ok', 'cancel'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  dataset: {
    type: Object,
    default: () => ({})
  },
  initData: {
    type: Object,
    default: () => ({})
  },
  fields: {
    type: Array,
    default: () => []
  },
  multipleTable: Boolean // 是否多表
})

const functions = shallowRef([])
const fetchFunctions = async () => {
  try {
    const res = await getFunctionList()

    functions.value = res
  } catch (error) {
    console.error('获取函数错误', error)
  }
}

const title = computed(() => {
  return props.initData._mode === 'edit' ? '编辑字段' : '新增字段'
})

watch(
  () => props.open,
  visible => {
    if (visible) {
      if (!functions.value.length) {
        fetchFunctions()
      }
      init()
    } else {
      resetFields()
    }
  }
)

// 初始化
const init = () => {
  if (props.initData._mode === 'insert') return

  for (const key in toRaw(formState)) {
    const val = props.initData[key]

    if (key === 'dataType') {
      formState[key] = Array.isArray(val)
        ? val[0]
        : val.includes('TIME')
          ? 'TIME'
          : val
    } else {
      formState[key] = val ?? undefined
    }
  }
}

const formState = reactive({
  name: '',
  displayName: '',
  expression: undefined,
  dataType: 'TEXT',
  category: CATEGORY.PROPERTY,
  dataFormat: FORMAT_DEFAULT_CODE,
  computeExpression: '',
  description: '',
  type: 'ADD'
})
const nameValidator = (rule, value) => {
  const names = props.fields
    .map(item => item.name)
    .filter(t => t !== props.initData.name)

  if (names.includes(value)) {
    return Promise.reject(rule.message)
  } else {
    return Promise.resolve()
  }
}
const formRules = reactive({
  name: [
    { required: true, message: '字段名不能为空' },
    {
      pattern: /^[0-9a-zA-Z_]{1,}$/,
      message: '字段名只能包含数字、字母、下划线'
    },
    { max: 50, message: '字段名长度不能超过50个字符' },
    { validator: nameValidator, message: '字段名不能和现有字段名重复' }
  ],
  displayName: [
    { required: true, message: '显示名称不能为空' },
    { max: 50, message: '字段显示名不能超过50个字符' }
  ],
  expression: [{ required: true, message: '字段逻辑不能为空' }]
})
const { resetFields, validate, validateInfos } = Form.useForm(
  formState,
  formRules
)

const cancel = () => {
  resetFields()

  emits('update:open', false)
}

/**
 * 验证表达式
 * @param str 表达式字符串
 * @returns 如果表达式合法则返回true，否则返回false
 */
const reg = /\[([^\[\]]+)\]/g
const validateExpression = str => {
  let matchesNames = str.match(reg)

  if (!matchesNames) return true

  matchesNames = matchesNames.map(t => t.replace(/[\[\]]/g, ''))

  if (
    matchesNames.some(name => {
      const sameItems = props.fields.filter(
        t => t.sourceFieldName === name && t.name !== name
      )
      if (sameItems.length) {
        const sameNames = sameItems.map(t => '[' + t.name + ']').join('、')

        message.warn(
          `由于数据集中有多个[${name}]，已自动重命名为${sameNames}，请输入重命名后的字段名`
        )
        return true
      }
    })
  ) {
    return false
  }

  return true
}

const confirmLoading = ref(false)
const ok = () => {
  validate().then(async res => {
    const payload = {
      ...props.initData,
      ...toRaw(formState)
    }

    if (props.multipleTable && !validateExpression(payload.expression)) {
      return
    }

    const hasAggregation = await validateField()

    payload.parentName =
      payload.category === CATEGORY.PROPERTY ? '维度' : '指标'

    // 更新字段类型的格式
    if (payload.dataType === 'TIME') {
      if (hasDataTypeChanged) {
        payload.dataType = [payload.dataType]
      } else {
        payload.dataType = props.initData.dataType
      }
    }

    payload.description = payload.description || payload.displayName

    if (hasAggregation) {
      payload.aggregator = SUMMARY_DEFAULT
    } else if (!props.initData.aggregator) {
      payload.aggregator =
        payload.category === CATEGORY.PROPERTY
          ? SUMMARY_PROPERTY_DEFAULT
          : SUMMARY_INDEX_DEFAULT
    } else {
      payload.aggregator = props.initData.aggregator
    }

    emits('ok', payload)
    setTimeout(() => {
      cancel()
    }, 10)
  })
}

// 字段校验
const validateField = async () => {
  try {
    confirmLoading.value = true

    const { fields } = props
    const payloadFields = fields
      .filter(t => t.id !== CATEGORY.PROPERTY && t.id !== CATEGORY.INDEX)
      .map(t => {
        return {
          ...t,
          dataType: Array.isArray(t.dataType)
            ? t.dataType.filter(Boolean).join('_')
            : t.dataType
        }
      })

    const payload = {
      workspaceId: props.dataset.workspaceId,
      dataset: {
        ...props.dataset,
        config: getDatasetConfig(),
        fields: payloadFields
      },
      expression: formState.expression
    }

    // pass 校验通过 hasAggregation 是否有聚合
    const {
      pass,
      msg,
      hasAggregation,
      queryId: qId
    } = await validateDatasetField(payload)

    queryId.value = qId

    if (pass) {
      return Promise.resolve(hasAggregation)
    } else {
      let chMsg = msg.match(/^\[(.*?)\]/)
      chMsg = chMsg ? chMsg[1] || msg : msg

      fetchReason(qId).then(() => {
        setTimeout(() => {
          message.destroy(errorMsgKey)
        }, 5000)
      })

      const tooltip = qId ? (
        <Tooltip title='点击查询详细信息'>
          <InfoCircleOutlined
            class='btn-icon'
            style='margin-left: 4px;vertical-align: -3px;color: #1677ff'
            onclick={showSuggestion}
          />
        </Tooltip>
      ) : null

      message.error({
        key: errorMsgKey,
        duration: !qId ? 3 : 0,
        class: 'ai-message',
        content: () => (
          <span>
            <span>{chMsg}</span>

            {reasonLoading.value ? (
              <ASpin size='small' style='margin-left: 6px'></ASpin>
            ) : !errorReason.value ? (
              tooltip
            ) : null}

            {errorReason.value ? (
              <div style='margin: 4px 0 0 24px;'>
                AI分析: {errorReason.value}
                {tooltip}
              </div>
            ) : null}
          </span>
        )
      })

      return Promise.reject()
    }
  } catch (error) {
    console.error('数据集字段校验错误', error)

    return Promise.reject()
  } finally {
    confirmLoading.value = false
  }
}

const onDataFormatChange = e => {
  if (e !== FORMAT_CUSTOM_CODE) {
    formState.dataFormat = e
    formState.customFormatConfig = '{}'
    formState.customFormatterLabel = ''
  }
}
// 自定义格式
const dataFormatOpen = ref(false)
const onFormatterOptionClick = ({ label, value }) => {
  if (value === FORMAT_CUSTOM_CODE) dataFormatOpen.value = true
}
const onFormatOk = e => {
  formState.dataFormat = FORMAT_CUSTOM_CODE
  formState.customFormatConfig = JSON.stringify(e)
  formState.customFormatterLabel = displayCustomFormatterLabel(e)
}

// 字段逻辑
const expressTextarea = ref(null)
const insertField = item => {
  expressTextarea.value.insert('[' + item.name + ']')
}
const insertFunc = item => {
  expressTextarea.value.insert(item.name + '()', [item.name.length + 1])
}

// 字段类型是否改变
let hasDataTypeChanged = false
// 字段类型改变
const onDataTypeChange = e => {
  const { value } = e.target

  if (value === 'NUMBER') {
    formState.dataFormat = FORMAT_DEFAULT_CODE
  }

  hasDataTypeChanged = true
}

// 错误信息key
const errorMsgKey = getRandomKey()
// 查询ID
const queryId = ref('')

const showSuggestion = () => {
  show(queryId.value)
}
</script>

<style lang="scss" scoped>
.fields-select-list {
  :deep(.pane-item):has(> .disabled):hover {
    background-color: transparent;
    cursor: initial;
  }
}
.select-item {
  padding: 0 12px;
}
.field-insert-form {
  .expression {
    border: 1px solid #d9d9d9;
    border-radius: 4px;
  }
  :deep(.ant-form-item-has-error) {
    .expression {
      border-color: #ff4d4f;
    }
  }
}
</style>

<style lang="scss">
.ai-message {
  .ant-message-notice-content {
    text-align: initial;
  }
}
</style>
