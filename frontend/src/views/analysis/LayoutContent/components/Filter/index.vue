<template>
  <div class="filter-wrapper">
    <FilterDate
      v-if="isDate"
      ref="datepickerRef"
      :single="single"
      :utcOffset="timeOffset"
      :moda="+(field.conditions[0].timeType === 'EXACT')"
      :offset="field.conditions[0].args"
      :value="field.conditions[0].args"
      :hms="field.conditions[0].timeParts"
      :showTime="isTime_HHMMSS(field.dataType)"
      :extra="{
        dt: field.conditions[0].useLatestPartitionValue,
        current: field.conditions[0]._this,
        isCustom: field.conditions[0]._current,
        until: field.conditions[0]._until,
      }"
      @cancel="cancel"
      @ok="ok">
    </FilterDate>

    <FilterText
      v-else
      ref="filterTextRef"
      :single="single"
      :dataset="dataset"
      :field="field"
      @cancel="cancel"
      @ok="ok" />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, inject } from 'vue'
import { FilterOutlined } from '@ant-design/icons-vue'
import { RELATION } from '@/CONST.dict'
import { NOT_IN, IN, isTime_HHMMSS } from '@/views/dataset/config.field'
import FilterText from './Text.vue'
import FilterDate from 'common/components/DatePickers/PickerPanel.vue'

defineOptions({
  name: 'FilterVue',
  inheritAttrs: false,
})

const emits = defineEmits(['update:open', 'ok', 'cancel'])
const props = defineProps({
  // 面板的开启状态
  open: { type: Boolean },
  // 是否单条件
  single: { type: Boolean },
  dataset: { type: Object, default: () => ({}) },
  field: { type: Object, default: () => ({}) },
  timeOffset: { type: String, default: '+8' },
})

const datepickerRef = ref(null)
const filterTextRef = ref(null)

const init = () => {
  nextTick(() => {
    datepickerRef.value?.init()
    filterTextRef.value?.init()
  })
}

watch(
  () => props.open,
  op => {
    if (op) {
      init()
    } else {
      datepickerRef.value?.resetOpen?.()
    }
  },
  { immediate: true }
)

defineExpose({ init })

// 日期筛选
const isDate = computed(() => props.field.dataType?.includes('TIME'))

const cancel = () => {
  emits('update:open', false)
  emits('cancel')
}

// 过滤条件确认
const ok = e => {
  // 日期过滤
  if (isDate.value) {
    const { moda, offset, value, extra = {}, hms } = e

    props.field._dtChanged = true // 日期有修改过，看板携带的日期过滤不生效
    props.field.logical = RELATION.AND
    props.field.conditions = [
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

    props.field.filterType = mode
    // 条件过滤
    if (mode === 'CONDITION') {
      props.field.logical = relation === RELATION.OR ? RELATION.OR : RELATION.AND
      props.field.having = scope
      props.field.conditions = conditions.map(t => {
        const { operator, value } = t

        return {
          _id: t._id,
          functionalOperator: operator,
          args: [value],
        }
      })
    } else {
      props.field.logical = RELATION.AND
      props.field.conditions = [
        {
          functionalOperator: exclude ? NOT_IN : IN,
          args: Array.isArray(conditions)
            ? conditions
            : conditions?.split(/\n/).filter(Boolean) ?? [],
        },
      ]
    }
  }

  emits('update:open', false)
  emits('ok')
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
</style>
