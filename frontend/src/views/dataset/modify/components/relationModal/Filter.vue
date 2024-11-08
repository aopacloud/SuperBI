<template>
  <a-popover
    trigger="click"
    placement="bottomLeft"
    v-model:open="open"
    @openChange="onOpenChange"
  >
    <a-tooltip title="关联前，进行数据过滤">
      <a-button
        :class="['filter-btn', active ? 'active' : '']"
        size="small"
        type="text"
        :icon="h(FilterOutlined)"
      />
    </a-tooltip>
    <template #content>
      <RelationList
        class="relation-list"
        style="width: 560px"
        v-model="modelValue"
        size="small"
        :maxLevel="2"
        :copyable="false"
      >
        <template #item="item">
          <a-select
            style="width: 200px"
            placeholder="请选择字段"
            show-search
            :filterOption="filterOption"
            v-model:value="item.name"
            @change="onFieldChange(item)"
          >
            <a-select-option
              v-for="t in fields"
              :key="t.name"
              :label="t.description || t.name"
            >
              {{ t.description || t.name }}
            </a-select-option>
          </a-select>

          <FilterDropdown
            style="flex: 1"
            :dataset="dataset"
            :field="item"
            :disabled="!item.name"
            :placeholder="!item.name ? '请先选择字段' : '请选择'"
          />
        </template>
      </RelationList>

      <div style="margin-top: 12px; text-align: center">
        <a-space>
          <a type="text" size="small" @click="clear">清空条件</a>
          <a-button size="small" @click="cancel" style="margin-left: 12px">
            取消
          </a-button>
          <a-button size="small" type="primary" @click="ok">确认</a-button>
        </a-space>
      </div>
    </template>
  </a-popover>
</template>

<script setup>
import { h, ref } from 'vue'
import { message } from 'ant-design-vue'
import { FilterOutlined } from '@ant-design/icons-vue'
import RelationList from '@/common/components/Layout/RelationList/index.vue'
import FilterDropdown from '@/components/Filter'
import { IS_NOT_NULL, IS_NULL } from '@/views/dataset/config.field'
import { RELATION } from '@/CONST.dict'
import { reactive } from 'vue'
import { getRandomKey } from '@/common/utils/string'
import { computed } from 'vue'
import { deepClone } from '@/common/utils/help'

const props = defineProps({
  table: {
    type: Object,
    default: () => ({})
  },
  active: Boolean
})

const dataset = computed(() => {
  const { dbName, tableName, engine } = props.table
  return {
    config: {
      dbName,
      tableName,
      engine
    }
  }
})

const fields = computed(() => props.table.originFields || [])
const filters = computed(() => props.table.filters || {})

// 筛选条件
const open = ref(false)
const onOpenChange = open => {
  open ? init() : cancel()
}

const modelValue = ref({})
// const hasValidFilters = ref(false)
const filterOption = (input, option) => {
  const { value, label } = option
  return (
    value.toLowerCase().indexOf(input.toLowerCase()) >= 0 ||
    label.toLowerCase().indexOf(input.toLowerCase()) >= 0
  )
}
const onFieldChange = item => {
  const field = fields.value.find(f => f.name === item.name)

  item.dataType = field.dataType
  item.conditions = [{}]
}

const reset = () => {
  modelValue.value = { ...filters.value }
}

const getItem = () => ({
  _key: getRandomKey()
})

const init = () => {
  const { relation = RELATION.AND, children = [] } = deepClone(filters.value)
  modelValue.value.relation = relation
  if (!children.length) {
    modelValue.value.children = [getItem(), getItem()]
  } else {
    modelValue.value.children = [...children]
  }
}

const clear = () => {
  modelValue.value.children = []
  handleOk()
  open.value = false
}

const cancel = () => {
  open.value = false
  setTimeout(() => {
    nextTick(reset)
  }, 100)
}

const handleOk = () => {
  const { relation, children } = modelValue.value
  props.table.filters = { relation, children }
}

const ok = () => {
  if (filterValidatorError(modelValue.value.children)) {
    message.warn(validateErrorInfos.text || '请检查输入项')
  } else {
    open.value = false
    handleOk()
  }
}

const validateErrorInfos = reactive({ text: '' })
const filterValidatorError = (list = []) => {
  if (
    list.some(item => {
      const { children = [], name, conditions = [] } = item

      if (children.length) {
        return filterValidatorError(children)
      }

      // 名称必填
      if (!name) return true

      // 条件必填
      if (
        conditions.some(c => {
          const { useLatestPartitionValue, functionalOperator, args = [] } = c

          // 有数的一天、有值、无值
          if (
            useLatestPartitionValue ||
            functionalOperator === IS_NOT_NULL ||
            functionalOperator === IS_NULL
          )
            return false

          return !args.filter(t => typeof t !== 'undefined' && t !== null)
            .length
        })
      )
        return true
    })
  ) {
    validateErrorInfos.text = '请检查输入项'
    return true
  } else {
    if (isSameFilter(list)) {
      validateErrorInfos.text = '数据满足条件重复'
      return true
    } else {
      return false
    }
  }
}

/**
 * 判断给定的数据满足条件是否存在相同的过滤器
 * @param list 过滤器列表
 * @returns {Boolean}
 */
const isSameFilter = list => {
  // conditions => str
  const _toStr = cs => {
    return cs
      .map(c => {
        const { functionalOperator, args = [] } = c

        return functionalOperator + '-' + args.join(',')
      })
      .join(';')
  }

  // 是否相同的conditions
  const _isSameCondition = (c1 = [], c2 = []) => {
    if (c1.length !== c2.length) return false

    const c1Str = _toStr(c1),
      c2Str = _toStr(c2)

    return c1Str === c2Str
  }
  return list.some((item, index) => {
    const { name, logical, conditions } = item
    const sameName = [...list]
      .filter((item, i) => i !== index)
      .find(t => t.name === name)

    if (
      sameName &&
      logical === sameName.logical &&
      _isSameCondition(conditions, sameName.conditions)
    )
      return true
  })
}
</script>

<style lang="scss" scoped>
.filter-btn {
  &.active {
    color: #1677ff;
  }
}
.i-suffix {
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
.i-input:hover {
  .i-suffix-down:not(:only-child) {
    visibility: hidden;
  }
  .i-suffix-close {
    opacity: 1;
  }
}
</style>
