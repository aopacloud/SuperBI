<template>
  <a-dropdown
    trigger="click"
    :disabled="disabled"
    v-model:open="modelOpen"
    @openChange="onOpenChange">
    <div class="trigger">
      <slot
        v-bind="{ value: modelValue, valueDisplay, disabled, onClear: handleClear }">
        <a-input
          readonly
          :disabled="disabled"
          :placeholder="placeholder"
          :value="valueDisplay">
          <template #suffix>
            <div class="pane-suffix">
              <DownOutlined class="pane-suffix-down" />
              <CloseCircleFilled
                class="pane-suffix-close"
                v-if="
                  modelValue.conditions?.[0].useLatestPartitionValue ||
                  modelValue.conditions?.[0].args?.length > 0
                "
                @click.stop="handleClear" />
            </div>
          </template>
        </a-input>
      </slot>
    </div>

    <template v-if="!disabled" #overlay>
      <div class="filter-wrapper">
        <FilterDate
          v-if="isDate"
          ref="datepickerRef"
          :single="single"
          :utcOffset="timeOffset"
          :moda="+(modelValue.conditions[0].timeType === 'EXACT')"
          :offset="modelValue.conditions[0].args"
          :value="modelValue.conditions[0].args"
          :hms="modelValue.conditions[0].timeParts"
          :showTime="isTime_HHMMSS(modelValue.dataType)"
          :extra="{
            dt: modelValue.conditions[0].useLatestPartitionValue,
            current: modelValue.conditions[0]._this,
            isCustom: modelValue.conditions[0]._current,
            until: modelValue.conditions[0]._until,
          }"
          @cancel="cancel"
          @ok="ok">
        </FilterDate>

        <FilterText
          v-else
          ref="filterTextRef"
          :single="single"
          :dataset="dataset"
          :field="modelValue"
          @cancel="cancel"
          @ok="ok" />
      </div>
    </template>
  </a-dropdown>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { CloseCircleFilled, DownOutlined } from '@ant-design/icons-vue'
import { RELATION } from '@/CONST.dict'
import { NOT_IN, IN, isTime_HHMMSS } from '@/views/dataset/config.field'
import FilterText from './Text.vue'
import FilterDate from 'common/components/DatePickers/PickerPanel.vue'
import { deepClone } from '@/common/utils/help'
import { displayFilter } from './utils'
import { watchEffect } from 'vue'

defineOptions({
  name: 'FilterVue',
  inheritAttrs: false,
})

const emits = defineEmits(['update:field', 'ok', 'cancel'])
const props = defineProps({
  // 是否单条件
  single: {
    type: Boolean,
  },
  // 数据集
  dataset: {
    type: Object,
    default: () => ({}),
  },
  // 值字段
  field: {
    type: Object,
    default: () => ({}),
  },
  // 时区偏移
  timeOffset: {
    type: String,
    default: '+8',
  },
  // 是否允许清除
  allowClear: {
    type: Boolean,
    default: true,
  },
  disabled: Boolean,
  placeholder: {
    type: String,
    default: '请选择',
  },
})

const modelOpen = ref(false)
const modelValue = ref({})

// 显示的值
const valueDisplay = computed(() =>
  displayFilter(modelValue.value, { timeOffset: props.timeOffset })
)

watchEffect(() => {
  modelValue.value = deepClone(props.field)
})

const datepickerRef = ref(null)
const filterTextRef = ref(null)
const onOpenChange = op => {
  if (op) {
    nextTick(() => {
      datepickerRef.value?.init()
      filterTextRef.value?.init()
    })
  }
}

const cancel = () => {
  modelOpen.value = false
}

const handleClear = () => {
  modelValue.value.conditions = [{}]
  updatePropsField()
}

const updatePropsField = () => {
  for (const k in modelValue.value) {
    props.field[k] = modelValue.value[k]
  }
}

// 日期筛选
const isDate = computed(() => modelValue.value?.dataType?.includes('TIME'))
// 过滤条件确认
const ok = e => {
  // 日期过滤
  if (isDate.value) {
    const { moda, offset, value, extra = {}, hms } = e

    modelValue.value._dtChanged = true // 日期有修改过，看板携带的日期过滤不生效
    modelValue.value.logical = RELATION.AND
    modelValue.value.conditions = [
      {
        useLatestPartitionValue: extra.dt || undefined,
        functionalOperator: 'BETWEEN',
        timeType: moda === 0 ? 'RELATIVE' : 'EXACT', // 时间字段的筛选类型， EXACT 精确时间， RELATIVE 相对时间
        args: moda === 0 ? offset : value,
        timeParts: hms,
        _this: extra.current,
        _current: extra.isCustom,
        _until: extra.until,
      },
    ]
  } else {
    const { conditions, exclude, relation, mode, scope } = e

    modelValue.value.filterType = mode
    // 条件过滤
    if (mode === 'CONDITION') {
      modelValue.value.logical =
        relation === RELATION.OR ? RELATION.OR : RELATION.AND
      modelValue.value.having = scope
      modelValue.value.conditions = conditions.map(t => {
        const { operator, value } = t

        return {
          _id: t._id,
          functionalOperator: operator,
          args: [value],
        }
      })
    } else {
      modelValue.value.logical = RELATION.AND
      modelValue.value.conditions = [
        {
          functionalOperator: exclude ? NOT_IN : IN,
          args: Array.isArray(conditions)
            ? conditions
            : conditions
                ?.split(/\n/)
                .map(t => t.trim())
                .filter(Boolean) ?? [],
        },
      ]
    }
  }

  updatePropsField()

  modelOpen.value = false
  emits('ok', modelValue.value)
}
</script>

<style lang="scss" scoped>
.filter-wrapper {
  background-color: #ffffff;
  background-clip: padding-box;
  border-radius: 8px;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12),
    0 9px 28px 8px rgba(0, 0, 0, 0.05);
  padding: 12px;

  & > .custom-picker-section {
    padding: 0;
    background: #fff;
    border-radius: 0;
    box-shadow: none;
  }
}
.trigger {
  flex: 1;
}
.pane-suffix {
  position: relative;
  display: inline-flex;
  align-items: center;
  color: rgba(0, 0, 0, 0.25);
  font-size: 12px;
  &-close {
    position: absolute;
    cursor: pointer;
    opacity: 0;
    transition: all 0.2s;
  }
}
.trigger:hover {
  .pane-suffix-down:not(:only-child) {
    visibility: hidden;
  }
  .pane-suffix-close {
    opacity: 1;
  }
}
</style>
