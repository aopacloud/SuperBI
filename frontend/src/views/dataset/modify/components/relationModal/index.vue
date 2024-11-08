<template>
  <a-modal
    title="数据关联"
    :width="1000"
    :maskClosable="false"
    :open="open"
    @cancel="cancel"
    @ok="ok"
  >
    <section>
      <div class="section-title">数据选择</div>
      <div class="flex">
        <Splitpanes
          style="height: 457px"
          class="default-theme select-splitpanes"
          :dbl-click-splitter="false"
          @resize="leftSize = $event[0].size"
        >
          <Pane :size="leftSize" style="display: flex">
            <SelectVue type="source" :table="sourceTable"></SelectVue>
          </Pane>
          <Pane :size="100 - leftSize" style="display: flex">
            <SelectVue
              type="target"
              :sourceHasDt="sourceHasDt"
              :table="targetTable"
            ></SelectVue>
          </Pane>
        </Splitpanes>
      </div>
    </section>

    <section style="margin-top: 30px">
      <div class="section-title">
        字段关联
        <div class="flex-inline align-center" style="margin-left: 12px">
          <JoinIcon style="font-size: 26px" :joinType="joinType" />
          <div class="custom-select small join-select">
            <select v-model="joinType">
              <option v-for="t in joinOptions" :key="t.value" :value="t.value">
                {{ t.label }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <div class="join-list">
        <div class="flex" style="margin-bottom: 8px">
          <div class="flex-1">
            {{ sourceTable.dbName }}.{{ sourceTable.tableName }}
          </div>
          <div class="flex-1" style="margin-left: 10px">
            {{ targetTable.dbName }}.{{ targetTable.tableName }}
          </div>
        </div>

        <JoinItem
          v-for="(item, index) in joinFields"
          :key="item._key"
          :source="sourceTable"
          :target="targetTable"
          :item="item"
          :removable="joinFields.length > 1"
          @remove="onRemove(index)"
        />
        <a @click="add">添加关联字段</a>
      </div>
    </section>
  </a-modal>
</template>

<script setup>
import { toRaw, provide } from 'vue'
import { message } from 'ant-design-vue'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import SelectVue from './Select.vue'
import JoinItem from './JoinItem.vue'
import JoinIcon from '../graph/components/JoinIcon.vue'
import { joinOptions } from '../../config'
import { getRandomKey } from '@/common/utils/string'
import { versionJs } from '@/versions'

const emits = defineEmits(['update:open', 'ok', 'update:joinDescriptor'])
const props = defineProps({
  open: {
    type: Boolean
  },
  source: {
    type: Object,
    default: () => ({})
  },
  target: {
    type: Object,
    default: () => ({})
  },
  joinDescriptor: {
    type: Object,
    default: () => ({})
  },
  dataset: {
    type: Object,
    default: () => ({})
  }
})

provide('relationModal', {
  dataset: {
    get: () => props.dataset
  }
})

const sourceHasDt = computed(() =>
  props.source.originFields.some(versionJs.ViewsDatasetModify.isDt)
)
const targetHasDt = computed(() =>
  props.target.originFields.some(versionJs.ViewsDatasetModify.isDt)
)

const cancel = () => {
  emits('update:open', false)
}
const ok = () => {
  if (!validate()) return

  emits('ok', {
    source: transformTable(sourceTable.value),
    target: transformTable(targetTable.value),
    joinDescriptor: {
      sourceAlias: sourceTable.value.alias,
      targetAlias: targetTable.value.alias,
      joinType: joinType.value,
      joinFields: toRaw(joinFields.value)
    }
  })

  emits('update:open', false)
}

const transformTable = ({ filters, ...res }) => {
  if (!filters) return res

  const f = (list = [], rel, level = 1) => {
    if (!list.length) return []

    return list.map(item => {
      if (item.children?.length > 1) {
        const relation = item.relation ?? rel
        return {
          relation,
          children: f(item.children, relation, level + 1)
        }
      } else {
        if (level === 1) {
          return {
            relation: item.relation ?? rel,
            children: [{ ...item }]
          }
        } else {
          return item
        }
      }
    })
  }

  return {
    ...res,
    filters: {
      relation: filters.relation,
      children: f(filters.children, filters.relation, 1)
    }
  }
}

const reverseTable = ({ filters, ...res }) => {
  if (!filters) return res

  const f = (list, rel) => {
    return list.map(item => {
      if (!item.children) return item

      if (item.children.length === 1) {
        const t = { ...item, ...item.children[0] }
        // const t = { ...item, ...f(item.children) }
        delete t.children

        return t
      }

      return {
        ...item,
        children: f(item.children)
      }
    })
  }

  return {
    ...res,
    filters: {
      relation: filters.relation,
      children: f(filters.children)
    }
  }
}

// 选中字段叫校验
const fieldValidator = () => {
  const hasSourceDt = sourceTable.value.columns.some(
    t => t === versionJs.ViewsDatasetModify.dtFieldName
  )
  const hasTargetDt = targetTable.value.columns.some(
    t => t === versionJs.ViewsDatasetModify.dtFieldName
  )

  if (hasSourceDt && hasTargetDt) {
    message.warn('dt字段只能勾选一个')
    return false
  }

  return true
}

// 关联校验
const joinValidator = () => {
  const list = joinFields.value
  if (!list.length) {
    message.warn('关联字段不能为空')
    return false
  }

  if (
    list.some(({ sourceField, targetField }) => !sourceField || !targetField)
  ) {
    message.warn('关联字段不能为空')
    return false
  }

  let dtPassed = true
  if (!sourceHasDt.value || !targetHasDt.value) {
    dtPassed = true
  } else {
    dtPassed = list.some(({ sourceField, targetField }) => {
      return (
        sourceField === versionJs.ViewsDatasetModify.dtFieldName &&
        targetField === versionJs.ViewsDatasetModify.dtFieldName
      )
    })
  }

  if (!dtPassed) {
    // 任一表有dt作为过滤条件时，不校验dt作为关联字段
    const sTFHasDt = (sourceTable.value?.filters?.children || []).some(
      t => t.name === versionJs.ViewsDatasetModify.dtFieldName
    )
    const tTFHasDt = (targetTable.value?.filters?.children || []).some(
      t => t.name === versionJs.ViewsDatasetModify.dtFieldName
    )
    if (!sTFHasDt && !tTFHasDt) {
      message.warn('两个表都有dt的时候，dt必须作为关联字段')
      return false
    }
  }

  if (
    list.some(({ sourceField, targetField }) => {
      // 关联字段类型需保持一致
      const sourceOriginField = sourceTable.value.originFields.find(
        t => t.name === sourceField
      )
      const targetOriginField = targetTable.value.originFields.find(
        t => t.name === targetField
      )

      return sourceOriginField.dataType !== targetOriginField.dataType
    })
  ) {
    message.warn('关联字段类型需保持一致')
    return false
  }

  return true
}
const validate = () => {
  // 校验数据选择
  if (!fieldValidator()) return
  // 校验字段关联
  if (!joinValidator()) return

  return true
}

watch(
  () => props.open,
  v => {
    if (v) {
      init()
    } else {
      reset()
    }
  }
)

const getItem = r => {
  return {
    _key: getRandomKey(),
    ...r
  }
}

const joinType = ref()
const joinFields = ref([])

const add = () => {
  joinFields.value.push(getItem())
}
const onRemove = index => {
  joinFields.value.splice(index, 1)
}

const leftSize = ref(50)

const sourceTable = ref({})
const targetTable = ref({})

const reset = () => {}
const init = () => {
  leftSize.value = 50

  const { source, target } = props
  sourceTable.value = reverseTable(source)
  targetTable.value = reverseTable(target)

  const { joinType: type = 'LEFT', joinFields: fs = [] } = props.joinDescriptor

  joinType.value = type
  if (fs.length === 0) {
    joinFields.value = [getItem()]
  } else {
    // 从底层源表变更
    joinFields.value = fs.map(({ sourceField, targetField, joinType }) => {
      const sourceOriginField = sourceTable.value.originFields.find(
        t => t.name === sourceField
      )
      const targetOriginField = targetTable.value.originFields.find(
        t => t.name === targetField
      )

      return {
        joinType,
        sourceField: sourceOriginField?.name,
        targetField: targetOriginField?.name
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.section-title {
  position: relative;
  display: flex;
  align-items: center;
  padding-left: 12px;
  margin: 12px 0;
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 2px;
    bottom: 2px;
    width: 3px;
    background-color: #1677ff;
  }
}
.select-splitpanes {
  :deep(.splitpanes__splitter) {
    margin: 0 6px;
    border: none;
  }
  .splitpanes__pane {
    background-color: initial;
  }
}
.join-select {
  &::before {
    right: 10px;
  }
  &::after {
    right: 6px;
  }
  & > select {
    padding-right: 20px;
    color: rgba(0, 0, 0, 0.66);
    border: none;
    outline: none;
    box-shadow: none !important;
  }
}
.join-list {
  :deep(.join-item) {
    margin-bottom: 8px;
    .remove-btn {
      opacity: 0;
      pointer-events: none;
    }
    &:hover {
      .remove-btn {
        opacity: 1;
        pointer-events: initial;
      }
    }
  }
}
</style>
