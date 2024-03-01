<template>
  <a-dropdown
    trigger="click"
    placement="bottomLeft"
    overlayClassName="filter-dropdown"
    v-model:open="open">
    <div class="filter-dropdown-trigger" :class="{ open: open }">
      <slot></slot>

      <FilterOutlined
        class="dropdown-trigger-icon"
        :class="{ active: isDate || (field.conditions && field.conditions.length) }" />
    </div>

    <template #overlay>
      <div class="filter-dropdown-overlay">
        <FilterDate
          v-if="isDate"
          ref="datepickerRef"
          :utcOffset="timeOffset"
          :moda="+(field.conditions[0].timeType === 'EXACT')"
          :offset="field.conditions[0].args"
          :value="field.conditions[0].args"
          :extra="{
            dt: field.conditions[0].useLatestPartitionValue,
            current: field.conditions[0]._this,
            isCustom: field.conditions[0]._current,
          }"
          @cancel="cancel"
          @ok="ok" />

        <FilterText v-else ref="filterTextRef" :field="field" @cancel="cancel" @ok="ok" />
      </div>
    </template>
  </a-dropdown>
</template>

<script setup>
import { ref, computed, watch, nextTick, inject, onMounted } from 'vue'
import { FilterOutlined } from '@ant-design/icons-vue'
import { RELATION } from '@/CONST.dict'
import { NOT_IN, IN } from '@/views/dataset/config.field'
import FilterText from './Text.vue'
import FilterDate from 'common/components/DatePickers/PickerPanel.vue'

const emits = defineEmits(['cancel', 'ok'])
const props = defineProps({
  field: {
    type: Object,
    default: () => ({}),
  },
})

// 时区偏移
const { timeOffset } = inject('index')

const open = ref(false)
const datepickerRef = ref(null)
const filterTextRef = ref(null)
watch(open, op => {
  if (op) {
    nextTick(() => {
      datepickerRef.value?.init()
      filterTextRef.value?.init()
    })
  }
})

onMounted(() => {
  // 自动展开过滤面板
  if (props.field.autoPopover) {
    open.value = true
  }
})

// 日期筛选
const isDate = computed(() => props.field.dataType.includes('TIME'))

const cancel = () => {
  open.value = false
}

// 过滤条件确认
const ok = e => {
  // 日期过滤
  if (isDate.value) {
    const { moda, offset, date, extra = {} } = e

    props.field.logical = RELATION.AND
    props.field.conditions = [
      {
        useLatestPartitionValue: extra.dt || undefined,
        functionalOperator: 'BETWEEN',
        timeType: moda === 0 ? 'RELATIVE' : 'EXACT', // 时间字段的筛选类型， EXACT 精确时间， RELATIVE 相对时间
        args: moda === 0 ? offset : date,
        _this: extra.current,
        _current: extra.isCustom,
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

  open.value = false
  emits('ok')
}
</script>

<style lang="scss" scoped>
.filter-dropdown-trigger {
  display: inline-flex;
}
.dropdown-trigger-icon {
  margin-left: 12px;
  transition: all 0.2s;
  &.active {
    color: #1677ff;
  }
}
</style>

<style lang="scss">
.filter-dropdown {
  .filter-dropdown-overlay {
    background-color: #ffffff;
    background-clip: padding-box;
    border-radius: 8px;
    box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12),
      0 9px 28px 8px rgba(0, 0, 0, 0.05);
    padding: 12px;

    & > .section {
      padding: 0;
      background: #fff;
      border-radius: 0;
      box-shadow: none;
    }
  }
}
</style>
