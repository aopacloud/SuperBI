<template>
  <section class="role-list-section flex-column">
    <header class="flex justify-between align-center">
      <a-input-search
        placeholder="请输入搜索"
        allow-clear
        v-model:value="keyword"
        @search="onKeywordSearch" />

      <a-button
        style="margin-left: 8px"
        type="primary"
        :icon="h(PlusOutlined)"
        @click="add">
        角色
      </a-button>
    </header>
    <main class="flex-1 scroll" style="margin-top: 10px">
      <a-spin :spinning="loading">
        <a-empty v-if="!list.length" style="margin-top: 50px" />
        <ul v-else>
          <li
            class="item"
            v-for="item in list"
            :key="item.key"
            :title="item.name"
            :class="{ active: item.id === activeId }"
            @click="onSelect(item)">
            {{ item.name || '-' }}
          </li>
        </ul>
      </a-spin>
    </main>
  </section>

  <!-- 新增、编辑 -->
  <ModifyModal
    v-model:open="modifyModelOpen"
    :allList="allList"
    :init-data="rowInfo"
    @ok="onModifyOk" />
</template>

<script setup>
import { computed, h, onMounted, ref } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getRoleList, deleteRole } from '@/apis/system/role'
import ModifyModal from './ModifyModal.vue'

const emits = defineEmits(['click'])

const keyword = ref('')
const onKeywordSearch = e => {
  if (!e.trim().length) fetchData()
}

const loading = ref(false)
const allList = ref([])

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true

    const data = await getRoleList()

    allList.value = data

    onSelect(data[0])
  } catch (error) {
    console.error('角色列表获取失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const list = computed(() => {
  const s = keyword.value.trim()

  if (!s.length) {
    return allList.value
  } else {
    return allList.value.filter((t, i) => t.name.includes(s))
  }
})

// 当前选中的id
const activeId = ref(-1)

// 选中
const onSelect = row => {
  if (!row) return

  activeId.value = row.id

  emits('click', { ...row })
}

const rowInfo = ref({})
const modifyModelOpen = ref(false)
const add = () => {
  rowInfo.value = {}
  modifyModelOpen.value = true
}
const onModifyOk = payload => {
  const item = allList.value.find(t => t.id === payload.id)
  if (!item) {
    allList.value.push(payload)

    onSelect(list.value.slice(-1)[0])
  } else {
    for (const key in payload) {
      item[key] = payload[key]
    }

    emits('click', item)
  }
}

// 编辑
const edit = info => {
  rowInfo.value = { ...info }
  modifyModelOpen.value = true
}

// 删除
const del = async id => {
  try {
    await deleteRole(id)

    const aIndex = allList.value.findIndex(t => t.id === id)
    allList.value.splice(aIndex, 1)

    if (!list.value.length) {
      keyword.value = ''
    }

    if (aIndex > allList.value.length - 1) {
      onSelect(allList.value.slice(-1)[0])
    } else {
      onSelect(allList.value[aIndex])
    }

    return Promise.resolve()
  } catch (error) {
    console.error('角色删除失败', error)
  }
}

defineExpose({ edit, del })
</script>

<style lang="scss" scoped>
.role-list-section {
  height: 100%;
}
.item {
  padding: 6px 12px;
  transition: all 0.2s;
  cursor: pointer;
  &:hover {
    background-color: #e6f7ff;
  }
  &.active {
    background-color: #bae7ff;
  }
}
</style>
