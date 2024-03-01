<template>
  <div ref="rootRef" class="virtual-wrapper" v-resize="init" @scroll="onScroll">
    <div class="virtual-scroll" :style="{ height: totalHeight + 'px' }">
      <div
        class="virtual-list"
        :style="{
          transform: `translateY(${startIndex * itemHeight}px)`,
        }">
        <div
          class="virtual-item"
          v-for="(item, index) in list"
          :key="item[rowKey]"
          :data-index="index"
          :data-origin-index="startIndex + index"
          :style="{ height: itemHeight + 'px', lineHeight: itemHeight + 'px' }">
          <slot
            name="item"
            v-bind="{ list, item: toRaw(item), index, $index: startIndex + index }"
            >{{ item }}</slot
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, toRaw, onMounted } from 'vue'

defineOptions({
  inheritAttrs: false,
})

// defineOptions({ inheritAttrs: false })
const props = defineProps({
  dataSource: {
    type: Array,
    default: () => [],
  },
  rowKey: {
    type: String,
    default: 'id',
  },
  itemHeight: {
    type: Number,
    default: 30,
  },
  keep: {
    type: Number,
    default: 20,
  },
})

// 显示起始索引
const startIndex = ref(0)
// 显示结束索引
const endIndex = ref(0)
// 显示个数补偿（显示个数不足盛满容器）
const keepBuffer = ref(0)

const list = computed(() => {
  return props.dataSource.slice(
    startIndex.value,
    endIndex.value || props.keep + keepBuffer.value
  )
})

const reset = () => {
  startIndex.value = 0
  endIndex.value = 0
  keepBuffer.value = 0

  rootRef.value.scrollTo(0, 0)
}

// 容器总高度
const totalHeight = computed(() => {
  return props.dataSource.length * props.itemHeight
})

const rootRef = ref(null)
const init = () => {
  reset()

  while (
    props.dataSource.length > list.value.length &&
    list.value.length * props.itemHeight < rootRef.value.clientHeight
  ) {
    keepBuffer.value += props.keep
  }
}

const onScroll = e => {
  const curentItem = e.target.scrollTop / props.itemHeight

  startIndex.value = Math.floor(curentItem)
  endIndex.value = Math.ceil(curentItem) + props.keep + keepBuffer.value
}

onMounted(init)
</script>

<style scoped>
.virtual-wrapper {
  height: 100%;
  min-height: 200px;
  overflow: auto;
}
</style>
