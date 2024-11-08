<template>
  <Resize
    class="filter-render-resize"
    :disabled="!resizable"
    :directions="['right']"
    :style="{ width: sizeWidth }"
    :minWidth="
      isLonger ? 320 : isTextOrIsNumber ? 160 : isDatepicker ? 240 : 80
    "
    @resizing="onResizing"
  >
    <div class="filter-render">
      <a-select
        v-if="type === 'ENUM'"
        placeholder="请选择默认值"
        allow-clear
        showArrow
        style="width: 100%"
        :loading="enumLoading"
        :mode="item.single ? '-' : 'multiple'"
        :size="fieldSize"
        :options="
          item.enumResourceType === 'MANUAL' ? item.enumList_input : enumList
        "
        v-model:value="modelValue"
      />

      <DatePicker
        v-if="type === 'TIME'"
        class="filter-render-datepicker"
        :showTime="dateShowMMSS"
        :modeOnly="from === 'setting' ? undefined : 1"
        :utcOffset="timeOffset"
        :options="{ dt: false }"
        v-model:extra="modelValue.extra"
        v-model:moda="modelValue.mode"
        v-model:offset="modelValue.offset"
        v-model:value="modelValue.date"
        v-model:hms="modelValue.hms"
      >
        <a-input
          readonly
          placeholder="请选择默认日期"
          style="width: 100%"
          :size="fieldSize"
          :value="displayDate(modelValue)"
        >
          <template #prefix>
            <CalendarOutlined style="margin-right: 2px" />
          </template>

          <template #suffix>
            <CloseOutlined
              v-if="modelValue.date && modelValue.date.length"
              style="
                font-size: 10px;
                color: rgba(0, 0, 0, 0.45);
                cursor: pointer;
              "
              @click.stop="removeDate"
            />
          </template>
        </a-input>
      </DatePicker>

      <template v-if="type === 'TEXT' || type === 'NUMBER'">
        <div class="flex align-center">
          <a-space-compact style="flex: 1">
            <a-select
              style="width: 85px"
              class="input-select"
              :size="fieldSize"
              :options="operators"
              v-model:value="modelValue[0]['operator']"
              @change="e => onOperatorChange(e, modelValue[0])"
            />
            <a-input
              style="flex: 1"
              placeholder="请输入默认值"
              :disabled="isOperatorNullOrNot(modelValue[0]['operator'])"
              allow-clear
              :size="fieldSize"
              v-model:value="modelValue[0]['value']"
            />
          </a-space-compact>

          <template v-if="method === RELATION.OR || method === RELATION.AND">
            <span style="margin: 0 10px">{{ filterMethodLabel }}</span>

            <a-space-compact style="flex: 1">
              <a-select
                style="width: 85px"
                class="input-select"
                :size="fieldSize"
                :options="operators"
                v-model:value="modelValue[1]['operator']"
                @change="e => onOperatorChange(e, modelValue[1])"
              />
              <a-input
                style="flex: 1"
                placeholder="请输入默认值"
                :disabled="isOperatorNullOrNot(modelValue[1]['operator'])"
                :size="fieldSize"
                v-model:value="modelValue[1]['value']"
              />
            </a-space-compact>
          </template>
        </div>
      </template>

      <TagsInput
        v-if="type === 'CUSTOM'"
        placeholder="回车/英文逗号分割（粘贴时英文逗号,分割）"
        style="width: 100%"
        :size="fieldSize"
        v-model:value="modelValue"
      />
    </div>
  </Resize>
</template>

<script setup>
import { ref, computed, watch, inject, watchEffect } from 'vue'
import { Form } from 'ant-design-vue'
import { CalendarOutlined, CloseOutlined } from '@ant-design/icons-vue'
import { RELATION } from '@/CONST.dict'
import { operatorMap, IS_NULL, IS_NOT_NULL } from '@/views/dataset/config.field'
import DatePicker from 'common/components/DatePickers/index.vue'
import TagsInput from 'common/components/TagsInput/index.vue'
import { postDashboardFilter } from '@/apis/analysis'
import { displayDateFormat } from 'common/components/DatePickers/utils'
import { isTime_HHMMSS } from '@/views/dataset/config.field'
import Resize from 'common/components/Layout/Resize/index.vue'

const emits = defineEmits(['update:value', 'sizeChange'])
const props = defineProps({
  dId: [String, Number], // 看板ID
  // 布局
  layout: {
    type: String,
    default: 'inline',
    validator: str => {
      return str === 'inline' || str === 'block'
    }
  },
  // 筛选项
  item: {
    type: Object,
    default: () => ({})
  },
  // 输入框尺寸
  fieldSize: {
    type: String,
    default: 'default'
  },
  // 出发更新 prop.item
  value: {
    type: [String, Object, Array, Number]
  },
  from: {
    type: String,
    default: 'setting',
    validator: str => ['setting', 'filterItem'].includes(str)
  },
  size: {
    type: Object,
    default: () => ({})
  },
  resizable: {
    type: Boolean
  }
})

// 时区偏移
const {
  timeOffset,
  layout: { get: getLayout }
} = inject('index')

// 筛选类型
const type = computed(() => {
  return props.item.filterType
})

const isTextOrIsNumber = computed(
  () => type.value === 'TEXT' || type.value === 'NUMBER'
)

const isDatepicker = computed(() => type.value === 'TIME')

const isEnum = computed(() => type.value === 'ENUM')

const isLonger = computed(() => {
  return (
    isTextOrIsNumber.value &&
    (method.value === RELATION.OR || method.value === RELATION.AND)
  )
})

const sizeWidth = computed(() => {
  if (props.size.width) return props.size.width + 'px'

  if (isDatepicker.value) return dateShowMMSS.value ? '350px' : '240px'

  if (isEnum.value) return '230px'

  if (isTextOrIsNumber.value) return isLonger.value ? '480px' : '240px'

  return '240px'
})

const modelValue = ref({})

// 枚举值
const enumLoading = ref(false)
const enumList = ref([])
const fetchEnumList = async () => {
  try {
    enumLoading.value = true
    const { rows = [] } = await postDashboardFilter(
      Object.values(props.item.charts)
    )

    enumList.value = rows.map(t => {
      return {
        label: t[0],
        value: t[0]
      }
    })
  } catch (error) {
    console.error('获取看板筛选项枚举值失败', error)
  } finally {
    enumLoading.value = false
  }
}

// 默认值初始化
const init = () => {
  const { value, filterMethod, filterType } = props.item

  if (filterType === 'ENUM') {
    modelValue.value = value
  } else if (filterType === 'TIME') {
    modelValue.value = value ?? {}
  } else if (filterType === 'TEXT' || filterType === 'NUMBER') {
    const item = { operator: RELATION.EQUAL, value: '' }

    if (value?.length) {
      modelValue.value = value
    } else {
      if (filterMethod === RELATION.OR || filterMethod === RELATION.AND) {
        modelValue.value = [{ ...item }, { ...item }]
      } else {
        modelValue.value = [{ ...item }]
      }
    }
  } else {
    modelValue.value = value
  }
}

watch(
  () => props.item.filterType,
  type => {
    if (type !== 'ENUM') return

    if (props.item.enumResourceType === 'MANUAL') return
    if (props.from === 'setting' && !props.item.setDefault) return

    fetchEnumList()
  },
  { immediate: true }
)

watchEffect(init)

// 触发外层的表单校验
const formItemContext = Form.useInjectFormItemContext()

// 监听value, 更新并校验
watch(
  () => modelValue.value,
  val => {
    emits('update:value', val)
    formItemContext.onFieldChange()
  },
  { deep: true }
)

// 筛选方式
const method = computed(() => {
  return props.item.filterMethod
})

// 文本\数值 操作符
const operators = computed(() => {
  if (type.value !== 'TEXT' && type.value !== 'NUMBER') return []

  const list = operatorMap[type.value]
  return Object.keys(list).map(key => {
    return {
      label: list[key],
      value: key
    }
  })
})

const onOperatorChange = (e, itemValue) => {
  if (e === IS_NULL || e == IS_NOT_NULL) {
    itemValue['value'] = ''
  }
}
const isOperatorNullOrNot = e => e === IS_NULL || e === IS_NOT_NULL

// 筛选方式文本
const filterMethodLabel = computed(() => {
  return method.value === RELATION.OR
    ? '或'
    : method.value === RELATION.AND
      ? '且'
      : ''
})

const removeDate = () => {
  modelValue.value.date = []
  modelValue.value.offset = []
  modelValue.value.hms = undefined
  if (modelValue.value.extra) {
    modelValue.value.extra.until = undefined
  }
}
const displayDate = ({ date = [], mode, extra = {}, offset = [], hms }) => {
  if (extra.dt) return '有数的一天'

  return displayDateFormat({
    mode,
    offset,
    hms,
    date,
    extra,
    format: 'YYYY/MM/DD'
  }).join(' ~ ')
}

const layoutCharts = computed(() =>
  getLayout().filter(t => t.type === 'REPORT')
)

const dateShowMMSS = computed(() => {
  const charts = props.item.charts || {}
  const entries = Object.entries(charts)
  if (entries.length === 0) return false

  const fields = entries.map(([key, value]) => {
    const chart = layoutCharts.value.find(c => c.reportId === +key) || {}
    const fields = chart.content?.report?.dataset?.fields || []
    const field = fields.find(f => f.name === value.fieldName)
    return {
      ...value,
      ...field
    }
  })

  return fields.some(t => isTime_HHMMSS(t.dataType))
})

const onResizing = e => {
  emits('sizeChange', e)
}
</script>

<style lang="scss" scoped>
.filter-render {
  :deep(.filter-render-datepicker) {
    width: 100%;
  }
}
.input-select {
  :deep(.ant-select-selector) {
    background-color: rgba(0, 0, 0, 0.02);
  }
}
.filter-render-resize {
  border-color: transparent !important;
}
</style>
