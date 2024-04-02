<template>
  <section class="section">
    <header class="header">
      <h3 style="margin: 0">查询条件</h3>
      <PlusOutlined
        class="pointer"
        style="padding: 2px; margin-left: 6px"
        @click="add" />
    </header>

    <main class="list">
      <draggable
        itemKey="id"
        :componentData="{
          name: 'fade',
          type: 'transition-group',
        }"
        :options="{ handle: '.handler' }"
        :list="list">
        <template #item="{ element, index }">
          <div class="item" :class="{ active: activeIndex === index }">
            <UnorderedListOutlined class="handler" />

            <input
              type="text"
              class="item-name"
              placeholder="请输入查询名称"
              :class="{ editting: element._editting }"
              :readonly="!element._editting"
              :ref="e => (inputRefs[element.id] = e)"
              v-model="element.name"
              @dblclick="onItemDbclick(element, index)"
              @click="onItemclick(index)"
              @blur="onItemBlur(element, index)" />

            <a-dropdown
              trigger="click"
              placement="bottomRight"
              :getPopupContainer="node => node.parentNode">
              <EllipsisOutlined class="condition--icon-more" />
              <template #overlay>
                <a-menu @click="e => onMenuClick(e, index, element)">
                  <a-menu-item style="padding: 2px 6px" key="edit">
                    <EditOutlined /> 编辑
                  </a-menu-item>
                  <a-menu-item style="padding: 2px 6px" key="delete">
                    <DeleteOutlined /> 删除
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </template>
      </draggable>
    </main>
  </section>
</template>

<script setup>
import { ref, nextTick, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  UnorderedListOutlined,
  EllipsisOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import draggable from 'vuedraggable'
import { defaultFilterItem } from '../config'

const emits = defineEmits(['select'])
const getId = () => Date.now()

const props = defineProps({
  dataSource: {
    type: Array,
    default: () => [],
  },
})

// 筛选项列表
const list = ref([])
watchEffect(() => {
  list.value = props.dataSource.map(t => {
    const { charts = {}, ...res } = t

    return {
      ...res,
      charts,
    }
  })

  nextTick(() => {
    if (list.value.length) {
      onItemclick(0)
    }
  })
})

// 当前选中索引
const activeIndex = ref(-1)
// 当前选中项
// const activeItem = ref({})

// 双击
const onItemDbclick = (item, index) => {
  item._editting = true

  onItemclick(index)
  setInputSelect(item.id)
}

// 单击
const onItemclick = index => {
  const item = list.value[index] || {}

  activeIndex.value = index
  emits('select', item)
}

onMounted(() => {
  if (list.value.length > 0) {
    onItemclick(0)
  }
})

// 失焦
const onItemBlur = (item, index) => {
  if (item.name.trim().length === 0) {
    message.warn('查询条件名称不能为空')
  }

  item._editting = false
}

const onMenuClick = ({ key }, index, item) => {
  if (key === 'edit') {
    onItemDbclick(item, index)
  } else if (key === 'delete') {
    remove(index)
  }
}
const inputRefs = ref({})
const setInputSelect = id => {
  nextTick(() => {
    const input = inputRefs.value[id]

    input && input.select()
  })
}

const add = () => {
  const id = getId()
  list.value.push({
    id,
    name: '',
    _editting: true,
    ...defaultFilterItem,
  })

  onItemclick(list.value.length - 1)
  setInputSelect(id)
}

const remove = i => {
  list.value.splice(i, 1)

  if (list.value.length === 0) {
    onItemclick(-1)

    return
  }

  if (activeIndex.value > list.value.length - 1) {
    const nextIndex = list.value.length - 1

    onItemclick(nextIndex)
  } else {
    onItemclick(activeIndex.value)
  }
}

// 检验
const validate = () => {
  // 筛选项名称必填
  const _unValidateName = t => t.name.trim().length === 0
  // 必须关联图表
  const _unValidateCharts = t => Object.keys(t.charts).length === 0
  // 关联图表必须选择筛选字段
  const _unValidateChartsField = t => {
    return Object.keys(t.charts).some(id => {
      const chart = t.charts[id]

      return typeof chart.fieldName === 'undefined'
    })
  }

  if (
    list.value.some(item => {
      if (_unValidateName(item)) {
        message.warn('查询条件名称不能为空')

        return true
      } else if (_unValidateCharts(item)) {
        message.warn('查询条件必须关键图表')

        return true
      } else if (_unValidateChartsField(item)) {
        message.warn('关联图表必须选择筛选项')

        return true
      } else {
        return false
      }
    })
  ) {
    return false
  }

  return true
}

// 获取筛选项数据
const getData = () => {
  return toRaw(list.value)
}

// 更新列表
const update = (item = {}) => {
  const index = list.value.findIndex(t => t.id === item.id)

  list.value[index] = item
}

defineExpose({ update, validate, getData })
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
}
.header {
  display: flex;
  margin-bottom: 16px;
}
.list {
  flex: 1;
  line-height: 1.5;
  padding-right: 16px;
  overflow: auto;
}
.item {
  display: flex;
  align-items: center;
  padding: 0 8px 0 4px;
  margin-bottom: 5px;

  &.active {
    background-color: #e6f7ff;
  }

  &-name {
    flex: 1;
    line-height: inherit;
    margin: 0 10px 0 6px;
    background-color: transparent;
    border: none;
    border-bottom: 1px solid transparent;
    transition: all 0.15s;
    font-size: 14px;
    outline: none;
    text-overflow: ellipsis;
    cursor: pointer;
    &.editting {
      border-bottom-color: #1790ff;
    }
  }
}
</style>
