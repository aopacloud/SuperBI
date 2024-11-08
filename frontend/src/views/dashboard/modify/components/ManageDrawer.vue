<template>
  <a-drawer
    class="manage-drawer"
    title="报表内容管理"
    width="750"
    :open="open"
    :bodyStyle="{ display: 'flex', 'flex-direction': 'column', padding: 0 }"
    @close="close"
  >
    <div class="header">
      图表数量: {{ layout.length }} / {{ LAYOUT_MAX }}
      <a target="_blank" style="margin-left: auto" @click="toReport"
        >图表管理</a
      >
    </div>

    <div class="content">
      <div class="left" ref="layoutRef">
        <a-empty v-if="!layout.length" style="margin-top: 200px" />

        <grid-layout
          v-else
          class="layout"
          :col-num="colNum"
          :cols="{
            lg: colNum,
            md: colNum,
            sm: colNum,
            xs: colNum,
            xxs: colNum
          }"
          :row-height="rowHeight"
          :is-draggable="graggable"
          :verticalCompact="true"
          :margin="margin"
          v-model:layout="layout"
        >
          <grid-item
            class="layout-item"
            v-for="item in layout"
            :class="[`layout-item-${item.type}`]"
            :key="item.i"
            :i="item.i"
            :x="item.x"
            :y="item.y"
            :w="item.w"
            :h="item.h"
            :minW="colNum / 5"
            :minH="18 / transferRatio"
            :maxW="colNum"
            :maxH="(rowHeight / transferRatio) * 100"
            :isResizable="item.type === 'REPORT'"
            :resizeFrom="['top', 'right', 'bottom', 'left']"
          >
            <div
              class="item"
              :style="getItemStyle(item.type)"
              :title="displayItemName(item)"
              @mousedown.prevent
            >
              <div class="item-name">
                {{ displayItemName(item) }}
              </div>

              <a-dropdown trigger="click">
                <div class="item-right tool">
                  <DownOutlined />
                </div>
                <template #overlay>
                  <a-menu @click="e => onMenuClick(e, item)">
                    <template
                      v-if="item.type !== 'REMARK' && item.type !== 'FILTER'"
                    >
                      <a-menu-item key="size_large">调整为大图 </a-menu-item>
                      <a-menu-item key="size_middle">调整为中图 </a-menu-item>
                      <a-menu-item key="size_small">调整为小图 </a-menu-item>
                    </template>
                    <a-menu-item v-if="item.type === 'REMARK'" key="edit">
                      编辑
                    </a-menu-item>
                    <a-menu-item key="remove">移除</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </grid-item>
        </grid-layout>
      </div>
      <div class="right flex-column">
        <ReportList
          :exited="reportExited"
          :disabled="layout.length >= LAYOUT_MAX"
          @click="onReportListClick"
        />
      </div>
    </div>

    <template #footer>
      <a-button :icon="h(FileTextOutlined)" @click="addNote">
        添加标签</a-button
      >
      <a-space style="float: right">
        <a-button @click="close">关闭</a-button>
        <a-button type="primary" @click="ok">确认</a-button>
      </a-space>
    </template>
  </a-drawer>

  <!-- 便签 -->
  <RemarkModal
    v-model:open="noteOpen"
    :initial-value="noteInfo"
    @ok="onNoteOk"
  />
</template>

<script setup>
import { h, ref, computed, watch, nextTick, toRaw } from 'vue'
import { useRouter } from 'vue-router'
import { DownOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import RemarkModal from './RemarkModal.vue'
import { ManageLayoutOptions, LAYOUT_MAX } from '../config'
import ReportList from './ReportList.vue'
import { deepClone } from 'common/utils/help'

const router = useRouter()

const { margin, colNum, rowHeight, graggable, transferRatio } =
  ManageLayoutOptions
const getLayoutItemSize = (type, size = 'default') => {
  if (type !== 'width' && type !== 'height') {
    console.error('参数不正确')

    return
  }

  const SIZE_MAP = {
    width: {
      large: colNum,
      middle: colNum / 2,
      small: colNum / 4,
      default: colNum
    },
    height: {
      large: rowHeight * 20,
      middle: rowHeight * 20,
      small: rowHeight * 10,
      default: rowHeight * 20
    }
  }

  const sizes = SIZE_MAP[type]

  return sizes[size] ?? sizes['default']
}

const emits = defineEmits(['update:open', 'ok'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  dataSource: {
    type: Array,
    default: () => []
  }
})

const toReport = () => {
  const routeRes = router.resolve({ name: 'ReportList' })
  if (!routeRes) return

  window.open(routeRes.href, '_blank')
}

// 布局引用
const layoutRef = ref()
// 布局数据
const layout = ref([])
const reportExited = computed(() =>
  layout.value.filter(t => t.type === 'REPORT')
)

watch(
  () => props.open,
  open => {
    if (!open) return

    nextTick(() => layoutRef.value.scrollTo(0, 0))
    init()
  }
)

const isFilterOrRemark = item =>
  item.type === 'REMARK' || item.type === 'FILTER'

const init = () => {
  layout.value = props.dataSource.map(item => {
    const h = isFilterOrRemark(item)
      ? getLayoutItemSize('height', 'small')
      : item.h / transferRatio
    return {
      ...item,
      h: item.h / transferRatio,
      y: item.y / transferRatio,
      _oH: isFilterOrRemark(item) ? item.h : undefined
    }
  })
}

const getItemStyle = type => {
  const colorMap = {
    REMARK: '#fefcef',
    FILTER: '#e2e9ef'
  }

  return {
    backgroundColor: colorMap[type] ?? '#f6f8fa'
  }
}
const displayItemName = (item = {}) => {
  const { type, content } = item

  if (type === 'REMARK') {
    return content.title
  } else if (type === 'FILTER') {
    return '全局筛选项'
  } else {
    return content.name
  }
}

// 便签
const noteOpen = ref(false)
const noteInfo = ref({})
const addNote = () => {
  noteInfo.value = {}
  noteOpen.value = true
}
// 编辑便签
const onNodeEdit = row => {
  noteInfo.value = { ...row }
  noteOpen.value = true
}

// 便签编辑
const onNoteOk = payload => {
  const isAdd = Object.keys(noteInfo.value).length === 0

  // 新增便签
  if (isAdd) {
    const maxY = Math.max(...layout.value.map(t => t.y))
    const lastItem = layout.value.find(t => t.y === maxY) || {}
    const item = {
      type: 'REMARK',
      i: Date.now(),
      x: 0,
      y: maxY + (lastItem.h || 0),
      w: colNum,
      h: 7 / transferRatio, // getLayoutItemSize('height', 'small'),
      content: { ...payload }
    }

    layout.value.push(item)
  } else {
    // 编辑便签
    const item = layout.value.find(t => t.i === payload.i)

    delete payload.i
    item.content = { ...payload }
  }
}

const onItemRemove = i => {
  layout.value = layout.value.filter(t => t.i !== i)
}
const onMenuClick = ({ key }, item) => {
  switch (key) {
    case 'size_large':
      item.x = 0
      item.w = getLayoutItemSize('width', 'large')
      item.h = getLayoutItemSize('height', 'large')
      break
    case 'size_middle':
      item.w = getLayoutItemSize('width', 'middle')
      item.h = getLayoutItemSize('height', 'middle')
      break
    case 'size_small':
      item.w = getLayoutItemSize('width', 'small')
      item.h = getLayoutItemSize('height', 'small')
      break
    case 'edit':
      onNodeEdit({ i: item.i, ...item.content })
      break
    case 'remove':
      onItemRemove(item.i)
      break
    default:
      break
  }

  if (key.startsWith('size_')) {
    // item._oH = undefined
    layout.value = layout.value.map(t => t)
  }
}

const onReportListClick = payload => {
  const isStatistic = payload.reportType === 'statistic'
  const size = isStatistic ? 'small' : 'middle' // 指标卡小尺寸，其余中等尺寸
  const h = getLayoutItemSize('height', size)
  const w = getLayoutItemSize('width', size)

  const sorted = layout.value.sort((a, b) =>
    a.y === b.y ? a.x - b.x : a.y - b.y
  )

  let x = 0,
    y = 0
  const last = sorted.slice(-1)[0]
  if (last) {
    // 最后一个的行有剩余空间
    if (last.x + last.w + w <= colNum) {
      y = last.y
      x = last.x + last.w
    } else {
      // 否则下一行
      x = 0
      y = last.y + last.h
    }
  }

  const item = {
    type: 'REPORT',
    i: Date.now(),
    w: getLayoutItemSize('width', size),
    h,
    x,
    y,
    content: { ...payload }
  }

  layout.value.push(item)
  layout.value = layout.value.map(t => t)
}

const reset = () => {}
const close = () => {
  emits('update:open', false)
  reset()
}

const _resolvePayload = list =>
  list.map(item => {
    return {
      ...item,
      _id: item._id || item.i || Date.now(),
      h: item.h * transferRatio,
      y: item.y * transferRatio
    }
  })

const ok = () => {
  emits('ok', _resolvePayload(toRaw(layout.value)))
  close()
}
</script>

<style lang="scss" scoped>
$borderStyle: 1px solid #f0f0f0;
.header {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-bottom: $borderStyle;
}
.content {
  flex: 1;
  display: flex;
  overflow: hidden;
  & > .left,
  & > .right {
    flex: 1;
    overflow-x: hidden;
  }
}
.right {
  border-left: $borderStyle;
}

.layout {
  height: 100%;
}
.item {
  display: flex;
  height: inherit;
  align-items: center;
  padding-left: 6px;
  background-color: #f6f8fa;
  outline: 1px solid transparent;
  &.filter {
    background-color: #e2e9ef;
  }
  &.note {
    background-color: #fefcef;
  }
  .item-name {
    flex: 1;
    @extend .ellipsis;
  }
  .item-right {
    width: 20px;
    background-color: #f6f8fa;
  }
  .tool {
    display: inline-flex;
    justify-content: center;
    align-self: stretch;
    font-size: 12px;
    cursor: pointer;
    color: #666;
    &:hover {
      background-color: #bdddff;
    }
  }
}

.layout {
  .layout-item:hover {
    outline: 1px solid #1677ff;
  }
}
.layout-item:not(.vue-grid-placeholder):not(:hover) {
  background: #fff;
  :deep(.vue-resizable-handle),
  :deep(.vue-resizable-handle2) {
    opacity: 0;
  }
}
</style>
