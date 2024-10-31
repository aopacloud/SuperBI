<template>
  <section class="section">
    <h3 style="margin-top: 0">库表选择</h3>
    <main class="main">
      <a-spin :spinning="loading">
        <a-select
          class="mb-8"
          style="width: 100%"
          placeholder="请选择数据库"
          v-model:value="sourceValue"
          @change="onSourceChange"
        >
          <a-select-option
            v-for="item in sourcesList"
            :key="item.datasourceName"
          >
            {{ item.datasourceName }}
          </a-select-option>
        </a-select>

        <a-input-search
          class="mb-8"
          placeholder="输入表名模糊搜索"
          allow-clear
          v-model:value="keyword"
          @search="onKeywordSearch"
        />

        <a-empty v-if="list.length === 0" style="margin-top: 20px" />

        <div v-else class="list">
          <div
            class="item"
            v-for="item in list"
            :key="item.tableName"
            :disabled="selectedDisabled || item.disabled || unmovable(item)"
            :class="{
              active: isActive(item),
              disabled: selectedDisabled || unmovable(item)
            }"
            :title="item.tableName"
            @click="onClick(item)"
            @mousedown="e => onMousedown(e, item)"
          >
            {{ item.tableName }}
          </div>
        </div>
      </a-spin>
    </main>
  </section>
</template>

<script setup>
import { ref, computed, shallowRef, watch, inject, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import useAppStore from '@/store/modules/app'
import { getDatabase, getTableListByDatabaseItem } from '@/apis/metaData'
import { clearQuerys } from '@/common/utils/window'
import { MAX_TABLES_COUNT } from '../config'

const route = useRoute()
const appStore = useAppStore()
const workspaceId = computed(() => appStore.workspaceId)

const emits = defineEmits(['on-table-change'])

const props = defineProps({
  detail: {
    type: Object,
    default: () => ({})
  },

  // 当前组件接口参数需要依赖外层详情接口的返回值
  shouldMount: Boolean
})

const { selectedContent, graph } = inject('index')
const graphCore = computed(() => graph.get())

// 选中的表
const selectedTable = computed(() => selectedContent.get('tables') || [])
const selectedDisabled = computed(
  () => selectedTable.value.length >= MAX_TABLES_COUNT
)
watch(
  selectedTable,
  ts => {
    tableValues.value = ts.map(t => t.tableName)
  },
  { deep: true }
)

const reloadSelected = () => {
  tableValues.value = selectedTable.value.map(t => t.tableName)
}
defineExpose({ reloadSelected })

// const initConfig = computed(() => props.detail.config || {})
// const configContent = computed(() => initConfig.value?.content || { tables: [] })

const loading = ref(false)
// 数据源
const sourceValue = ref(route.query.db)
clearQuerys('db')

const sourceListLoaded = ref(false)
const sourcesList = shallowRef([])
let sourcePm = null
// 获取数据源
const fetchSource = async () => {
  try {
    sourceListLoaded.value = true
    loading.value = true

    const res = await getDatabase({ workspaceId: workspaceId.value })

    if (!res.length) return

    sourcesList.value = res

    return res
  } catch (error) {
    sourceListLoaded.value = false
    // if (fromDetail) {
    //   const { workspaceId, engine, datasourceName } = initConfig.value
    //   fetchTableList({ workspaceId, engine, datasourceName })
    // }
  } finally {
    loading.value = false
  }
}

// 数据源切换
const onSourceChange = async e => {
  const item = sourcesList.value.find(t => t.datasourceName === e)
  if (!item) return

  fetchTableList(item)
}

// 库无权限，表不可选
const tableEnabled = computed(() => {
  return sourcesList.value.some(t => t.datasourceName === sourceValue.value)
})

// 所有的表
const tableList = shallowRef([])
// 渲染表
const list = ref([])
const tableValues = ref(
  typeof route.query.tb === 'string' ? route.query.tb.split('|') : []
)
clearQuerys('tb')

const canClick = computed(() => !tableValues.value.length)

const isActive = item => {
  return tableValues.value.some(t => t === item.tableName)
}

const alias = Array.from({ length: MAX_TABLES_COUNT }).map(
  (_, i) => 't' + (i + 1)
)
const getNextAlias = () =>
  alias.find(t => !selectedTable.value.some(s => s.alias === t))

const onClick = item => {
  if (!canClick.value) return

  const newItem = { ...item, alias: getNextAlias() }
  graphCore.value.__initNode__(newItem).then(({ node }) => {
    node.position(30, 40)
  })
  emits('on-table-change', newItem)
  tableValues.value.push(newItem.tableName)
  selectedContent.addTable(newItem)
}

const unmovable = item => {
  // return tableValues.value.includes(item.tableName)
  return false
}

const onMousedown = (e, item) => {
  if (selectedDisabled.value) return // 最多只能选择3张表
  if (unmovable(item)) return // 已经选中的表不能拖动
  if (canClick.value) return

  const newItem = { ...item, alias: getNextAlias() }
  graphCore.value.__insertNode__(e, newItem, (source, target) => {
    tableValues.value.push(newItem.tableName)
    selectedContent.addTable(newItem, { source, target })
  })
}

/**
 * 获取表
 * @param {{workspaceId: number, engine: string, datasourceName: string}} payload 参数
 * @param {boolean} isChange
 */
const fetchTableList = async payload => {
  try {
    loading.value = true

    const res = await getTableListByDatabaseItem(payload)
    // 无表，使用编辑初始化的参数
    if (!res.length) return

    list.value = tableList.value = res.map(t => ({
      ...t,
      disabled: !tableEnabled.value
    }))

    tableValues.value = tableValues.value.length
      ? tableValues.value
      : selectedTable.value.map(t => t.tableName)
  } catch (error) {
    console.error('获取数据表错误', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  sourcePm = fetchSource()
})

watch(
  () => props.shouldMount,
  async mounted => {
    if (mounted) {
      sourcePm.then(() => {
        sourceValue.value =
          sourceValue.value ??
          (selectedTable.value.length
            ? selectedTable.value[0]?.dbName
            : sourcesList.value[0]?.dbName)

        tableValues.value = selectedTable.value.map(t => t.tableName)

        const item = sourcesList.value.find(
          t => t.datasourceName === sourceValue.value
        )
        item && fetchTableList(item)
      })
    }
  }
)

const keyword = ref('')
const onKeywordSearch = str => {
  str = str.trim()

  if (str.length === 0) {
    list.value = tableList.value
  } else {
    list.value = tableList.value.filter(t => t.tableName.includes(str))
  }
}
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
  padding: 12px 16px;
  height: 100%;
  background-color: #fff;
}
.main {
  flex: 1;
  overflow: auto;
  :deep(.ant-spin-nested-loading),
  :deep(.ant-spin-container) {
    height: 100%;
  }
  :deep(.ant-spin-container) {
    display: flex;
    flex-direction: column;
  }
}
.mb-8 {
  margin-bottom: 8px;
}
.list {
  flex: 1;
  overflow: auto;
  user-select: none;
}

.item {
  padding: 0px 8px;
  margin-bottom: 2px;
  border-radius: 2px;
  line-height: 26px;
  transition: background 0.2s;
  cursor: pointer;
  @extend .ellipsis;
  &.active {
    background-color: #e6f4ff;
    color: #1677ff;
  }
  &:not(.active):hover {
    background-color: rgba(0, 0, 0, 0.06);
  }
  &.disabled {
    cursor: not-allowed;
  }
}
</style>
