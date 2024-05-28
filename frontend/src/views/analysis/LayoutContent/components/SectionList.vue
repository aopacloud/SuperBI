<template>
  <section class="section-list">
    <aside>{{ label }}</aside>

    <draggable
      class="list"
      tag="main"
      itemKey="_id"
      ghost-class="tag-ghost"
      :list="dataSource"
      @dragover.native="onDragover"
      @dragleave.native="onDragleave"
      @drop.native="onDrop"
      @start="onStart">
      <template #item="{ element }">
        <SectionTag
          v-if="isTagVisible(element)"
          class="list-item"
          :dataset="dataset"
          :tag="element"
          :category="category"
          :color="tagColor"
          @remove="onRemove(element)">
          {{ element.displayName }}
        </SectionTag>
      </template>

      <template #footer>
        <!-- 字段多选 -->
        <MultipleSelectedPopover
          #footer
          v-if="hasDatasetAnalysis"
          class="multiple-trigger"
          :data-source="datasetFields"
          :value="multipleValue"
          @ok="onMultipleOk" />
      </template>
    </draggable>

    <!-- 清除选中 -->
    <a-button
      v-if="hasDatasetAnalysis"
      size="small"
      class="clear-button"
      :icon="h(CloseOutlined)"
      @click="handleClear"></a-button>
  </section>
</template>

<script setup>
import { h, computed, inject } from 'vue'
import { CloseOutlined } from '@ant-design/icons-vue'
import draggable from 'vuedraggable'
import MultipleSelectedPopover from '@/views/analysis/components/MultipleSelectedPopover.vue'
import SectionTag from './SectionTag.vue'
import { categoryMap } from '@/views/dataset/config.field'
import { getSectionListLabel } from '@/views/analysis/config'
import { CATEGORY } from '@/CONST.dict'
import { getRandomKey } from 'common/utils/string'

const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({}),
  },
  category: {
    type: String,
    default: CATEGORY.PROPERTY,
  },
  group: {
    type: String,
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
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
  permissions,
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
      disabled: force && filters.some(t => t.name === item.name),
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

// 默认不允许被拖入， 需阻止默认行为 evt.preventDefault()
const onDragover = evt => {
  if (!draggingField.value) return

  const dragging = draggingField.value

  // 内部拖拽只能在相同分组中
  if (dragging._fromInner) {
    if (props.category === dragging.category) {
      evt.target.classList.add('drag-ghost')
      evt.preventDefault()
    }

    return
  }

  // 不可拖入同一字段的过滤条件
  if (props.category === CATEGORY.FILTER) {
    const choosedFilters = choosedGet(props.category)

    if (choosedFilters.some(t => t.name === dragging.name)) {
      return false
    } else {
      evt.target.classList.add('drag-ghost')
      evt.preventDefault()
    }
  } else if (dragging.category === CATEGORY.PROPERTY) {
    evt.target.classList.add('drag-ghost')
    evt.preventDefault()
  } else if (dragging.category === CATEGORY.INDEX) {
    // 指标 只能拖入指标和过滤
    if (props.category === CATEGORY.INDEX || props.category === CATEGORY.FILTER) {
      evt.target.classList.add('drag-ghost')
      evt.preventDefault()
    } else {
      return false
    }
  }
}

const onDragleave = e => {
  e.target.classList.remove('drag-ghost')
}

const onDrop = evt => {
  if (!draggingField.value) return

  evt.target.classList.remove('drag-ghost')

  const item = draggingField.value

  // 内部拖拽只能在相同分组中
  if (item._fromInner && item.category !== props.category) return

  if (props.category === CATEGORY.FILTER) {
    item._group = props.group
  } else {
    if (props.category === CATEGORY.PROPERTY) {
      item._group = props.group

      const exitedIndex = props.dataSource.findIndex(t => t.name === item.name)
      if (exitedIndex > -1) {
        if (item._fromInner) {
          props.dataSource.splice(exitedIndex, 1)
          props.dataSource.push(item)
        } else {
          props.dataSource.splice(exitedIndex, 1)
        }
      }
    }
  }

  if (!item._fromInner) {
    props.dataSource.push({ ...item, _id: getRandomKey() })
  }

  indexCompare.update.dimensions(item)
  indexDraggingField.set()
}

const onStart = ({ item }) => {
  // 目前只有维度分组见可以相互拖动(行分组、列分组)
  if (props.category !== CATEGORY.PROPERTY) return

  const old = props.dataSource.find(t => t._id === item.dataset.id)
  indexDraggingField.set({ ...old, _fromInner: true })
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
