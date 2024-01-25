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
          @change="onSourceChange">
          <a-select-option v-for="item in sourcesList" :key="item.datasourceName">
            {{ item.datasourceName }}
          </a-select-option>
        </a-select>

        <a-input-search
          class="mb-8"
          placeholder="输入表名模糊搜索"
          allow-clear
          v-model:value="keyword"
          @search="onKeywordSearch" />

        <a-empty v-if="list.length === 0" style="margin-top: 20px" />

        <a-radio-group
          v-else
          class="list flex flex-column"
          :value="tableValue"
          @change="onTableChane">
          <a-radio
            class="item"
            v-for="item in list"
            :disabled="item.disabled"
            :key="item.tableName"
            :value="item.tableName"
            :title="item.tableName">
            {{ item.tableName }}
          </a-radio>
        </a-radio-group>
      </a-spin>
    </main>
  </section>
</template>

<script setup>
import { ref, computed, shallowRef, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Modal } from 'ant-design-vue'
import useAppStore from '@/store/modules/app'
import { getDatabase, getTableListByDatabaseItem } from '@/apis/metaData'
import { clearQuerys } from '@/common/utils/window'

const route = useRoute()
const appStore = useAppStore()
const workspaceId = computed(() => appStore.workspaceId)

const emits = defineEmits(['on-table-change'])

const props = defineProps({
  detail: {
    type: Object,
    default: () => ({}),
  },
  // 当前组件接口参数需要依赖外层详情接口的返回值
  shouldMount: Boolean,
})

const initConfig = computed(() => props.detail.config || {})

const loading = ref(false)
// 数据源
const sourceValue = ref(route.query.db)
clearQuerys('db')

const sourcesList = shallowRef([])
// 获取数据源
const fetchSource = async (fromDetail = false) => {
  try {
    loading.value = true

    const res = await getDatabase({ workspaceId: workspaceId.value })
    if (!res.length) return

    sourcesList.value = res
    // 初始化参数 > initConfig > 默认第一个
    sourceValue.value =
      sourceValue.value ?? (initConfig.value.datasourceName || res[0].datasourceName)

    const payload = res.find(t => t.datasourceName === sourceValue.value)
    if (!payload && fromDetail) {
      // 无匹配的库，或编辑初始化
      const { workspaceId, engine, datasourceName } = initConfig.value

      fetchTableList({ workspaceId, engine, datasourceName })
    } else {
      fetchTableList(payload)
    }
  } catch (error) {
    if (fromDetail) {
      const { workspaceId, engine, datasourceName } = initConfig.value

      fetchTableList({ workspaceId, engine, datasourceName })
    }
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
const tableValue = ref(route.query.tb)
const hasRouteQuery = !!route.query.tb
clearQuerys('tb')

/**
 * 获取表
 * @param {{workspaceId: number, engine: string, datasourceName: string}} payload 参数
 * @param {boolean} isChange
 */
const fetchTableList = async payload => {
  try {
    loading.value = true

    const res = await getTableListByDatabaseItem(payload)

    list.value = tableList.value = res.map(t => ({ ...t, disabled: !tableEnabled.value }))

    // 无表，使用编辑初始化的参数
    if (!res.length) return

    tableValue.value = tableValue.value ?? initConfig.value.tableName
    if (!tableValue.value) return

    const _payload = res.find(t => t.tableName === tableValue.value)
    if (!_payload) return

    if (initConfig.value.tableName || hasRouteQuery) {
      emits('on-table-change', _payload, +hasRouteQuery)
    }
  } catch (error) {
    console.error('获取数据表错误', error)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.shouldMount,
  mounted => {
    if (mounted) {
      fetchSource(!!props.detail.id)
    }
  },
  { immediate: true }
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

const tableChanged = value => {
  const item = list.value.find(t => t.tableName === value)

  if (!item) return

  tableValue.value = item.tableName
  emits('on-table-change', item, 1)
}

const onTableChane = e => {
  if (!initConfig.value.tableName) {
    tableChanged(e.target.value)

    return
  }

  Modal.confirm({
    content: '目前只支持单表，请确认是否要替换当前数据源',
    okText: '确认',
    onOk() {
      tableChanged(e.target.value)
    },
  })
}
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
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
  display: flex !important;
}

.item {
  padding: 2px 4px;
  margin-bottom: 2px;
  border-radius: 2px;
  &:hover {
    background-color: #eff1f3;
  }
  :deep(.ant-radio) + span {
    flex: 1;
    @extend .ellipsis;
  }
}
</style>
