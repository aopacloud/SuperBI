<template>
  <section class="aside">
    <a-input-search placeholder="请输入图表过滤" allow-clear v-model:value="keyword" />

    <main class="list">
      <div v-if="list.length" @click="itemClick">
        <a
          class="item"
          v-for="item in list"
          :key="item.cId"
          :title="item.name"
          :data-boxid="`box_${item.reportId}`"
          >{{ item.content.name }}
        </a>
      </div>
      <a class="item disabled" v-else>无图表</a>
    </main>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  dataSource: {
    type: Array,
    default: () => [],
  },
})

const keyword = ref('')

const sort = (a, b) => {
  if (a.y === b.y) return a.x - b.x

  return a.y - b.y
}

const list = computed(() => {
  const str = keyword.value.trim()
  const _list = props.dataSource.filter(t => t.type === 'REPORT')

  if (!str.length) return _list.sort(sort)

  return _list.filter(c => c.content.name.includes(str)).sort(sort)
})

const itemClick = e => {
  const srcElement = e.srcElement || e.target

  if (srcElement.tagName !== 'A') return

  const element = document.querySelector(`#${srcElement.dataset.boxid}`)

  if (element) {
    element.scrollIntoView({
      behavior: 'smooth',
    })
  }
}
</script>

<style scoped lang="scss">
.aside {
  height: 100%;
  display: flex;
  flex-direction: column;
  width: 260px;
  padding: 10px 15px 15px;
  background-color: #f4f7fa;
  transition: all 0.2s;
}

.input {
  :deep(.ant-input) {
    border: none;
    box-shadow: none;
  }
}
.list {
  overflow: auto;
  margin: 10px -15px 0 0;
  padding: 0 15px 0 0;
}
.item {
  @extend .ellipsis;
  display: block;
  padding: 2px 4px;
  color: #555;
  font-size: 14px;
  cursor: pointer;
  &.disabled {
    color: #666;
    cursor: not-allowed;
  }
  &:not(.disabled):hover {
    background-color: #e4f2ff;
    color: #1890ff;
  }
}
</style>
