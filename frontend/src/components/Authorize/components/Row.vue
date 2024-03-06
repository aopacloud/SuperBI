<template>
  <div class="row">
    <div class="relation">
      <span
        class="relation-text"
        :class="{ active: relation === RELATION.AND }"
        @click="toggleRelation"
        >{{ relationText }}
      </span>
    </div>
    <div class="list" :style="listStyle">
      <div class="item" v-for="(item, index) in list">
        <Row
          v-if="item.children && item.children.length"
          :level="level + 1"
          :dataset="dataset"
          :row="{ relation: item.relation, children: item.children }"
          @update:row="e => onRowUpdate(e, index)"
          @update:levelsRow="e => onRowUpdate(e, index)" />

        <!-- @update:row="e => onRowUpdate(e, index)" -->
        <RowItem
          v-else
          :dataset="dataset"
          :removable="list.length > 1"
          :addable="addable"
          :level="level"
          :maxLevel="maxLevel"
          :item="item"
          @split="onSplit(index)"
          @add="onAddFromRowItem(index)"
          @remove="onRemoveFromRowItem(index)" />
      </div>
    </div>
  </div>

  <a-button
    v-if="level === 1"
    type="link"
    size="small"
    style="display: inline-block; margin-top: 10px"
    :disabled="!addable"
    @click="add"
    >添加规则
  </a-button>
</template>

<script setup>
import { ref, computed, watch, watchEffect, nextTick, inject } from 'vue'
import RowItem from './RowItem.vue'
import { getRandomKey } from 'common/utils/help'
import { RELATION } from '@/CONST.dict'

const getRowItem = () => {
  return {
    _id: getRandomKey(),
    relation: RELATION.OR,
    type: 'ENUM',
  }
}

const emits = defineEmits(['update:levelsRow', 'update:row'])
const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({}),
  },
  row: {
    type: Object,
    default: () => ({}),
  },
  level: {
    type: Number,
    default: 1,
  },
  maxLevel: {
    type: Number,
    default: 2,
  },
  max: {
    type: Number,
    default: 10,
  },
})
const addable = inject('canaddable')

const relation = ref('')
const relationText = computed(() => {
  return relation.value === RELATION.AND ? '且' : '或'
})
const toggleRelation = () => {
  relation.value = relation.value === RELATION.AND ? RELATION.OR : RELATION.AND
}

const list = ref([])
watchEffect(() => {
  const { relation: rel, children = [] } = props.row

  relation.value = rel || RELATION.OR
  list.value = children

  if (props.level === 1 && !children.length) {
    list.value = [getRowItem()]
  }
})

const modelValue = computed(() => {
  return {
    relation: relation.value,
    children: list.value,
  }
})
watch(
  modelValue,
  vals => {
    emits('update:row', vals)
  },
  { deep: true }
)

/**
 * 获取 list 样式
 * TODO: 多层级此处应递归计算
 * @param {array} list
 */
const getListStyle = (list = []) => {
  const first = list[0],
    fLen = first?.children?.length || 0
  const last = list.slice(-1)[0],
    lLen = last?.children?.length || 0

  let top = 16,
    bottom = 16
  if (fLen > 1) {
    top = fLen * 16 + (fLen - 1) * 5
  }
  if (lLen > 1) {
    bottom = lLen * 16 + (lLen - 1) * 5
  }
  return {
    top,
    bottom,
  }
}
const listStyle = computed(() => {
  const { top, bottom } = getListStyle(list.value)

  return {
    '--top': top + 'px',
    '--bottom': bottom + 'px',
  }
})

// 添加
const add = () => {
  if (typeof list.value === 'undefined') {
    list.value = []
  }

  list.value.push(getRowItem())
}
// 拆分
const onSplit = i => {
  if (list.value.length === 1) {
    list.value.push(getRowItem())
  } else {
    let child = list.value[i]

    if (typeof child.children === 'undefined') {
      child = {
        relation: child.relation,
        children: [{ ...child }],
      }
    }

    child.children.push(getRowItem())

    list.value[i] = { ...child }
  }
}
// 从child中添加
const onAddFromRowItem = i => {
  const item = getRowItem()

  list.value.splice(i + 1, 0, item)
}
// 从child中移除
const onRemoveFromRowItem = i => {
  list.value.splice(i, 1)

  // 当前只有一个
  if (list.value.length === 1) {
    if (props.level === 1) {
      const { relation: rel, children = [] } = list.value[0]

      relation.value = rel
      // children有多个，展开进当前层级
      if (children.length > 1) {
        nextTick(() => {
          list.value = [...children]
        })
      }
    } else {
      nextTick(() => {
        emits('update:levelsRow', {
          ...list.value[0],
        })
      })
    }
  }
}

// 更新Children - 将只有一个child的children展开进父级
const onRowUpdate = (e, i) => {
  list.value[i] = { ...e }
}
</script>

<style lang="scss" scoped>
.row {
  display: flex;
}
.content {
  display: flex;
}
.list {
  position: relative;
  flex: 1;
  margin-left: 16px;
  &:not(:empty)::before {
    content: '';
    position: absolute;
    width: 1px;
    top: var(--top, 16px);
    left: -10px;
    bottom: var(--bottom, 16px);
    background-color: #ddd;
  }

  .item {
    position: relative;
    &::before {
      content: '';
      position: absolute;
      left: -10px;
      width: 10px;
      background-color: #fff;
    }
    &:first-child {
      &::before {
        top: 0px;
        height: calc(50% + 1px);
        border-bottom: 1px solid #ddd;
      }
    }
    &:not(:only-child):last-child {
      &::before {
        bottom: 0;
        height: calc(50% + 1px);
        border-top: 1px solid #ddd;
      }
    }
  }

  .row {
    position: relative;
    &::before {
      content: '';
      position: absolute;
      top: 50%;
      left: -10px;
      width: 12px;
      height: 1px;
      background-color: #ddd;
    }
  }
  & > .item:not(:first-child) {
    margin-top: 10px;
  }
}
.relation {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 30px;
  min-height: 32px;
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    right: -6px;
    width: 8px;
    height: 1px;
    background-color: #ddd;
  }
  &-text {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    position: relative;
    width: 26px;
    height: 26px;
    border: 1px solid #d8d8d8;
    background-color: #fff;
    border-radius: 2px;
    text-align: center;
    color: #3d90ff;
    transition: all 0.2s;
    cursor: pointer;
    &.active,
    &:hover {
      background-color: #3d90ff;
      color: #fff;
    }
  }
}
</style>
