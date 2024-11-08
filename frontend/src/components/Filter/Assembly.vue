<template>
  <div class="filter-assembly">
    <RelationList
      style="max-height: 500px; overflow: auto"
      :maxLevel="2"
      :copyable="false"
      v-model="modelValue"
    >
      <template #item="item">
        <a-select
          style="width: 160px"
          placeholder="请选择字段"
          show-search
          :options="fields"
          :filterOption="filterOption"
          :field-names="{ label: 'displayName', value: 'name' }"
          v-model:value="item.name"
          @change="onFieldChange(item)"
        >
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

    <a-space class="wrapper-footer">
      <a-button @click="cancelHandler">取消</a-button>
      <a-button type="primary" @click="okHandler">确定</a-button>
    </a-space>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import RelationList from 'common/components/Layout/RelationList/index.vue'
import FilterDropdown from '@/components/Filter/FilterDropdown.vue'
import { IS_NOT_NULL, IS_NULL } from '@/views/dataset/config.field'
import { RELATION } from '@/CONST.dict'
import { deepClone } from '@/common/utils/help'

const emits = defineEmits(['cancel', 'ok'])

const { dataset, field } = defineProps({
  dataset: {
    type: Object,
    default: () => ({})
  },
  field: {
    type: Object,
    default: () => ({})
  }
})

const fields = computed(() => dataset.fields || [])

const filterOption = (input, option) =>
  (option.displayName + option.name)
    .toLowerCase()
    .indexOf(input.toLowerCase()) >= 0

const onFieldChange = item => {
  const originField = fields.value.find(t => t.name === item.name)

  if (item.dataType !== originField.dataType) {
    item.conditions = [{ relation: RELATION.AND }]
  }
  item.dataType = originField.dataType
}

const modelValue = ref({
  relation: RELATION.AND
})

// TODO
// 1.逻辑待优化； 2.封装成工具函数
const _transformRowsInit = (list = []) => {
  const f = (l = []) => {
    return l.map(t => {
      if (!t.children) return t

      if (t.children.length === 1) {
        const it = { ...t, ...t.children[0] }
        delete it.children
        delete it.relation
        return it
      }

      return { ...t, children: f(t.children) }
    })
  }

  return f(list)
}

const init = () => {
  const { tableFilter = { relation: RELATION.AND, children: [] } } = field

  modelValue.value = {
    relation: tableFilter.relation,
    children: _transformRowsInit(tableFilter.children)
  }
}
defineExpose({ init })

onMounted(() => {
  init()
})

// 检验错误
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
    const { name, children = [], logical, conditions } = item
    if (children.length) return false

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

const cancelHandler = () => {
  emits('cancel')
}

// TODO
// 1.逻辑待优化； 2.封装成工具函数
const transformFilter = (
  { relation: rel = RELATION.AND, children = [] },
  level = 1
) => {
  return {
    relation: rel,
    children: children.map(item => {
      const { relation = rel, children = [] } = item

      if (children.length) {
        return transformFilter({ relation, children }, level + 1)
      } else {
        return level === 1 ? { relation, children: [{ ...item }] } : { ...item }
      }
    })
  }
}

const okHandler = () => {
  if (filterValidatorError(modelValue.value.children)) {
    message.warn(validateErrorInfos.text || '请检查输入项')
  } else {
    open.value = false
    emits('ok', transformFilter({ ...modelValue.value }))
  }
}
</script>

<style lang="scss" scoped>
.filter-assembly {
  padding: 20px 16px 0 24px;
  max-width: 560px;
}
.wrapper-footer {
  display: flex;
  width: 100%;
  justify-content: flex-end;
  margin: 50px 0 16px;
}
</style>
