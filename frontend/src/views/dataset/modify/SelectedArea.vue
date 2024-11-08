<template>
  <div class="selected-area" :class="{ showAlias: selectedTable.length > 1 }">
    <a-empty
      v-if="!selectedTable.length"
      class="placeholder"
      :image="placeholderImg"
      :image-style="{
        height: '45px'
      }"
    >
      <template #description>
        <span class="description font-help">请从左侧点击添加数据表</span>
      </template>
    </a-empty>

    <div
      v-if="selectedTable.length === 1"
      class="placeholder"
      style="margin-top: 40px"
    >
      <div class="imgs" style="opacity: 0.6">
        <img :src="tImg" alt="表" width="45" />
        <img :src="arrowImg" alt="关联" width="16" />
        <img :src="tImg" alt="表" width="45" />
      </div>
      <div class="description font-help" style="margin-top: 14px">
        可拖入进行表关联
      </div>
    </div>

    <Graph @joinBtnClick="onJoinBtnClick" />

    <RelationModal
      :dataset="detail"
      :source="source"
      :target="target"
      :joinDescriptor="joinDescriptor"
      v-model:open="relationOpen"
      @ok="onOk"
    />
  </div>
</template>

<script setup>
import { ref, computed, inject, nextTick } from 'vue'
import Graph from './components/graph/graph.vue'
import RelationModal from './components/relationModal/index.vue'
import placeholderImg from './images/placeholder.png'
import tImg from './images/t.svg'
import arrowImg from './images/arrow.png'

const emits = defineEmits(['change'])
const props = defineProps({
  detail: {
    type: Object,
    default: () => ({})
  }
})

const { selectedContent } = inject('index')

const selected = computed(() => selectedContent.get())
const selectedTable = computed(() => selected.value.tables || [])
const selectedJoinDescriptors = computed(() => selected.value.joinDescriptors)

const relationOpen = ref(false)
// 左边表
const source = ref(null)
// 右边表
const target = ref(null)
// 关联关系
const joinDescriptor = ref({})

const onJoinBtnClick = (s, t) => {
  source.value = selectedTable.value.find(n => n.alias == s.id)
  target.value = selectedTable.value.find(n => n.alias == t.id)

  joinDescriptor.value =
    selectedJoinDescriptors.value.find(
      j => j.sourceAlias === s.id && j.targetAlias === t.id
    ) || {}

  relationOpen.value = true
}

const onOk = e => {
  const { source, target, joinDescriptor } = e

  selectedContent.updateTable(source)
  selectedContent.updateTable(target)
  selectedContent.updateJoinDescriptor(joinDescriptor)

  setTimeout(() => {
    emits('change')
  }, 100)
}
</script>

<style lang="scss" scoped>
.selected-area {
  position: relative;
  overflow: hidden;
  &:not(.showAlias) {
    :deep(.t-alias) {
      display: none;
    }
  }
}
.placeholder {
  position: absolute;
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .imgs {
    display: inline-flex;
    align-items: center;
    gap: 16px;
  }
}
</style>
