<template>
  <a-space :size="2">
    <a-tooltip title="放大">
      <ZoomInOutlined class="icon-btn" @click="zoomIn" />
    </a-tooltip>
    <a-tooltip title="缩小">
      <ZoomOutOutlined class="icon-btn" @click="zoomOut" />
    </a-tooltip>
    <a-tooltip title="自适应">
      <BorderOuterOutlined class="icon-btn" @click="autoFit" />
    </a-tooltip>
    <a-tooltip :title="collapseText">
      <div class="icon-btn" @click="toggleCollapse">
        <DownSquareOutlined v-if="collapse" />
        <UpSquareOutlined v-else />
      </div>
    </a-tooltip>
  </a-space>
</template>

<script setup>
import {
  ZoomInOutlined,
  ZoomOutOutlined,
  BorderOuterOutlined,
  DownSquareOutlined,
  UpSquareOutlined,
} from '@ant-design/icons-vue'
import { computed } from 'vue'

const emits = defineEmits(['update:collapse'])
const props = defineProps({
  graph: {
    type: Object,
  },
  collapse: Boolean,
})

// 自适应
const autoFit = () => {
  if (!props.graph) return

  props.graph.zoomToFit({ maxScale: 1 })
  props.graph.centerContent()
}

// 放大
const zoomIn = () => {
  if (!props.graph) return

  props.graph.zoom(0.2)
}

// 缩小
const zoomOut = () => {
  if (!props.graph) return

  props.graph.zoom(-0.2)
}

const toggleCollapse = () => {
  emits('update:collapse', !props.collapse)
}
const collapseText = computed(() => (props.collapse ? '展开' : '收起'))
</script>

<style lang="scss" scoped>
.icon-btn {
  display: inline-block;
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  cursor: pointer;
  &:hover {
    background-color: rgba(0, 0, 0, 0.06);
  }
  &:active {
    background-color: rgba(0, 0, 0, 0.15);
  }
}
</style>
