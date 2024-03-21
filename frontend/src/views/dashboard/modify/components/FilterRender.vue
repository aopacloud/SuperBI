<template>
  <div>
    <a-select
      v-if="type === 'ENUM'"
      placeholder="请选择默认值"
      allow-clear
      showArrow
      :loading="enumLoading"
      :mode="item.single ? '-' : 'multiple'"
      :style="{ width: layout === 'inline' ? '230px' : '' }"
      :size="size"
      :options="item.enumResourceType === 'MANUAL' ? item.enumList_input : enumList"
      v-model:value="modelValue" />

    <DatePicker
      v-if="type === 'TIME'"
      :modeOnly="from === 'setting' ? undefined : 1"
      :utcOffset="timeOffset"
      :options="{ dt: false }"
      v-model:moda="modelValue.mode"
      v-model:offset="modelValue.offset"
      v-model:value="modelValue.date"
      v-model:extra="modelValue.extra">
      <a-input
        readonly
        placeholder="请选择默认日期"
        :size="size"
        :style="{ width: layout === 'inline' ? '230px' : '' }"
        :value="displayDate(modelValue)">
        <template #prefix>
          <CalendarOutlined style="margin-right: 2px" />
        </template>

        <template #suffix>
          <CloseOutlined
            v-if="modelValue.date && modelValue.date.length"
            style="font-size: 10px; color: rgba(0, 0, 0, 0.45); cursor: pointer"
            @click="removeDate" />
        </template>
      </a-input>
    </DatePicker>

    <template v-if="type === 'TEXT' || type === 'NUMBER'">
      <div class="flex-1 flex align-center">
        <a-input
          placeholder="请输入默认值"
          allow-clear
          :size="size"
          :style="{ width: layout === 'inline' ? '240px' : '' }"
          v-model:value="modelValue[0]['value']">
          <template #addonBefore>
            <a-select
              style="width: 85px"
              class="input-select"
              :size="size"
              :options="operators"
              v-model:value="modelValue[0]['operator']">
            </a-select>
          </template>
        </a-input>

        <template v-if="method === RELATION.OR || method === RELATION.AND">
          <span style="margin: 0 10px">{{ filterMethodLabel }}</span>
          <a-input
            :style="{ width: layout === 'inline' ? '240px' : '' }"
            placeholder="请输入默认值"
            :size="size"
            v-model:value="modelValue[1]['value']">
            <template #addonBefore>
              <a-select
                style="width: 85px"
                class="input-select"
                :size="size"
                :options="operators"
                v-model:value="modelValue[1]['operator']">
              </a-select>
            </template>
          </a-input>
        </template>
      </div>
    </template>

    <TagsInput
      v-if="type === 'CUSTOM'"
      placeholder="回车/英文逗号分割（粘贴时英文逗号,分割）"
      style="width: 230px"
      :size="size"
      v-model:value="modelValue" />
  </div>
</template>

<script setup>
import { ref, computed, watch, inject, watchEffect } from 'vue'
import { Form } from 'ant-design-vue'
import { CalendarOutlined, CloseOutlined } from '@ant-design/icons-vue'
import { RELATION } from '@/CONST.dict'
import { operatorMap } from '@/views/dataset/config.field'
import DatePicker from 'common/components/DatePickers/index.vue'
import TagsInput from 'common/components/TagsInput/index.vue'
import { postDashboardFilter } from '@/apis/analysis'
import { displayDateFormat } from 'common/components/DatePickers/utils'

const emits = defineEmits(['update:value'])
const props = defineProps({
  dId: [String, Number], // 看板ID
  // 布局
  layout: {
    type: String,
    default: 'inline',
    validator: str => {
      return str === 'inline' || str === 'block'
    },
  },
  // 筛选项
  item: {
    type: Object,
    default: () => ({}),
  },
  // 输入框尺寸
  size: {
    type: String,
    default: 'default',
  },
  // 出发更新 prop.item
  value: {
    type: [String, Object, Array, Number],
  },
  from: {
    type: String,
    default: 'setting',
    validator: str => ['setting', 'filterItem'].includes(str),
  },
})

// 时区偏移
const { timeOffset } = inject('index')

// 筛选类型
const type = computed(() => {
  return props.item.filterType
})

const modelValue = ref('')

// 枚举值
const enumLoading = ref(false)
const enumList = ref([])
const fetchEnumList = async () => {
  try {
    enumLoading.value = true
    const { rows = [] } = await postDashboardFilter(Object.values(props.item.charts))

    enumList.value = rows.map(t => {
      return {
        label: t[0],
        value: t[0],
      }
    })
  } catch (error) {
    console.error('获取看板筛选项枚举值失败', error)
  } finally {
    enumLoading.value = false
  }
}

// 默认值初始化
const init = type => {
  const { value, filterMethod } = props.item

  if (type === 'ENUM') {
    modelValue.value = value
  } else if (type === 'TIME') {
    modelValue.value = value ?? {}
  } else if (type === 'TEXT' || type === 'NUMBER') {
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

watchEffect(() => {
  init(props.item.filterType)
})

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
      value: key,
    }
  })
})
// 筛选方式文本
const filterMethodLabel = computed(() => {
  return method.value === RELATION.OR ? '或' : method.value === RELATION.AND ? '且' : ''
})

const removeDate = () => {
  props.item.value.date = []
  props.item.value.offset = []
}
const displayDate = ({ date = [], mode, extra = {}, offset = [] }) => {
  if (extra.dt) return '有数的一天'

  return displayDateFormat({
    mode,
    offset,
    date,
    format: 'YYYY/MM/DD',
  }).join(' ~ ')
}
</script>

<style lang="scss" scoped>
.input-select {
  :deep(.ant-select-selection-item) {
    color: rgba(0, 0, 0, 0.8);
  }
}
</style>
