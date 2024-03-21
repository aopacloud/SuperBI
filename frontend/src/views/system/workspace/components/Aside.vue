<template>
  <section class="section flex-column">
    <header class="header flex justigy-between align-center">
      <a-input-search
        placeholder="输入关键字搜索"
        allow-clear
        v-model:value="keyword"
        @search="oKeywordnSearch" />
      <a-button
        style="margin-left: 12px"
        type="primary"
        :icon="h(PlusOutlined)"
        @click="emits('create')">
        添加
      </a-button>
    </header>

    <main class="content">
      <a-empty v-if="!list.length" style="margin-top: 40%" />

      <a-spin :spinning="loading">
        <draggable
          class="list"
          itemKey="id"
          ghost-class="tag-ghost"
          :componentData="{
            name: 'fade',
            type: 'transition-group',
          }"
          :list="list"
          @start="onDragstart"
          @end="onDragend">
          <template #item="{ element }">
            <li
              class="item"
              :class="{ active: activeId === element.id }"
              @click="itemClick(element)">
              {{ element.name }}
            </li>
          </template>
        </draggable>
      </a-spin>
    </main>
  </section>
</template>

<script setup>
import { h, onMounted, ref, shallowRef } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import draggable from 'vuedraggable'
import { getManagableWorkspaceList, updateWorkspaceList } from '@/apis/workspace'
import { useRoute } from 'vue-router'
import { clearQuerys } from '@/common/utils/window'

const route = useRoute()

const emits = defineEmits(['updateAll', 'create', 'select'])

const keyword = ref('')
const oKeywordnSearch = e => {
  const s = e.trim()

  if (!s.length) {
    fetchData()
  } else {
    list.value = allList.value.filter(o => o.name.includes(s))
  }
}

const activeId = ref(+route.query.id)
clearQuerys('id')

const itemClick = row => {
  emits('select', { ...row })
  activeId.value = row.id
}

// 列表
const loading = ref(false)
const allList = shallowRef([])
const list = ref([])
// 获取列表数据
const fetchData = async () => {
  try {
    loading.value = true

    const res = await getManagableWorkspaceList()

    list.value = allList.value = res

    emits('updateAll', res)

    if (!res.length) {
      activeId.value = undefined
      emits('select', {})
    } else {
      const activeIndex = res.findIndex(o => o.id === activeId.value)

      if (activeIndex < 0) {
        activeId.value = res[0].id
        emits('select', res[0])
      } else {
        emits('select', res[activeIndex])
      }
    }
  } catch (error) {
    console.error('获取可管理的空间列表失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
defineExpose({ reload: fetchData })

// 开始拖拽的顺序
const dragStartSortObj = {}
const onDragstart = e => {
  dragStartSortObj.value = list.value.reduce((acc, cur) => {
    acc[cur.id] = cur.sort || 0

    return acc
  }, {})
}

// 重置排序
const resetSort = () => {
  list.value = list.value
    .map(t => {
      return {
        ...t,
        sort: dragStartSortObj.value[t.id] || 0,
      }
    })
    .sort((a, b) => a.sort - b.sort)
}

// 拖动结束，更新排序
const onDragend = async e => {
  try {
    const params = list.value.map((t, i) => ({ id: t.id, sort: i }))

    await updateWorkspaceList(params)
  } catch (error) {
    resetSort()
    console.error('空间列表排序失败', error)
  } finally {
    dragStartSortObj.value = {}
  }
}
</script>

<style lang="scss" scoped>
.section {
  height: 100%;
  padding: 16px;
}
.content {
  margin-top: 10px;
  flex: 1;
  overflow: auto;
}
.item {
  display: flex;
  align-items: center;
  height: 34px;
  padding: 6px 12px;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.88);
  &:hover {
    background-color: #eaf6fb;
  }
  &.active {
    background-color: #bae7ff;
  }
}
</style>
