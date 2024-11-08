<template>
  <div
    ref="elRef"
    :allLength="allLength"
    :class="['relation-wrapper', { 'no-relation': !hasRelation }]">
    <div v-if="hasRelation" class="relation-label">
      <span
        :class="['relation-text', { active: relation === RELATION.AND }]"
        @click="toggleRelation">
        {{ relationText }}
      </span>
    </div>

    <div
      class="relation-list"
      :style="{
        paddingLeft: hasRelation ? '12px' : '',
        '--top': styleObj.top,
        '--bottom': styleObj.bottom,
      }">
      <template v-for="(item, index) in list" :key="item._id">
        <RelationList
          v-if="item.children && item.children.length > 1"
          class="relation-item"
          :ref="e => setListRefs(e, index)"
          :hasRelation="hasRelation"
          :level="level + 1"
          :modelValue="item"
          @update:modelValue="e => onModelValueUpdate(e, index)"
          @updateList="onListChildrenUpdate(item, index)">
        </RelationList>
        <RelationItem
          v-else
          :ref="e => setItemRefs(e, index)"
          :index="index"
          :level="level"
          :item="item"
          @add="onAdd(index)"
          @copy="onCopy(index, item)"
          @split="onSplit(item)"
          @delete="onDelete(index)" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, provide, watch, watchEffect } from 'vue'
import RelationItem from './RelationItem.vue'
import { RELATION } from '@/CONST.dict'
import { deepClone } from 'common/utils/help'
import { getRandomKey } from 'common/utils/string'

defineOptions({
  name: 'RelationList',
})

const emits = defineEmits(['update:modelValue', 'updateList'])

const props = defineProps({
  // 当前层级
  level: {
    type: Number,
    default: 1,
  },
  // 最大层级
  maxLevel: {
    type: Number,
    default: 5,
  },
  // 最大条数
  max: {
    type: Number,
    default: 0,
  },
  //  是否可复制
  copyable: {
    type: Boolean,
    default: true,
  },
  //  是否可分割
  splitable: {
    type: Boolean,
    default: true,
  },
  // 值
  modelValue: {
    type: [Array, Object],
    default: () => ({
      relation: RELATION.AND,
    }),
  },
  hasRelation: {
    type: Boolean,
    default: true,
  },
  size: {
    type: String,
    default: 'middle',
    validator: value => ['small', 'middle', 'large'].includes(value),
  },
})

const getItem = r => ({ _id: getRandomKey(), ...r })

// 关系
const relation = ref(RELATION.AND) // AND OR
const relationText = computed(() => (relation.value === RELATION.AND ? '且' : '或'))
const toggleRelation = () => {
  relation.value = relation.value === RELATION.AND ? RELATION.OR : RELATION.AND
}

// 单个项的el
const itemRefs = ref({})
const setItemRefs = (e, i) => {
  const n = i === 0 ? 'start' : i === list.value.length - 1 ? 'end' : i
  itemRefs.value[n] = e
}

// 所有list的el
const listRefs = ref({})
const setListRefs = (e, i) => {
  const n = i === 0 ? 'start' : i === list.value.length - 1 ? 'end' : i
  listRefs.value[n] = e
}

const aStyle = ref({ top: 0, bottom: 0 })
// 根据dom获取距离，会频繁触发回流
const updateStyleObjByDom = () => {
  const firstListRef = listRefs.value['start'] || itemRefs.value['start']
  const lastListRef = listRefs.value['end'] || itemRefs.value['end']

  let top = 0,
    bottom = 0
  if (firstListRef) top = firstListRef.$el?.clientHeight / 2 + 'px'

  if (lastListRef) bottom = lastListRef.$el?.clientHeight / 2 + 'px'

  aStyle.value = { top, bottom }
}

// 当前el
const elRef = ref()

// 样式变量
const styleVars = computed(() => {
  if (!elRef.value) return {}

  const computedStyle = window.getComputedStyle(elRef.value)

  const gap = computedStyle.getPropertyValue('--gap'),
    itemHeight = computedStyle.getPropertyValue('--itemHeight') || '32px' // 默认值

  return {
    gap: parseInt(gap),
    itemHeight: parseInt(itemHeight),
  }
})

const _getChild = (list = []) => {
  return list.reduce((acc, item) => {
    if (item.children?.length) {
      acc = acc.concat(_getChild(item.children))
    } else {
      acc.push(item)
    }

    return acc
  }, [])
}

// 连线样式
const styleObj = computed(() => {
  if (!elRef.value || !props.hasRelation) return {}

  // 获取距离
  const getDis = l =>
    (l * styleVars.value.itemHeight + (l - 1) * styleVars.value.gap) / 2

  // 获取所有子节点、孙子节点

  // 首节点
  const firstItem = list.value[0] || {}
  // 尾节点
  const lastItem = list.value[list.value.length - 1] || {}
  // 首节点子节点
  const firstChildren = firstItem.children
  // 尾节点子节点
  const lastChildren = lastItem.children

  const setDis = children => {
    if (children?.length) {
      const deepChildren = _getChild(children)
      return getDis(deepChildren.length)
    } else {
      return styleVars.value.itemHeight / 2
    }
  }

  const top = setDis(firstChildren) + 'px'
  const bottom = setDis(lastChildren) + 'px'

  return { top, bottom }
})

const list = ref([])
// watch(list, () => nextTick(updateStyleObjByDom), { deep: true })

watch(
  [() => relation, () => list],
  ([r, l]) => {
    if (props.hasRelation) {
      emits('update:modelValue', { relation: r, children: l })
    } else {
      emits('update:modelValue', l)
    }
  },
  { deep: true }
)

// 当前list的长度求和（包含孙子节点）
const listLengthSum = list => {
  return list.reduce((sum, item) => {
    if (item.children && item.children.length) {
      sum += listLengthSum(item.children)
    } else {
      sum += 1
    }

    return sum
  }, 0)
}

// list 长度
const allLength = computed(() => listLengthSum(list.value))

// 只在第一层透传数据
if (props.level === 1) {
  provide('relation', {
    allLength,
  })
}

watchEffect(() => {
  if (props.hasRelation) {
    const { relation: rel, children = [] } = props.modelValue

    relation.value = rel
    if (!children.length) {
      list.value = [getItem()]
    } else {
      list.value = children
    }
  } else {
    if (!props.modelValue.length) {
      list.value = [getItem()]
    } else {
      list.value = props.modelValue
    }
  }
})

const onModelValueUpdate = (e, i) => {
  list.value[i] = e
}

// 更新list.children的数据
const onListChildrenUpdate = (item, index) => {
  if (item.children?.length === 1) {
    const child = item.children[0]

    list.value[index] = { ...item, ...child, children: undefined }
  }
}

// 添加
const onAdd = index => {
  list.value.splice(index + 1, 0, getItem())
}

// 复制
const onCopy = (index, item) => {
  list.value.splice(index, 0, getItem(deepClone(item)))
}

// 分裂
const onSplit = item => {
  // 只有一层的时候，且只有一个时直接添加
  if (props.level === 1 && list.value.length === 1) {
    onAdd()
  } else {
    const old = deepClone(item)
    item.relation = RELATION.AND
    item.children = [old, getItem()]
  }
}

// 删除
const onDelete = index => {
  list.value.splice(index, 1)

  if (list.value.length === 1) {
    emits('updateList', list.value)
  }
}
</script>

<style lang="scss" scoped>
.relation-wrapper {
  --gap: 12px;
  --itemHeight: inherit;
  display: flex;
  align-items: center;

  &.no-relation {
    .relation-list::before,
    .relation-item::before {
      display: none;
    }
  }
}

.relation-label {
  position: relative;
  padding-right: 12px;
  &::after {
    content: '';
    position: absolute;
    width: 100%;
    height: 1px;
    top: 50%;
    right: 0;
    background-color: #d8d8d8;
  }
}

.relation-text {
  position: relative;
  display: inline-flex;
  min-width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  border: 1px solid #d8d8d8;
  background-color: #fff;
  border-radius: 2px;
  cursor: pointer;
  user-select: none;
  transform: all 0.2s;
  z-index: 1;
  &.active {
    border-color: #3d90ff;
    background-color: #3d90ff;
    color: #fff;
  }

  &:hover:not(.active) {
    border-color: #3d90ff;
    color: #3d90ff;
  }
}

.relation-list {
  flex: 1;
  position: relative;
  padding-left: '12px';
  overflow: hidden;
  &::before {
    content: '';
    position: absolute;
    width: 1px;
    top: var(--top, 0);
    bottom: var(--bottom, 0);
    left: 0;
    background-color: #d8d8d8;
  }

  & > .relation-wrapper {
    position: relative;
    &::before {
      content: '';
      position: absolute;
      width: 100%;
      height: 1px;
      top: 50%;
      left: -100%;
      background-color: #d8d8d8;
    }
  }
}
.relation-item {
  position: relative;
  &::before {
    content: '';
    position: absolute;
    left: -100%;
    top: 50%;
    height: 1px;
    width: 100%;
    background-color: #d8d8d8;
  }
  & + .relation-item {
    margin-top: var(--gap);
  }
}
</style>
