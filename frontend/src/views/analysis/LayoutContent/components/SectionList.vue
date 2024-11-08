<template>
  <section class="section-list">
    <aside>{{ label }}</aside>

    <draggable
      class="list"
      tag="main"
      itemKey="_id"
      ghost-class="tag-ghost"
      ref="listRef"
      :list="dataSource"
      @start="onStart"
      @dragover.native="onDragover"
      @dragleave.native="onDragleave"
      @drop.native="onDrop"
    >
      <template #item="{ element }">
        <SectionTag
          v-if="isTagVisible(element)"
          class="list-item"
          :open="open"
          :dataset="dataset"
          :tag="element"
          :category="category"
          :color="tagColor"
          @remove="onRemove(element)"
        >
          {{ element.displayName }}
        </SectionTag>
      </template>

      <template #footer>
        <!-- 字段多选 -->
        <MultipleSelectedPopover
          v-if="hasDatasetAnalysis"
          class="multiple-trigger"
          :data-source="datasetFields"
          :value="multipleValue"
          @ok="onMultipleOk"
          @addAssembly="onAddAssembly"
        />
      </template>
    </draggable>

    <!-- 清除选中 -->
    <a-button
      v-if="hasDatasetAnalysis"
      size="small"
      class="clear-button"
      :icon="h(CloseOutlined)"
      @click="handleClear"
    ></a-button>
  </section>
</template>

<script setup>
import { h, computed, inject } from 'vue'
import { CloseOutlined } from '@ant-design/icons-vue'
import draggable from 'vuedraggable'
import MultipleSelectedPopover from '@/views/analysis/components/MultipleSelectedPopover.vue'
import SectionTag from './SectionTag.vue'
import {
  categoryMap,
  SUMMARY_PROPERTY_DEFAULT,
  SUMMARY_INDEX_DEFAULT
} from '@/views/dataset/config.field'
import { getSectionListLabel } from '@/views/analysis/config'
import { CATEGORY } from '@/CONST.dict'
import { getRandomKey } from 'common/utils/string'
import { getNextIndex } from 'common/utils/help'

const props = defineProps({
  draggableGroup: String,
  dataset: {
    type: Object,
    default: () => ({})
  },
  category: {
    type: String,
    default: CATEGORY.PROPERTY
  },
  group: {
    type: String
  },
  dataSource: {
    type: Array,
    default: () => []
  },
  open: Boolean
})

// 使用计算属性需要处理数据的更新传递（数据传递方式较为复杂）
const isTagVisible = item => {
  if (props.category !== CATEGORY.PROPERTY) return true

  if (props.group === 'row') {
    return item._group === 'row' || typeof item._group === 'undefined'
  } else if (props.group === 'column') {
    return item._group === 'column'
  } else {
    return true
  }
}

const {
  draggingField: indexDraggingField,
  choosed: indexChoosed,
  options: indexOptions,
  compare: indexCompare,
  permissions
} = inject('index', {})

// 数据集分析权限
const hasDatasetAnalysis = computed(() => permissions.dataset.hasAnalysis())

const renderType = computed(() => indexOptions.get('renderType'))

const multipleValue = computed(() => {
  if (props.category === CATEGORY.INDEX) return []

  return props.dataSource
    .filter(t => {
      if (props.group === 'column') {
        return t._group === props.group
      } else {
        return t._group !== 'column'
      }
    })
    .map(item => item.name)
})

// 原始字段
const datasetFields = computed(() => {
  const list =
    props.dataset.fields?.filter(
      item =>
        item.status !== 'HIDE' &&
        (item.category === props.category || props.category === CATEGORY.FILTER)
    ) || []

  if (props.category !== CATEGORY.FILTER) return list

  return list.map(item => {
    const { force, filters } = props.dataset.extraConfig

    return {
      ...item,
      disabled: force && filters.some(t => t.name === item.name)
    }
  })
})

const label = computed(() => {
  const typeMap = getSectionListLabel(renderType.value)

  if (props.group === 'column') {
    return typeMap(props.category + '_COLUMN')
  } else {
    return typeMap(props.category)
  }
})

const tagColor = computed(() => {
  return categoryMap[props.category]['color']
})

const onRemove = item => {
  const index = props.dataSource.findIndex(t => t._id === item._id)

  props.dataSource.splice(index, 1)
}

const { clear: choosedClear, set: choosedSet, get: choosedGet } = indexChoosed
// 清除
const handleClear = () => {
  if (props.category !== CATEGORY.PROPERTY) {
    choosedClear(props.category)
  } else {
    if (props.group === 'row') {
      const old = props.dataSource.filter(
        t => typeof t._group !== 'undefined' && t._group !== 'row'
      )
      choosedSet(props.category, old)
    } else if (props.group === 'column') {
      const old = props.dataSource.filter(t => t._group !== 'column')
      choosedSet(props.category, old)
    } else {
      choosedClear(props.category)
    }
  }
}

// 当前拖动的字段
const draggingField = computed(() => indexDraggingField.get())

const listRef = ref(null)
// 默认不允许被拖入， 需阻止默认行为 evt.preventDefault()
const onDragover = evt => {
  if (!draggingField.value) return

  // 正在拖动的字段
  const dragging = draggingField.value

  const parent = listRef.value.$el

  if (props.category === CATEGORY.FILTER) {
    if (dragging._fromInner) return false

    // 过滤
    const choosedFilters = choosedGet(props.category)

    if (choosedFilters.some(t => t.name === dragging.name)) {
      return false
    } else {
      parent.classList.add('drag-ghost')
      evt.preventDefault()
    }
  } else if (props.category === CATEGORY.INDEX) {
    // 指标
    parent.classList.add('drag-ghost')
    evt.preventDefault()
  } else {
    // 维度

    //  指标不可拖入维度
    if (dragging.category === CATEGORY.INDEX) return

    parent.classList.add('drag-ghost')
    evt.preventDefault()
  }
}

const onDragleave = () => {
  listRef.value.$el.classList.remove('drag-ghost')
}

const onStart = ({ item }) => {
  // 过滤字段不允许拖动
  if (props.category === CATEGORY.FILTER) return

  const old = props.dataSource.find(t => t._id === item.dataset.id)
  indexDraggingField.set({ ...old, _fromInner: true, _from: props.category })
}

// 重置为指标字段
const _resetToI = field => {
  delete field._group
  delete field._weekStart // 移除周起始日
  delete field.dateTrunc // 移除日期分组
  delete field.viewModel // 移除日期显示
  delete field.firstDayOfWeek // 移除一周的开始

  return {
    ...field,
    _id: getRandomKey(),
    aggregator:
      field.category === CATEGORY.PROPERTY
        ? SUMMARY_PROPERTY_DEFAULT
        : SUMMARY_INDEX_DEFAULT
  }
}

// 重置为维度字段
const _resetToP = field => {
  delete field.fastCompute // 移除快速计算
  delete field.aggregator // 移除聚合函数

  return { ...field, _id: getRandomKey(), _group: props.group }
}

// 拖动结束
const onDrop = () => {
  if (!draggingField.value) return

  listRef.value.$el.classList.remove('drag-ghost')

  const dragging = draggingField.value

  if (props.category === CATEGORY.FILTER) {
    // 拖动到过滤

    // 内部拖拽不可拖入到过滤
    if (dragging._fromInner) {
      indexDraggingField.set()
      return
    }

    props.dataSource.push({ ...dragging, _id: getRandomKey() })
  } else if (props.category === CATEGORY.INDEX) {
    // 拖动到指标

    // 从外部左侧拖入
    if (!dragging._fromInner) {
      props.dataSource.push({ ...dragging, _id: getRandomKey() })
      indexDraggingField.set()
      return
    }

    // 指标到指标不做处理
    if (dragging._from === CATEGORY.INDEX) {
      indexDraggingField.set()
      return
    }

    const pChoosed = indexChoosed.get(CATEGORY.PROPERTY)
    // 从原有的维度中删除
    indexChoosed.set(
      CATEGORY.PROPERTY,
      pChoosed.filter(t => t._id !== dragging._id)
    )
    // 新增进现在的指标中
    props.dataSource.push(_resetToI(dragging))
  } else if (props.category === CATEGORY.PROPERTY) {
    // 拖动到维度

    // 指标不可拖入到维度
    if (dragging.category === CATEGORY.INDEX) {
      indexDraggingField.set()
      return
    }

    // 拖入到维度
    if (dragging._from === CATEGORY.PROPERTY) {
      if (props.group === 'row') {
        // 当前为行分组

        if (
          dragging._group === 'row' ||
          typeof dragging._group === 'undefined'
        ) {
          // 行分组件的拖动不做处理
          indexDraggingField.set()
          return
        } else {
          const item = props.dataSource.find(t => t._id === dragging._id)
          item._group = props.group
        }
      } else if (props.group === 'column') {
        // 当前为列分组

        if (dragging._group === 'column') {
          // 列分组件的拖动不做处理
          indexDraggingField.set()
          return
        } else {
          const item = props.dataSource.find(t => t._id === dragging._id)
          item._group = props.group
        }
      } else {
        indexDraggingField.set()
        return
      }
    }

    const iChoosed = indexChoosed.get(CATEGORY.INDEX)
    // 从原有的指标中删除
    indexChoosed.set(
      CATEGORY.INDEX,
      iChoosed.filter(t => t._id !== dragging._id)
    )
    const index = props.dataSource.findIndex(t => t.name === dragging.name)
    if (index > -1) {
      props.dataSource.splice(index, 1)
    }
    props.dataSource.push(_resetToP(dragging))
  }
  indexDraggingField.set()
}

// 批量选中
const onMultipleOk = value => {
  // 延迟赋值，否则会有闪烁
  setTimeout(() => {
    const list = props.dataSource
    const choosed = value.map(t => {
      const item = datasetFields.value.find(f => f.name === t)
      // 已选中的过滤项
      const pre = list.find(d => d.name === item.name)

      return { ...item, ...pre, _id: getRandomKey(), _group: props.group }
    })

    if (props.category === CATEGORY.INDEX) {
      choosedSet(props.category, list.concat(choosed))
    } else {
      const other = list.filter(t => {
        if (value.includes(t.name)) return false

        if (props.group === 'column') {
          return t._group !== 'column'
        } else {
          return t._group === 'column'
        }
      })

      choosedSet(props.category, [...other, ...choosed])

      const diff = choosed.filter(t => list.every(item => item.name !== t.name))
      indexCompare.update.dimensions(diff)
    }
  }, 20)
}

const onAddAssembly = () => {
  const nests = choosedGet(props.category)
    .filter(t => t.nested)
    .map(t => +t.displayName.substring(4))

  const nextIndex = getNextIndex(1, nests)

  choosedSet(
    props.category,
    props.dataSource.concat({
      _id: getRandomKey(),
      category: CATEGORY.FILTER,
      displayName: '组合过滤' + nextIndex,
      nested: true
    })
  )
}
</script>

<style lang="scss" scoped>
.section-list {
  position: relative;
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  background-color: #f9f9f9;
  border-radius: 4px;
  &:last-child {
    margin-bottom: 0;
  }

  // unsupported below safari 15.3
  &:has(.list-item):hover {
    .clear-button {
      opacity: 1;
      pointer-events: initial;
    }
  }

  // 兼容safari 15.3
  &:hover .clear-button {
    opacity: 1;
    pointer-events: initial;
  }

  aside {
    position: relative;
    width: 100px;
    padding-left: 8px;
    font-size: 13px;
    &:after {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      bottom: 0;
      width: 1px;
      background-color: #c9c9c9;
    }
  }
  main {
    flex: 1;
    min-height: 36px;
    max-height: 100px;
    overflow: auto;
    padding: 0 10px 6px 10px;
    overflow: auto;
    :deep(.tag) {
      margin-top: 6px;
    }
  }
}
.list {
  &.drag-ghost {
    &:after {
      content: '-';
      display: inline-block;
      vertical-align: bottom;
      min-width: 70px;
      height: 22px;
      margin-top: 6px;
      border-radius: 4px;
      background-color: #ccc;
      pointer-events: none;
      background-color: v-bind(tagColor);
      opacity: 0.15;
    }
  }
}
.multiple-icon {
  vertical-align: middle;
  font-size: 20px;
  cursor: pointer;
}

:deep(.multiple-trigger) {
  margin: 6px 6px 0 0;
  vertical-align: bottom;
}
.clear-button {
  position: absolute;
  right: 10px;
  bottom: 6px;
  opacity: 0;
  pointer-events: none;
}

.tag-ghost {
  background-color: v-bind(tagColor);
  opacity: 0.5;
}
</style>
