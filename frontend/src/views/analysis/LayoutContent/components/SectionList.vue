<template>
  <section class="section-list">
    <aside>{{ label }}</aside>

    <main @dragover="onDragover" @drop="onDrop">
      <draggable
        class="list"
        itemKey="_id"
        ghost-class="tag-ghost"
        :componentData="{
          name: 'fade',
          type: 'transition-group',
        }"
        :list="dataSource">
        <template #item="{ element, index }">
          <SectionTag
            class="list-item"
            :tag="element"
            :category="category"
            :color="tagColor"
            @remove="onRemove(index)">
            {{ element.displayName }}
          </SectionTag>
        </template>
      </draggable>

      <div v-if="dropPlaceholderVisible" class="tag-placeholder"></div>

      <!-- 字段多选 -->
      <MultipleSelectedPopover
        v-if="hasDatasetAnalysis"
        class="multiple-trigger"
        :data-source="datasetFields"
        :value="multipleValue"
        @ok="onMultipleOk" />
    </main>

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
import { h, ref, computed, watchEffect, onUnmounted, inject } from 'vue'
import { CloseOutlined } from '@ant-design/icons-vue'
import draggable from 'vuedraggable'
import MultipleSelectedPopover from '@/views/analysis/components/MultipleSelectedPopover.vue'
import SectionTag from './SectionTag.vue'
import { categoryMap } from '@/views/dataset/config.field'
import { getSectionListLabel } from '@/views/analysis/config'
import { CATEGORY } from '@/CONST.dict'
import { getRandomKey } from 'common/utils/help'
import { versionJs } from '@/versions'

const props = defineProps({
  category: {
    type: String,
    default: CATEGORY.PROPERTY,
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
})
const {
  dataset: datasetInfo,
  dragedField: indexDragedField,
  choosed: indexChoosed,
  options: indexOptions,
  permissions,
} = inject('index', {})

// 数据集分析权限
const hasDatasetAnalysis = computed(() => permissions.dataset.hasAnalysis())

const renderType = computed(() => {
  return indexOptions.get('renderType')
})

const multipleValue = computed(() => {
  const { category, dataSource } = props

  return category === CATEGORY.INDEX ? [] : dataSource.map(item => item.name)
})

const dataset = computed(() => {
  return datasetInfo.get()
})

// 原始字段
const datasetFields = computed(() => {
  const list =
    dataset.value.fields?.filter(
      item =>
        item.status !== 'HIDE' &&
        (item.category === props.category || props.category === CATEGORY.FILTER)
    ) || []

  // 筛选项需要确认dt字段作为必填筛选条件
  return props.category === CATEGORY.FILTER &&
    versionJs.ViewsAnalysis.dtInDatasetFields(dataset.value.fields)
    ? list.map(item => {
        return {
          ...item,
          disabled: versionJs.ViewsDatasetModify.isDt(item),
        }
      })
    : list
})

const label = computed(() => {
  const typeMap = getSectionListLabel(renderType.value)

  return typeMap(props.category)
})

const tagColor = computed(() => {
  return categoryMap[props.category]['color']
})

const onRemove = index => {
  props.dataSource.splice(index, 1)
}

const { clear: choosedClear, set: choosedSet, get: choosedGet } = indexChoosed
// 清除
const handleClear = () => {
  choosedClear(props.category)
}

const dropPlaceholderVisible = ref(false)
const onDragover = evt => {
  // evt.preventDefault() 才允许被拖入
  // 当前拖动的字段
  const draggingField = indexDragedField.getDragging()

  // 不可拖入同一字段的过滤条件
  if (CATEGORY.FILTER === props.category) {
    const choosedFilters = choosedGet(props.category)

    if (choosedFilters.some(t => t.name === draggingField.name)) {
      return false
    }
  }

  if (draggingField.category === CATEGORY.PROPERTY) {
    // 维度 能拖入 维度、指标、和筛选条件

    evt.preventDefault()
    indexDragedField.setDragover({
      category: props.category,
    })
  } else if (draggingField.category === CATEGORY.INDEX) {
    // 指标 只能拖入指标和过滤
    if (props.category === CATEGORY.INDEX || props.category === CATEGORY.FILTER) {
      evt.preventDefault()
      indexDragedField.setDragover({
        category: props.category,
      })
    }
  }
}
const onDrop = evt => {
  const payload = evt.dataTransfer.getData('dragging-field-data')

  if (!payload) return

  const item = JSON.parse(payload)

  if (props.category !== CATEGORY.FILTER) {
    const exitedIndex = props.dataSource.findIndex(t => t.id === item.id)

    if (exitedIndex > -1 && props.category === CATEGORY.PROPERTY) {
      props.dataSource.splice(exitedIndex, 1)
    }
  } else {
    // 自动展开过滤面板
    item.autoPopover = true
  }

  props.dataSource.push({ ...item, _id: getRandomKey() })
  // 清空拖拽覆盖字段
  indexDragedField.setDragover()
}

const dragoverWatcher = watchEffect(() => {
  // 当前划过的区域与当前拖动项的类别是否一致
  const dragoverFeild = indexDragedField.getDragover()

  dropPlaceholderVisible.value = dragoverFeild.category === props.category
})
onUnmounted(dragoverWatcher)

// 批量选中
const onMultipleOk = value => {
  // 延迟赋值，否则会有闪烁
  setTimeout(() => {
    const choosed = value.map(t => {
      const item = datasetFields.value.find(f => f.name === t)

      return { ...item, _id: getRandomKey() }
    })

    if (props.category === CATEGORY.INDEX) {
      choosedSet(props.category, props.dataSource.concat(choosed))
    } else {
      choosedSet(props.category, choosed)
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
.list-item {
  float: left;
}
.multiple-icon {
  vertical-align: middle;
  font-size: 20px;
  cursor: pointer;
}
.tag-placeholder {
  float: left;
  position: relative;
  display: inline-flex;
  min-width: 70px;
  height: 24px;
  margin: 6px 6px 0 0;
  border-radius: 4px;
  background-color: #ccc;
  pointer-events: none;
  background-color: v-bind(tagColor);
  opacity: 0.15;
}

:deep(.multiple-trigger) {
  margin-top: 6px;
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
