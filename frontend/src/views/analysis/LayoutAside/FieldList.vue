<template>
  <section class="field-list">
    <header class="field-list-header">
      <span class="label">{{ title }}</span>
      <a-input-search
        v-if="hasDatasetAnalysis"
        class="tools"
        size="small"
        allow-clear
        :placeholder="`搜索${title}`"
        v-model:value="keyword" />
    </header>

    <main class="field-list-main">
      <a-empty v-if="!list.length" :image="simpleImage" />
      <ul v-else class="list">
        <li
          class="item"
          v-for="(item, index) in list"
          :class="{ disabled: !hasDatasetAnalysis }"
          :key="index"
          :title="item.displayName + '(' + item.name + ')'"
          :draggable="hasDatasetAnalysis"
          @dragstart="onDragstart($event, item)"
          @dragend="onDragend($event, item)"
          @dblclick="onDblclick(item)">
          <i
            :class="['iconfont', getFieldTypeIcon(item.dataType)['icon']]"
            :style="{
              marginRight: '4px',
              color: getFieldTypeIcon(item.dataType)['color'],
            }">
          </i>
          <div class="item-name">
            {{ item.displayName }}
            <span class="item-help-text">({{ item.name }})</span>
          </div>
        </li>
      </ul>
    </main>
  </section>
</template>

<script setup lang="jsx">
import { shallowRef, ref, watch, watchEffect, inject, onUnmounted } from 'vue'
import { Empty } from 'ant-design-vue'
import { CATEGORY } from '@/CONST.dict.js'
import { getFieldTypeIcon } from '@/views/dataset/config.field'

const simpleImage = shallowRef(Empty.PRESENTED_IMAGE_SIMPLE)

const props = defineProps({
  title: {
    type: String,
    default: '维度',
  },
  category: {
    type: String,
    default: CATEGORY.PROPERTY,
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
  hasDatasetAnalysis: Boolean,
})

const list = shallowRef([]) // 显示数据
const initWatcher = watchEffect(() => {
  list.value = props.dataSource
})
onUnmounted(initWatcher)

const keyword = ref('')
watch(keyword, kw => {
  const s = kw.trim()

  if (!s.length) {
    list.value = props.dataSource
  } else {
    list.value = props.dataSource.filter(
      t => t.displayName.includes(s) || t.name.includes(s)
    )
  }
})

const emits = defineEmits(['dbclick'])
const onDblclick = row => {
  emits('dbclick', { ...row })
}

const indexInject = inject('index')
const indexDragedField = indexInject.dragedField
const onDragstart = (evt, row) => {
  const item = { ...row }

  evt.dataTransfer.setData('dragging-field-data', JSON.stringify(item))
  evt.dataTransfer.setDragImage(evt.target, 0, 0)
  // 设置拖拽中字段
  indexDragedField.setDragging(item)
}
const onDragend = () => {
  // 清空拖拽中字段， 清空拖拽覆盖字段
  indexDragedField.setDragging()
  indexDragedField.setDragover()
}
</script>

<style lang="scss" scoped>
.field-list {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.field-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 5px 0;
  padding: 0 10px;

  .label {
    font-size: 13px;
    font-weight: 600;
  }
  .tools {
    flex: 1;
    margin-left: 30px;
  }
}

.field-list-main {
  flex: 1;
  overflow: hidden;
  .list {
    height: 100%;
    overflow: auto;
    margin: 0;
    color: #606060;
    font-size: 13px;
  }
  .item {
    display: flex;
    width: 100%;
    align-items: center;
    height: 28px;
    padding: 0 10px;
    cursor: move;
    transition: all 0.2s;
    &.disabled {
      background-color: rgba(221, 221, 221, 0.3);
      cursor: not-allowed;
    }
    &:not(.disabled):hover {
      background-color: #ecf5ff;
    }
    &-name {
      flex: 1;
      @extend .ellipsis;
    }
    &-help-text {
      color: #aaa;
    }
  }
}
</style>
