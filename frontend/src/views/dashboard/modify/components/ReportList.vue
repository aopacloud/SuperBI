<template>
  <section class="report-list-section">
    <header class="title">
      <div>
        我创建的图表
        <a-button
          size="small"
          type="text"
          :icon="h(ReloadOutlined)"
          @click="refresh"></a-button>
      </div>
      <a-input-search
        style="width: 200px"
        placeholder="请输入搜索"
        v-model:value="keyword" />
    </header>
    <header class="sub-title">
      所属数据集
      <a-select
        style="width: 200px"
        placeholder="请选择数据集"
        :loading="loading"
        v-model:value="datasetValue"
        @change="onDatasetChange">
        <a-select-option value="">全部</a-select-option>
        <a-select-option v-for="item in datasetList" :key="item.id" :value="item.id">
          {{ item.name }}
        </a-select-option>
      </a-select>
    </header>
    <main class="content flex-1">
      <a-spin :spinning="loading">
        <a-empty v-if="!list.length" style="margin-top: 126px" />
        <ul v-else style="margin: 0">
          <li class="item" v-for="item in list" :key="item.id">
            <div class="item-type">
              <TableOutlined v-if="item.reportType === 'table'" />
              <BarChartOutlined v-else-if="item.reportType === 'bar'" />
              <LineChartOutlined v-else-if="item.reportType === 'line'" />
              <PieChartOutlined v-else-if="item.reportType === 'pie'" />
              <FieldNumberOutlined v-else-if="item.reportType === 'statistic'" />
              <AreaChartOutlined v-else />
            </div>
            <div class="item-name">{{ item.name }}</div>
            <div class="item-action">
              <a-button
                size="small"
                type="text"
                class="act-btn"
                :disabled="disabled"
                :icon="h(PlusOutlined)"
                @click="itemClick(item)" />
            </div>
          </li>
        </ul>
      </a-spin>
    </main>
  </section>
</template>

<script setup>
import { computed, h, onMounted, ref, shallowRef } from 'vue'
import {
  ReloadOutlined,
  PlusOutlined,
  TableOutlined,
  BarChartOutlined,
  LineChartOutlined,
  PieChartOutlined,
  FieldNumberOutlined,
  AreaChartOutlined,
} from '@ant-design/icons-vue'
import useAppStore from '@/store/modules/app'
import userUserStore from '@/store/modules/user'
import { getReportList } from '@/apis/report'

const appStore = useAppStore()
const userStore = userUserStore()
const workspaceId = computed(() => appStore.workspaceId)

const emits = defineEmits(['click'])
const props = defineProps({
  // 已经存在的图表
  exited: {
    type: Array,
    default: () => [],
  },
  disabled: Boolean,
})

// 搜索关键字
const keyword = ref('')

// 数据集
const datasetList = shallowRef()
const datasetValue = ref()
// 根据图表所属的数据集生成数据集列表
const generatorDatasetList = (chartList = []) => {
  const datasetMap = chartList.reduce((acc, cur) => {
    const { datasetId, datasetName } = cur

    if (!acc[datasetId]) {
      acc[datasetId] = { id: datasetId, name: datasetName }
    }

    return acc
  }, {})

  datasetList.value = Object.values(datasetMap)
}

// 数据集切换
const onDatasetChange = e => {
  keyword.value = ''
}

// 图表
const loading = ref(false)
const allList = shallowRef([]) // 所有的图表
// 显示的列表
const list = computed(() => {
  // 已选中的排除
  const list = allList.value.filter(
    t => !props.exited.some(e => e.content.id === t.id)
  )
  // 关联数据集
  const list2 = !datasetValue.value
    ? list
    : list.filter(t => t.datasetId === datasetValue.value)

  return list2.filter(t => t.name.includes(keyword.value.trim()))
})

// 获取图表
const fetchReports = async () => {
  try {
    loading.value = true

    const { data } = await getReportList({
      workspaceId: workspaceId.value,
      creator: userStore.username,
      pageSize: 10000,
      sortField: 'updateTime',
      sortType: 'desc',
    })

    allList.value = data
    generatorDatasetList(data)
  } catch (error) {
    console.error('获取图表错误', error)
  } finally {
    loading.value = false
  }
}

const reportRenderTypeMap = {
  table: 'TableOutlined',
  bar: 'BarChartOutlined',
  line: 'LineChartOutlined',
  pie: 'PieChartOutlined',
  statistic: 'FieldNumberOutlined',
  default: 'AreaChartOutlined',
}
const getReportRenderType = type => {
  return reportRenderTypeMap[type] || reportRenderTypeMap['default']
}

const itemClick = e => {
  emits('click', { ...e })
}

const refresh = () => {
  fetchReports()
}

onMounted(() => {
  fetchReports()
})
</script>

<style lang="scss" scoped>
.report-list-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.title,
.sub-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  border-bottom: 1px solid #f0f0f0;
}
.content {
  flex: 1;
  overflow: auto;
  padding: 10px;
  background-color: #f0f2f5;
}
.item {
  display: flex;
  align-items: center;
  line-height: 34px;
  padding: 0 8px 0 12px;
  background-color: #fff;
  margin-bottom: 8px;
  border-radius: 4px;
  color: #666;
  font-size: 14px;
  &:hover {
    outline: 1px solid #1677ff;
  }

  &-name {
    flex: 1;
    margin: 0 12px;
    @extend .ellipsis;
  }

  .act-btn:not(:disabled) {
    color: #666;
  }
}
</style>
