﻿<template>
  <div class="tools" :class="{ disabled: !hasDatasetAnalysis }">
    <p-a-space style="font-size: 14px">
      <div class="btn run" :class="{ disabled: !hasDatasetAnalysis }" @click="run">
        <LoadingOutlined v-if="runLoading" />
        <PlayCircleTwoTone
          v-else
          :two-tone-color="hasDatasetAnalysis ? '#1990ff' : '#ccc'" />
      </div>

      <div
        style="margin-left: 12px"
        class="btn"
        :class="{ disabled: topNDisabled, active: topNActived }"
        @click="topN">
        topN
      </div>

      <div
        class="btn"
        :class="{ disabled: basisRatioDisabled, active: basisRatioActived }"
        @click="basisRatio">
        同环比
      </div>
    </p-a-space>

    <p-a-space style="font-size: 14px">
      <div class="query-info">
        耗时 <b>{{ responseInfo.elapsed }}</b> S; 查询
        <b>{{ responseInfo.total }}</b> 行; 展示前
        <div class="custom-select small">
          <select
            style="width: 80px; margin: 0 2px; padding-left: 4px; text-align: center"
            v-model="queryTotal"
            @change="onQueryTotalChange">
            <option
              v-for="opt in queryTotalOptions"
              :key="opt.value"
              :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        条
      </div>

      <DownloadOutlined
        class="btn icon"
        :class="{ disabled: doanloadDisabled }"
        @click="handleDownload" />

      <HistoryOutlined
        class="btn icon"
        :class="{ disabled: !hasDatasetAnalysis, active: isHistoryMode }"
        @click="toggleHistory()" />
    </p-a-space>
  </div>
</template>

<script setup>
import { ref, reactive, computed, inject } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlayCircleTwoTone,
  DownloadOutlined,
  HistoryOutlined,
  LoadingOutlined,
} from '@ant-design/icons-vue'
import { CATEGORY } from '@/CONST.dict'
import { IS_NOT_NULL, IS_NULL, isTime_HHMMSS } from '@/views/dataset/config.field'
import { postAnalysisQuery } from '@/apis/analysis'
import { repeatIndex, sortDimension } from '@/views/analysis/utils'
import { getByIncludesKeys } from 'common/utils/help'
import dayjs from 'dayjs'
import { versionJs } from '@/versions'
import { toContrastFiled, queryTotalOptions } from '@/views/analysis/config'
import {
  getStartDateStr,
  getEndDateStr,
} from '@/common/components/DatePickers/utils'
import { isRenderTable } from '../utils'
import { isDateField } from '@/views/dataset/utils'

const emits = defineEmits([
  'run-loading',
  'topN',
  'download',
  'toggle-history',
  'basisRatio',
  'querySuccess',
])

const {
  timeOffset,
  dataset: indexDataset,
  choosed: indexChoosed,
  options: indexOptions,
  compare: indexCompare,
  paging: indexPaging,
  topN: indexTopN,
  requestResponse: indexRequestResponse,
  permissions,
  dashboardFilters: indexDashboardFilters,
} = inject('index', {})

// 数据集分析权限
const hasDatasetAnalysis = computed(() => permissions.dataset.hasAnalysis())

const dataset = computed(() => indexDataset.get())

// topN
const topNDisabled = computed(() => {
  return indexChoosed.get(CATEGORY.INDEX).length === 0
})
const topNActived = computed(() => {
  const topN = indexTopN.get()

  return !!topN.sortField
})
const topN = () => {
  if (topNDisabled.value) return

  emits('topN')
}

// 同环比
const basisRatioDisabled = computed(() => {
  const p = indexChoosed.get(CATEGORY.PROPERTY)
  const i = indexChoosed.get(CATEGORY.INDEX)

  return !p.some(toContrastFiled) || i.length === 0
})
const basisRatioActived = computed(() => {
  const compare = indexCompare.get() || {}

  return compare.measures?.length > 0
})
const basisRatio = () => {
  if (basisRatioDisabled.value) return

  emits('basisRatio')
}

const doanloadDisabled = computed(() => {
  const res = indexRequestResponse.get('response')

  return res.status !== 'SUCCESS'
})

// 查询条数配置
const queryTotal = computed({
  get() {
    return indexPaging.get('limit')
  },
  set(val) {
    indexPaging.set('limit', val)
  },
})
const onQueryTotalChange = () => {
  run()
}

// 历史记录
const isHistoryMode = ref(false)
const handleDownload = () => {
  if (doanloadDisabled.value) return

  emits('download')
}
const toggleHistory = m => {
  if (!hasDatasetAnalysis.value) return

  const mode = m ?? !isHistoryMode.value

  isHistoryMode.value = mode
  emits('toggle-history', mode)
}

const runLoading = ref(false)
// 查询响应信息
const responseInfo = reactive({
  elapsed: 0,
  total: 0,
})

/**
 * 处理日期过滤
 * 不分动态日期的过滤需要重新计算值
 */
const transferChoosedFilters = filterItem => {
  const {
    dataType,
    conditions: [cond1],
  } = filterItem

  if (!isDateField(filterItem)) return filterItem

  const { timeType, args, timeParts, _this, _until } = cond1

  // 自某日至*，需要将动态时间转换为静态时间
  if (_until) {
    args[1] = getEndDateStr({ type: 'day', offset: _until.split('_')[1] })
  } else if (timeType === 'RELATIVE' && _this) {
    // 相对时间的本周、本月、上周、上月 再查询时需重新计算
    const [tp, of = 0] = _this.split('_')
    const sDate = getStartDateStr(
      { type: tp.toLowerCase(), offset: +of },
      timeOffset.value
    )
    const endDate = getEndDateStr(
      { type: tp.toLowerCase(), offset: +of },
      timeOffset.value
    )

    args[0] = dayjs().startOf('day').diff(sDate, 'day')
    args[1] = dayjs().endOf('day').diff(endDate, 'day')
  }
  return {
    ...filterItem,
    conditions: [
      {
        ...cond1,
        timeType: !!_until ? 'EXACT' : timeType,
        timeParts: isTime_HHMMSS(dataType) ? timeParts : undefined,
        args,
      },
    ],
  }
}

const mergeDashbaordFilters = (filters = []) => {
  const choosedFilters = [...filters].map(transferChoosedFilters)

  const dashboardFilters = indexDashboardFilters.get().filters
  if (!dashboardFilters || !dashboardFilters.length) return choosedFilters

  // 看板筛选dt
  const dashboardDt = dashboardFilters.find(versionJs.ViewsDatasetModify.isDt)
  // 选中的dt
  const choosedDt = choosedFilters.find(versionJs.ViewsDatasetModify.isDt)

  // 日期字段过滤未修改
  if (choosedDt && !choosedDt._dtChanged && dashboardDt) {
    const {
      conditions: [cond1],
    } = dashboardDt

    const {
      conditions: [cond2],
    } = choosedDt

    const { useLatestPartitionValue, timeType, args, _until } = cond1

    cond2.timeType = timeType
    cond2.useLatestPartitionValue = useLatestPartitionValue

    if (useLatestPartitionValue) {
      cond2.args = []
    } else {
      cond2.args = [...args]
      // 自某日至*，需要将动态时间转换为静态时间
      if (_until) {
        cond2.args[1] = getEndDateStr({ type: 'day', offset: _until.split('_')[1] })
      }
    }
  }

  return [
    ...choosedFilters,
    ...dashboardFilters
      .filter(t => t.name !== versionJs.ViewsDatasetModify.dtFieldName)
      .map(t => ({ ...t, _from: 'dashboard' })),
  ]
}

// 渲染类型
const renderType = computed(() => indexOptions.get('renderType'))

const run = async from => {
  if (!hasDatasetAnalysis.value) return

  if (runLoading.value) return

  try {
    toggleHistory(false)

    runLoading.value = true
    emits('run-loading', true)

    // 更新数据集
    if (from !== 'history') {
      await indexDataset.update()
    }

    const choosedAllMap = indexChoosed.get()

    const _nameTs = map => {
      return Object.keys(map).reduce(
        (acc, key) => ((acc[key.toLowerCase() + 's'] = map[key]), acc),
        {}
      )
    }

    // 同环比
    const compare = indexCompare.get()
    // 校验选中
    if (!validateChoosed(choosedAllMap)) {
      return
    }
    // 校验过滤条件
    if (!validateFilter(choosedAllMap[CATEGORY.FILTER])) {
      return
    }

    // 转换参数
    const choosedTransformed = transformChoosed(choosedAllMap)

    // 提取字段中需要的参数
    const requiredKeysTransformed = transformRequiredKeys(choosedTransformed)
    // FILTER => filters
    const choosedPayload = _nameTs(requiredKeysTransformed)

    // topN indexTopN.get()
    let sorts = indexTopN.get()
    sorts = !!sorts.sortField ? [{ ...sorts }] : undefined

    // 查询分页
    const paging = indexPaging.get()

    // 表格参数
    const tableOptions = indexOptions.get('table')

    const paylaod = {
      datasetId: dataset.value.id,
      fromSource: 'temporary', // dashboard 从看板查询, report 保存图表查询, temporary 临时查询
      summary: true, // 快速计算占比需要使用到汇总值，查询默认使用汇总
      compare,
      sorts,
      paging,
      ...choosedPayload,
    }
    // type: // QUERY 查询, TOTAL 查询条数, SINGLE_FIELD 枚举值, RATIO 同环比 , PREVIEW 预览

    // 查询条数放入异步
    Promise.resolve().then(async () => {
      // 查询条数
      const { rows = 0 } = await postAnalysisQuery({
        ...paylaod,
        type: 'TOTAL',
      })
      const total = rows[0]?.[0] ?? 0

      responseInfo.total = total
    })
    // 同环比
    const IS_RATIO = Object.keys(compare || {}).length > 0

    const queryPayload = { ...paylaod, type: IS_RATIO ? 'RATIO' : 'QUERY' }
    // 查询数据
    const res = await postAnalysisQuery(queryPayload)

    responseInfo.elapsed = (res.elapsed / 1000).toFixed(2)

    if (res.status === 'FAILED') {
      message.warn('查询错误')
    }

    emits('querySuccess', {
      request: queryPayload,
      response: res,
    })
  } catch (error) {
    console.error('图表分析错误', error)
  } finally {
    runLoading.value = false
    emits('run-loading', false)
  }
}

defineExpose({ run })

// 校验字段选中
const validateChoosed = (choosed = {}) => {
  const type = renderType.value
  const choosedProperty = choosed[CATEGORY.PROPERTY]
  const choosedIndex = choosed[CATEGORY.INDEX]

  // 无指标，无维度
  if (!choosedProperty.length && !choosedIndex.length) {
    message.warn('请拖入分析字段')

    return false
  }

  // 表格只需有维度、指标中的一个就行
  if (isRenderTable(type)) return true

  // 指标卡和饼图值支持一个指标
  if (type === 'statistic' || type === 'pie') {
    if (choosedIndex.length > 1) {
      message.warn('该图表类型仅支持查看1个指标')

      return false
    }
  }

  // 指标卡
  if (type === 'statistic') {
    if (choosedIndex.length === 0) {
      message.warn('该图表类型指标不可为空')

      return false
    }
  } else {
    if (!choosedProperty.length || !choosedIndex.length) {
      message.warn('该图表类型需要维度和指标均不可为空')

      return false
    }
  }

  return true
}
// 校验过滤条件
const validateFilter = (filters = []) => {
  const unValidatedWithEmpty = filters.some(item => {
    return (
      !item.conditions.length ||
      item.conditions?.some(t => {
        // 有值、无值 跳过校验
        if (
          t.functionalOperator === IS_NOT_NULL ||
          t.functionalOperator === IS_NULL
        ) {
          return false
        } else {
          return !t.args?.length && !t.useLatestPartitionValue
        }
      })
    )
  })

  if (unValidatedWithEmpty) {
    message.warn('有过滤字段未筛选条件，请检查后再查询')

    return false
  } else {
    return true
  }
}

// 转换 choosed 参数
const transformChoosed = (choosedMap = {}) => {
  // 指标去重
  const indexRepeated = repeatIndex(choosedMap[CATEGORY.INDEX], item => {
    indexChoosed.remove(item)
  })
  // 维度分组排序
  const dimensionSorted = sortDimension(choosedMap[CATEGORY.PROPERTY], true)

  return {
    // ...choosedMap,
    [CATEGORY.PROPERTY]: dimensionSorted,
    [CATEGORY.INDEX]: indexRepeated,
    [CATEGORY.FILTER]: mergeDashbaordFilters(choosedMap[CATEGORY.FILTER]),
  }
}
// 提取字段需要的参数
const transformRequiredKeys = (choosedMap = {}) => {
  return {
    [CATEGORY.PROPERTY]: choosedMap[CATEGORY.PROPERTY].map(t =>
      getByIncludesKeys(t, [
        'name',
        'expression',
        'displayName',
        'dataType',
        'dateTrunc',
        'firstDayOfWeek',
        'viewModel',
        '_group', // 行维度、列维度
        '_weekStart', // 周显示的起始日
      ])
    ),
    [CATEGORY.INDEX]: choosedMap[CATEGORY.INDEX].map(t =>
      getByIncludesKeys(t, [
        'name',
        'expression',
        'displayName',
        'dataType',
        'aggregator',
        '_modifyDisplayName', // 重命名
        '_quick', // 指标快速计算
        '_quantile', // 自定义分位数
      ])
    ),
    [CATEGORY.FILTER]: choosedMap[CATEGORY.FILTER].map(t =>
      getByIncludesKeys(t, [
        'name',
        'filterType',
        'dataType',
        'logical',
        'having',
        'conditions',
        '_from', // 用作过滤从看板过来的筛选项
      ])
    ),
  }
}
</script>

<style lang="scss" scoped>
.tools {
  display: flex;
  width: 100%;
  padding: 0 10px 10px;
  justify-content: space-between;
  line-height: 1;
  overflow: hidden;

  &.disabled {
    background-color: rgba(211, 211, 211, 0.3);
    pointer-events: none;
  }
}

.btn {
  padding: 6px 8px;
  border-radius: 4px;
  transition: all 0.2s;

  cursor: pointer;
  &.run {
    padding: 0;
    font-size: 24px;
    color: #1677ff;
  }
  &.icon {
    padding-left: 6px;
    padding-right: 6px;
    font-size: 16px;
  }
  &.disabled {
    color: #ccc;
    cursor: not-allowed;
  }
  &:not(.disabled, .run) {
    &:hover {
      background-color: lighten(#1677ff, 40%);
    }
    &.active {
      background-color: #1677ff;
      color: #fff;
    }
  }
}
.query-info {
  font-size: 13px;
  color: #999;
}
</style>
