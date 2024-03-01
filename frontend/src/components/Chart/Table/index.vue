<template>
  <section class="section" v-resize="updateSize">
    <main class="main">
      <vxe-grid
        ref="vTableRef"
        class="chart-table"
        min-height="0"
        max-height="100%"
        show-overflow="title"
        :size="size"
        :border="tableConfig.bordered ?? true"
        :align="tableConfig.align"
        :columns="rColumns"
        :data="list"
        :column-config="vColumnConfig"
        :sort-config="vSortConfig"
        :scroll-y="{ enabled: true, gt: 20 }"
        :show-footer="showSummary"
        :footer-method="renderFooter"
        footer-row-class-name="summary-row"
        @sort-change="onSortChange">
        <template></template>
        <!-- 表头 -->
        <template #header="{ column }">
          {{ column.title }}

          <!-- 避免过多的组件实例???  https://cn.vuejs.org/guide/best-practices/performance.html#avoid-unnecessary-component-abstractions -->
          <SettingDropdown
            :column="column"
            :sort="config.sorter"
            :formatter="formatterConfig.find(t => t.field === column.field)"
            @openChange="e => onOnOpenChange(e, column)"
            @menuClick="e => onSettingDropdownMenuClick(e, column)">
            <SettingOutlined
              :class="['trigger-btn', { active: isColumnSettingActive(column) }]"
              @click.stop="e => onColumnSettingClick(e, column)" />
          </SettingDropdown>
        </template>

        <template #date="{ column, row }">
          {{ formatDtDisplay(row[column.field], column) }}
        </template>

        <!-- 维度插槽 -->
        <template #[CATEGORY.PROPERTY]="{ column, row }">
          {{ row[column.field] }}
        </template>

        <!-- 指标插槽 -->
        <template #[CATEGORY.INDEX]="{ column, row }">
          <span v-if="row._summary">{{ summaryMap[column.field] }}</span>
          <span v-else>{{ formatIndexDisplay(row[column.field], column) }}</span>
        </template>

        <!-- 同环比插槽 -->
        <template #vs="{ row, column }">
          <CompareDisplay
            :dataset="dataset"
            :field="column.params.field"
            :compare="config.compare"
            :target="getVsTarget(column.field, row)"
            :origin="row[column.field]" />
        </template>

        <!-- 表尾汇总 -->
        <template #footer.summary>
          <div class="summary-text">
            <b style="margin-right: 4px">汇总:</b>
            <span
              :class="['all', { active: summaryScope === 'ALL' }]"
              @click="toggleSummaryScope('ALL')"
              >全部
            </span>
            <span> / </span>
            <span
              :class="['page', { active: summaryScope === 'PAGE' }]"
              @click="toggleSummaryScope('PAGE')"
              >当前页
            </span>
          </div>
        </template>
        <template #[footerSummarySlot]="{ column }">
          <!-- 同环比汇总 -->
          <CompareDisplay
            v-if="column.params.field._isVs"
            :dataset="dataset"
            :field="column.params.field"
            :compare="config.compare"
            :target="getVsTarget(column.field, summaryMap)"
            :origin="summaryMap[column.field]" />
          <span v-else>
            {{ formatIndexDisplay(summaryMap[column.field], column) }}
          </span>
        </template>
      </vxe-grid>
    </main>

    <a-pagination
      v-if="showPager"
      ref="pagerRef"
      class="footer"
      show-size-changer
      show-quick-jumper
      :show-total="total => `共 ${total} 条`"
      :page-size-options="pageSizeOptions"
      :size="pagerSize"
      :page-size="pager.size"
      :total="pager.total"
      v-model:current="pager.current"
      @change="onPagerChange"
      @showSizeChange="onPagerShowSizeChange" />
  </section>

  <teleport to="body">
    <NumberCustomFormatter
      :style="formatterCustomStyle"
      :value="customFormatterField.config"
      v-model:open="formatterCustomVisible"
      @ok="onCustomFormatterOk" />
  </teleport>
</template>
<script setup lang="jsx">
import { ref, reactive, shallowRef, computed, watchEffect, watch, nextTick } from 'vue'
import { SettingOutlined } from '@ant-design/icons-vue'
import SettingDropdown from './components/SettingDropdown.vue'
import NumberCustomFormatter from '@/components/CustomFormatter/index.vue'
import { CATEGORY } from '@/CONST.dict'
import { PAGER_SIZES, SORT_PREFFIX } from './components/config'
import {
  FORMAT_DEFAULT_CODE,
  FORMAT_CUSTOM_CODE,
  formatterOptions,
} from '@/views/dataset/config.field'
import { getByPager, createSortByOrder, getCompareDisplay } from './utils'
import {
  getFixedPlace,
  CELL_HEADER_PADDING,
  CELL_BODY_PADDING,
  CELL_BUFFER_WIDTH,
  CELL_MIN_WIDTH,
  getSummary,
} from '@/components/Chart/utils/createTableData'
import {
  VS_FIELD_SUFFIX,
  is_vs,
  formatDtWithOption,
} from '@/components/Chart/utils/index.js'
import CompareDisplay from './components/CompareDisplay.vue'
import { getWordWidth } from '@/common/utils/help'

const props = defineProps({
  size: {
    type: String,
    default: 'mini',
    validator: size => {
      return ['mini', 'small', 'default', 'large'].includes(size)
    },
  },
  columns: {
    type: Array,
    default: () => [],
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
  options: {
    type: Object,
    default: () => ({}),
  },
})

const vTableRef = ref(null)
// 表格高度
const vTableHeight = ref(10)

// 分页引用
const pagerRef = ref(null)

const updateSize = () => {
  const parent = vTableRef.value.$el.parentElement
  vTableHeight.value = parent.offsetHeight - pagerRef.value?.$el?.clientHeight
}

// 当前配置
const config = reactive({
  table: {}, // 表格参数
  sorter: {}, // 排序
  compare: {}, // 同环比
  formatter: [], // 格式化
})
// 渲染列
const rColumns = ref([])

// 初始化
watchEffect(() => {
  const { sorter = {}, compare = {}, formatter = [], ...res } = props.options

  config.table = res
  config.sorter = sorter
  config.compare = compare
  config.formatter = formatter

  setTimeout(() => {
    vTableRef.value?.sort(sorter)
  }, 10)
})

// 更新外层options
watch(
  config,
  c => {
    for (const key in c) {
      props.options[key] = c[key]
    }
  },
  { deep: true }
)

// vxe列配置
const vColumnConfig = reactive({})
// vxe排序配置
const vSortConfig = computed(() => {
  return {
    trigger: 'cell',
    sortMethod: () => {},
    defaultSort: config.sorter || {},
  }
})
// 排序事件
const onSortChange = e => {
  const { order, field } = e

  config.sorter.field = field
  config.sorter.order = order
}
// 数据集
const dataset = computed(() => props.options.dataset)
// 格式化配置
const formatterConfig = computed(() => {
  return config.formatter || []
})
// 表格配置
const tableConfig = computed(() => {
  return config.table || {}
})

const showPager = computed(() => {
  return tableConfig.value.pager?.show !== false
})

// 监听排序
watch(
  () => config.sorter,
  ({ field, order }) => {
    if (!!order) {
      originSortedList.value = originSortedList.value.sort(
        createSortByOrder(order === 'asc', field)
      )
    } else {
      originSortedList.value = props.dataSource.slice()
    }

    list.value = showPager.value
      ? getByPager(originSortedList.value, {
          current: pager.current,
          size: pager.size,
        })
      : originSortedList.value

    updateColumnWidth()
  },
  { deep: true }
)

// 设置列固定
const setColumnFixed = () => {
  rColumns.value = rColumns.value.map((col, i, arr) => {
    return {
      ...col,
      fixed: getFixedPlace(i, arr.length, tableConfig.value.fixed.columnSpan),
    }
  })

  setTimeout(() => {
    vTableRef.value?.sort(config.sorter)
  })
}

/**
 * 获取汇总行列宽
 * @param {列信息} column
 * @param {汇总数据} summaryMap
 * @param {列原始字段} originField
 */
const getSummaryColumnWidth = (column, summaryMap, field) => {
  // 无汇总行
  if (!showSummary.value) return 0

  // 同环比同时显示
  if (config.compare.merge) {
    const target = formatIndexDisplay(getVsTarget(column.field, summaryMap), column)

    // 原值的格式化
    const origin =
      ' ( ' +
      getCompareDisplay(getVsTarget(column.field, summaryMap), summaryMap[column.field])(
        field,
        dataset.value.fields
      ) +
      ' ) '

    return getWordWidth(target) + getWordWidth(origin)
  } else {
    return summaryMap[column.field]
      ? getWordWidth(formatIndexDisplay(summaryMap[column.field], column))
      : 0
  }
}

// 更新列宽度
const updateColumnWidth = () => {
  if (tableConfig.value.layout === 'auto') {
    rColumns.value = rColumns.value.map((col, i) => {
      const originField = col.params.field
      const summaryWidth = getSummaryColumnWidth(col, summaryMap.value, originField)

      if (col._width) {
        // 同环比的汇总列

        return {
          ...col,
          width: Math.max(col._width, summaryWidth) + CELL_BUFFER_WIDTH,
        }
      }

      // 当前字段的所有数据(当前页)
      const data = list.value.map(t => t[col.field])
      // 计算数据格式化后的最大宽度
      const contentMaxWidth =
        Math.max(
          ...data.map(t => {
            if (originField.category === CATEGORY.INDEX) {
              return getWordWidth(formatIndexDisplay(t, col))
            } else {
              if (originField.dataType.includes('TIME')) {
                return getWordWidth(formatDtDisplay(t, col))
              } else {
                return getWordWidth(t)
              }
            }
          }),
          showSummary.value && i === 0 ? 105 : 0
        ) + CELL_BODY_PADDING

      // 表头宽度
      const titleWidth = getWordWidth(col.title) + CELL_HEADER_PADDING

      return {
        ...col,
        width:
          Math.max(titleWidth, contentMaxWidth, summaryWidth, CELL_MIN_WIDTH) +
          CELL_BUFFER_WIDTH,
      }
    })
  } else {
    // 所有字段的宽度
    const contentMaxWidths = rColumns.value.map((col, i) => {
      const originField = col.params.field
      // 当前字段的所有数据(当前页)
      const data = list.value.map(t => t[col.field])
      // 计算数据格式化后的最大宽度
      const contentMaxWidth =
        Math.max(
          ...data.map(t => {
            if (originField.category === CATEGORY.INDEX) {
              return getWordWidth(formatIndexDisplay(t, col))
            } else {
              if (originField.dataType.includes('TIME')) {
                return getWordWidth(formatDtDisplay(t, col))
              } else {
                return getWordWidth(t)
              }
            }
          }),
          showSummary.value && i === 0 ? 105 : 0
        ) + CELL_BODY_PADDING

      // 表头宽度
      const titleWidth = getWordWidth(col.title) + CELL_HEADER_PADDING
      const summaryWidth = getSummaryColumnWidth(col, summaryMap.value, originField)

      return Math.max(titleWidth, contentMaxWidth, summaryWidth, col._width ?? 0)
    })

    rColumns.value = rColumns.value.map(col => {
      return {
        ...col,
        width: Math.max(...contentMaxWidths, CELL_MIN_WIDTH) + CELL_BUFFER_WIDTH,
      }
    })
  }

  setTimeout(() => {
    vTableRef.value?.sort(config.sorter)
  })
}

// 监听列冻结
watch(
  tableConfig.value.fixed,
  () => {
    setColumnFixed()
  },
  { deep: true }
)

// 监听列宽设置
watch(
  () => tableConfig.value.layout,
  () => {
    updateColumnWidth()
  }
)

// 分页大小
const pagerSize = props.size === 'mini' ? 'small' : props.size
const pageSizeOptions = shallowRef(PAGER_SIZES)
const pager = reactive({
  current: 1,
  size: props.options.pager?.pageSize || 20,
  total: 0,
})

// 监听排序
watch(
  [() => pager.current, () => pager.size],
  ([current, size]) => {
    list.value = showPager.value
      ? getByPager(originSortedList.value, { current: current, size: size })
      : originSortedList.value

    updateColumnWidth()
  },
  { deep: true }
)
// 分页改变
const onPagerChange = (page, pageSize) => {
  pager.current = page
  pager.size = pageSize
}
// 切换pageSizes
const onPagerShowSizeChange = (_, pageSize) => {
  pager.current = 1
  pager.size = pageSize

  if (!props.options.pager) {
    props.options.pager = {}
  }

  props.options.pager.pageSize = pageSize
}

// 源数据
const originSortedList = shallowRef([])
// 渲染数据
const list = ref([])
// 初始化
const init = () => {
  console.log('--------  Components/Chart/Table init  --------')

  // TODO:
  // 看板项中会触发多次 table * 3 => chart => table * 2， 分析中会触发两次 table => chart => table
  const { dataSource, columns } = props

  originSortedList.value = sortedList(dataSource.slice())

  pager.current = 1
  pager.total = dataSource.length

  list.value = showPager.value
    ? getByPager(originSortedList.value, {
        current: 1,
        size: pager.size,
      })
    : originSortedList.value

  rColumns.value = columns

  setColumnFixed()
  nextTick(() => {
    updateColumnWidth()
  })
}
// 排序数据
const sortedList = (arr = []) => {
  const { field, order } = config.sorter

  return arr.sort(createSortByOrder(order === 'asc', field))
}
// 监听数据源
watch([() => props.dataSource, () => props.columns], init)
// 监听格式化配置
watch(formatterConfig, updateColumnWidth, { deep: true })

// 表头设置高亮
const isColumnSettingActive = column => {
  const { field } = column
  const { order, field: sField } = config.sorter
  const { code, field: fField } =
    formatterConfig.value.find(t => t.field === column.field) || {}

  return (
    (field === sField && !!order) || (field === fField && code !== FORMAT_DEFAULT_CODE)
  )
}

// 自定义格式显示
const formatterCustomVisible = ref(false)
// 自定义格式样式
const formatterCustomStyle = ref()
// 自定义格式字段
const settingColumn = ref()
const customFormatterField = ref({})
let settingDropdownTriggerCell = null

// 表头下拉菜单点击
const onColumnSettingClick = (e, column) => {
  let parent = e.target.parentElement

  while (parent === undefined || parent === null) {
    parent = parent.parentElement
  }

  settingDropdownTriggerCell = parent.parentElement.offsetParent
}
// 表头设置下拉菜单展开事件
const onOnOpenChange = (e, column) => {
  if (e) {
    // settingColumn.value = column
  } else {
    if (!formatterCustomVisible.value) settingColumn.value = null
  }
}
// 列设置菜单点击
const onSettingDropdownMenuClick = ({ key }, column) => {
  const { field } = column
  const [type, val] = key.split('_/_')

  if (type === SORT_PREFFIX) {
    const order = !!val ? val : null

    config.sorter.field = field
    config.sorter.order = order

    vTableRef.value.sort({ field, order })
  } else {
    if (val === FORMAT_CUSTOM_CODE) {
      const { code, config } = formatterConfig.value.find(t => t.field === field) || {}

      formatterCustomVisible.value = true
      customFormatterField.value = { field, code, config }

      const { right, top, height } = settingDropdownTriggerCell.getBoundingClientRect()
      const x = `calc(-100% + ${right}px)`,
        y = `${top + height}px`

      formatterCustomStyle.value = {
        position: 'fixed',
        transform: `translate(${x}, ${y})`,
        top: 0,
        opacity: 1,
      }
    } else {
      setFormatter(field, {
        code: val,
      })
    }
  }
}
// 设置格式化
const setFormatter = (field, { code, config: _config }) => {
  const item = config.formatter.find(t => t.field === field)

  if (item) {
    item.code = code
    item.config = _config
  } else {
    config.formatter.push({ field, code, config: _config })
  }
}
// 格式化确认
const onCustomFormatterOk = payload => {
  setFormatter(customFormatterField.value.field, {
    code: FORMAT_CUSTOM_CODE,
    config: payload,
  })
}

const formatDtDisplay = (value, { params }) => {
  return formatDtWithOption(value, params.field)
}

// 格式化显示
const formatIndexDisplay = (value, column) => {
  const { field: fieldName, params } = column
  const originField = params.field

  if (originField.category !== CATEGORY.INDEX) return value

  const originDatasetField = dataset.value.fields.find(t => t.name === originField.name)
  if (!originDatasetField) return value

  // 当前配置的格式化
  let fConfig = formatterConfig.value.find(t => t.field === fieldName)
  if (!fConfig || fConfig.code === FORMAT_DEFAULT_CODE) {
    // 无格式化 或 默认，使用数据集字段的格式化
    fConfig = {
      field: fieldName,
      code: originDatasetField.dataFormat,
      config: originDatasetField.customFormatConfig,
    }
  }

  // 格式化配置
  const formatItem = formatterOptions.find(t => t.value === fConfig.code)
  if (!formatItem) return value

  return formatItem.format(value, fConfig.config)
}

// 获取原始字段值
const getVsTarget = (fieldName, row) => {
  return row[fieldName.replace(VS_FIELD_SUFFIX, '')]
}

// 显示汇总行
// const showSummary = ref(true)
const showSummary = computed(() => {
  return (
    tableConfig.value.showSummary &&
    props.columns.some(t => t.params.field.category === CATEGORY.INDEX)
  )
})

// 表尾渲染
const renderFooter = () => {
  return [{}]
}
const footerSummarySlot = computed(() => 'footer.' + CATEGORY.INDEX)

/**
 * 汇总范围
 * ALL: 全部
 * PAGE: 当前页
 */
const summaryScope = ref('ALL')
const toggleSummaryScope = scope => {
  summaryScope.value = scope
}

// 汇总数据
const summaryMap = computed(() => {
  // 只有指标需要被汇总计算
  const summaryColumns = props.columns
    .filter(t => t.params.field.category === CATEGORY.INDEX)
    .reduce((acc, col) => {
      if (config.compare.merge && col.params.field._isVs) {
        acc.push({
          ...col,
          field: col.field.replace(VS_FIELD_SUFFIX, ''),
        })
      }

      acc.push(col)

      return acc
    }, [])

  const keys = summaryColumns.map(t => t.field)

  // 汇总的数据范围
  const summaryList = summaryScope.value === 'ALL' ? originSortedList.value : list.value

  return {
    ...getSummary({
      keys,
      columns: summaryColumns,
      list: summaryList.filter(t => !t._summary),
    }),
  }
})
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.main {
  flex: 1;
  overflow: hidden;
}

.chart-table {
  display: inline-block;
  max-width: 100%;

  :deep(.vxe-table) {
    .vxe-header--column {
      cursor: pointer;
      .vxe-cell {
        padding-right: 30px;
      }
      &:hover {
        background-color: #f1f1f2;
      }
    }

    .summary-row {
      background-color: #f8f8f9;
    }
  }
}

.summary-text {
  user-select: none;
  color: #333;
  .all,
  .page {
    color: #aaa;
    cursor: pointer;
    &.active {
      color: inherit;
    }
  }
}

.footer {
  align-self: flex-end;
  padding: 10px;
}
.trigger-btn {
  position: absolute;
  top: 50%;
  right: 4px;
  line-height: 1;
  margin-top: -11px;
  padding: 4px;
  border-radius: 2px;
  font-size: 14px;
  z-index: 1;
  &:hover {
    background-color: #e1e1e2;
  }
  &.active {
    font-weight: 600;
    color: #1677ff;
  }
}
</style>
