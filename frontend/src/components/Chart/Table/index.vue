<template>
  <section class="section">
    <main class="main">
      <vxe-grid
        ref="vTableRef"
        class="chart-table"
        min-height="0"
        max-height="100%"
        show-header-overflow="title"
        show-overflow="title"
        show-footer-overflow="title"
        footer-row-class-name="summary-row"
        :size="size"
        :border="tableConfig.bordered ?? true"
        :align="tableConfig.align"
        :loading="listLoading"
        :column-config="vColumnConfig"
        :sort-config="vSortConfig"
        :scroll-x="vScrollX"
        :scroll-y="{ enabled: true, gt: 10 }"
        :show-footer="showSummary"
        :footer-method="renderFooter"
        :tree-config="treeConfig"
        @toggle-tree-expand="onToggleTreeExpand"
        @sort-change="onSortChange">
        <template></template>

        <!-- 表头 -->
        <template #header="{ column }">
          <template
            v-if="column.params._columnFields && column.params._columnFields.length">
            <div
              v-for="col in column.params._columnFields"
              class="header-cell-column"
              @click.stop>
              <span class="header-cell-text"> {{ col.displayName }}</span>
            </div>
          </template>

          <div class="header-cell" v-if="column.title">
            <span class="header-cell-text">
              <template v-if="column.treeNode">
                <a-button
                  size="small"
                  type="text"
                  v-if="isTreeExpandAll"
                  :icon="h(CaretDownOutlined)"
                  @click.stop="toggleTreeExpandAll(false)" />
                <a-button
                  size="small"
                  type="text"
                  v-else
                  :icon="h(CaretRightOutlined)"
                  @click.stop="toggleTreeExpandAll(true)" />
              </template>
              {{ column.title }}
            </span>

            <!-- 避免过多的组件实例???  https://cn.vuejs.org/guide/best-practices/performance.html#avoid-unnecessary-component-abstractions -->
            <SettingDropdown
              v-if="column.params.formable"
              :column="column"
              :sort="config.sorter"
              :formatter="formatterConfig.find(t => t.field === column.field)"
              @openChange="e => onOnOpenChange(e, column)"
              @menuClick="e => onSettingDropdownMenuClick(e, column)">
              <SettingOutlined
                :class="['trigger-btn', { active: isColumnSettingActive(column) }]"
                @click.stop="e => onColumnSettingClick(e, column)" />
            </SettingDropdown>
          </div>
        </template>

        <template #groupName="{ column, row }">
          {{ displayGroupNameCell(row, column) }}
        </template>

        <template #date="{ column, row }">
          {{ displayDateCell(row, column) }}
        </template>

        <!-- 维度插槽 -->
        <template #[CATEGORY.PROPERTY]="{ column, row }">
          {{ row[column.field] }}
        </template>

        <!-- 指标插槽 -->
        <template #[CATEGORY.INDEX]="{ column, row }">
          {{ displayIndex(row, column) }}
        </template>

        <!-- 快速计算插槽 -->
        <template #quickCalculate="{ column, row }">
          {{ displayQuickIndexCell(row[column.field], column, row) }}
        </template>

        <!-- 同环比插槽 -->
        <template #vs="{ row, column }">
          <CompareDisplay
            :dataset="dataset"
            :field="column.params.field"
            :compare="config.compare"
            :dTarget="displayQuickIndexCell(row[column.field], column, row)"
            :target="getVsTarget(column.field, row)"
            :origin="row[column.field]" />
        </template>

        <!-- 表尾汇总 -->
        <template #footerSummary>
          <div class="summary-text">
            <b>汇总</b>
          </div>
        </template>

        <template #[footerIndexSummarySlot]="{ row, column }">
          <!-- 同环比汇总 -->
          <CompareDisplay
            v-if="column.params._isVs"
            :dataset="dataset"
            :field="column.params.field"
            :compare="config.compare"
            :target="getVsTarget(column.field, summaryMap)"
            :origin="summaryMap[column.field]" />
          <span v-else>
            {{ displayBottomSummaryIndexCell(column) }}
          </span>
        </template>

        <template #[footerPropertySummarySlot]></template>
      </vxe-grid>
    </main>

    <a-pagination
      v-if="showPager"
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
<script setup>
import {
  h,
  ref,
  reactive,
  shallowRef,
  computed,
  watchEffect,
  watch,
  nextTick,
  inject,
} from 'vue'
import {
  CaretDownOutlined,
  CaretRightOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'
import SettingDropdown from './components/SettingDropdown.vue'
import NumberCustomFormatter from '@/components/CustomFormatter/index.vue'
import CompareDisplay from './components/CompareDisplay.vue'
import { CATEGORY } from '@/CONST.dict'
import {
  FORMAT_DEFAULT_CODE,
  FORMAT_CUSTOM_CODE,
  formatterOptions,
} from '@/views/dataset/config.field'
import { PAGER_SIZES, SORT_PREFFIX } from './components/config'
import {
  getByPager,
  getCompareDisplay,
  getFixedPlace,
  listDataToTreeByKeys,
  createSummaryMap,
  displayQuickCalculateValue,
} from './utils'
import {
  CELL_HEADER_PADDING,
  CELL_BODY_PADDING,
  CELL_BUFFER_WIDTH,
  CELL_MIN_WIDTH,
} from './config'
import {
  VS_FIELD_SUFFIX,
  createSortByOrder,
  formatDtWithOption,
} from '../utils/index'
import { getWordWidth, deepFind } from '@/common/utils/help'
import { upcaseFirst } from 'common/utils/string'
import { flat } from 'common/utils/array'
import { toPercent } from '@/common/utils/number'
import { findLast } from 'common/utils/compatible'
import {
  GROUP_PATH,
  TREE_ROW_KEY,
  TREE_ROW_PARENT_KEY,
  SUMMARY_GROUP_NAME_JOIN,
  TREE_GROUP_NAME,
} from '../utils/createGroupTable'
import { isRenderTable } from '@/views/analysis/utils'
import { isDateField } from '@/views/dataset/utils'
import { COLUMN_FIELDS_NAME_JOIN } from '../utils/createIntersectionTable'

defineOptions({
  name: 'ChartTable',
  inheritAttrs: false,
})

const props = defineProps({
  renderType: {
    type: String,
    default: 'table',
    validate: t => isRenderTable(t),
  },
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
  summaryRows: {
    type: Array,
    default: () => [],
  },
  summaryMap: {
    type: [Boolean, Object],
  },

})

const vTableRef = ref(null)

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
// vxe横向滚动
const vScrollX = computed(() => {
  return {
    enabled: props.renderType !== 'intersectionTable',
    gt: 15,
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

    renderTable()
  },
  { deep: true }
)

// 设置列固定
const setColumnFixed = async () => {
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
 * @param {汇总数据} summary
 * @param {列原始字段} originField
 */
const getSummaryColumnWidth = ({ column, summary, field }) => {
  // 无汇总行
  if (!showSummary.value) return 0

  const fieldName = column.field
  // 同环比同时显示
  if (config.compare.merge) {
    const target = formatIndexDisplay(getVsTarget(fieldName, summary), column)

    // 原值的格式化
    const origin =
      ' ( ' +
      getCompareDisplay(getVsTarget(fieldName, summary), summary[fieldName])(
        field,
        dataset.value.fields
      ) +
      ' ) '

    return getWordWidth(target) + getWordWidth(origin)
  } else {
    return summary[fieldName]
      ? getWordWidth(formatIndexDisplay(summary[fieldName], column))
      : 0
  }
}

// 更新列宽度
const updateColumnWidth = async () => {
  const summary = props.summaryMap

  // 设置自定义宽度
  const setAutoWidth = (list = [], data = [], initialWidth = 0) => {
    if (!list.length) return []

    return list.map((item, index) => {
      const originField = item.params.field
      const summaryWidth = getSummaryColumnWidth({
        column: item,
        summary,
        field: originField,
      })

      if (item._width) {
        return {
          ...item,
          children: setAutoWidth(item.children, data),
          width: Math.max(item._width, summaryWidth) + CELL_BUFFER_WIDTH,
        }
      }

      // 当前字段的所有数据(当前页)
      const dataArr = data.map(t => t[item.field])

      // 计算数据格式化后的最大宽度
      const contentMaxWidth =
        Math.max(
          ...dataArr.map(t => {
            if (originField?.category === CATEGORY.INDEX) {
              if (item.params._quick) {
                return getWordWidth(displayQuickIndexCell(t, item))
              } else {
                return getWordWidth(formatIndexDisplay(t, item))
              }
            } else {
              if (originField?.dataType?.includes('TIME')) {
                return getWordWidth(formatDtWithOption(t, originField))
              } else {
                return getWordWidth(t)
              }
            }
          }),
          showSummary.value && index === 0 ? 105 : 0
        ) + CELL_BODY_PADDING

      // 分组表格折叠icon宽
      const treeExpandIconWith = isGroupTable.value && index === 0 ? 24 : 0

      // 表头宽度
      const titleWidth =
        getWordWidth(item.title) + CELL_HEADER_PADDING + treeExpandIconWith

      // 列宽
      const columnWidth =
        Math.max(titleWidth, contentMaxWidth, summaryWidth, CELL_MIN_WIDTH) +
        CELL_BUFFER_WIDTH

      // 子列
      const childrenColumns = setAutoWidth(item.children, data).map(t => t.width)
      // 子列宽度和
      const childrenWidthSum = childrenColumns.reduce((a, b) => a + b, 0)

      // 子列宽度初始宽度（子列宽度和小于父列宽，则子列平均分配父列宽）
      const childrenInitWidth =
        childrenWidthSum && childrenWidthSum < columnWidth
          ? Math.floor(columnWidth / item.children.length)
          : undefined

      return {
        ...item,
        children: setAutoWidth(item.children, data, childrenInitWidth),
        width: initialWidth ? Math.max(columnWidth, initialWidth) : columnWidth,
      }
    })
  }

  // 获取内容宽度
  const getContentWidth = (list = [], data = []) => {
    if (!list.length) return []

    // 所有字段的宽度
    const contentWidth = list.map((item, index) => {
      const originField = item.params.field
      // 当前字段的所有数据(当前页)
      const dataArr = data.map(t => t[item.field])
      // 计算数据格式化后的最大宽度
      const contentMaxWidth =
        Math.max(
          ...dataArr.map(t => {
            if (originField.category === CATEGORY.INDEX) {
              return getWordWidth(formatIndexDisplay(t, item))
            } else {
              if (originField.dataType.includes('TIME')) {
                return getWordWidth(formatDtWithOption(t, item.params.field))
              } else {
                return getWordWidth(t)
              }
            }
          }),
          showSummary.value && index === 0 ? 105 : 0
        ) + CELL_BODY_PADDING
      // 分组表格折叠icon宽
      const treeExpandIconWith = isGroupTable.value && index === 0 ? 24 : 0
      // 表头宽度
      const titleWidth =
        getWordWidth(item.title) + CELL_HEADER_PADDING + treeExpandIconWith
      const summaryWidth = getSummaryColumnWidth({
        column: item,
        summary,
        field: originField,
      })

      const childrenContentWidth = getContentWidth(item.children, data)

      return Math.max(
        titleWidth,
        contentMaxWidth,
        summaryWidth,
        ...childrenContentWidth,
        item._width ?? 0
      )
    })

    return contentWidth
  }

  // 设置等宽
  const setFixedWidth = (list = [], width = CELL_MIN_WIDTH) => {
    if (!list.length) return
    return list.map(item => {
      return {
        ...item,
        children: setFixedWidth(item.children, width),
        width,
      }
    })
  }

  if (tableConfig.value.layout === 'auto') {
    rColumns.value = setAutoWidth(rColumns.value, list.value)
  } else {
    const contentWidth = getContentWidth(rColumns.value, list.value)
    const width = Math.max(...contentWidth, CELL_MIN_WIDTH) + CELL_BUFFER_WIDTH

    rColumns.value = setFixedWidth(rColumns.value, width)
  }

  setTimeout(() => {
    vTableRef.value?.sort(config.sorter)
  }, 40)
}

// 监听列冻结
watch(
  tableConfig.value.fixed,
  () => {
    setColumnFixed().then(renderColumns).then(initExpandRowKeys)
  },
  { deep: true }
)

// 监听列宽设置
watch(
  () => tableConfig.value.layout,
  () => {
    updateColumnWidth().then(renderColumns).then(initExpandRowKeys)
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
watch([() => pager.current, () => pager.size], () => renderTable(), { deep: true })

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
// 渲染数据的树结构, 用作明细表格时的一些需要分组数据时的处理
const listTree = shallowRef([])
const listLoading = ref(false)
// 数据渲染
const renderList = async () => {
  if (!vTableRef.value) return

  if (isGroupTable.value) listLoading.value = true

  vTableRef.value.reloadData(list.value).then(r => {
    initExpandRowKeys()
    setTimeout(() => {
      listLoading.value = false
    })
  })
}

// 表头渲染
const columnsLoading = ref(false)
const renderColumns = async () => {
  if (!vTableRef.value) return

  columnsLoading.value = true
  vTableRef.value.reloadColumn(rColumns.value).then(r => {
    setTimeout(() => {
      columnsLoading.value = false
    })
  })
}

// 渲染表格
const renderTable = async () => {
  const l = showPager.value
    ? getByPager(originSortedList.value, {
        current: pager.current,
        size: pager.size,
      })
    : originSortedList.value

  list.value = !isGroupTable.value ? [...l] : flat(l)

  if (isGroupTable.value) {
    listTree.value = [...l]
  } else {
    const groupKeys = props.columns
      .filter(col => col.params.field.category === CATEGORY.PROPERTY)
      .map(t => t.params.field.renderName)

    // 汇总数据
    const cellSummaryRows = props.summaryRows.slice(+showSummary.value)
    const fields = props.columns.map(t => t.params.field)
    const summaryMap = props.summaryMap || createSummaryMap(cellSummaryRows, fields)
    listTree.value = listDataToTreeByKeys({
      list: l,
      groupKeys,
      summaryMap,
    })
  }

  // 更新列宽
  updateColumnWidth()
  // 渲染列
  renderColumns()
  // 渲染行
  renderList()
}

// 初始化
const init = () => {
  console.info('--------  Components/Chart/Table init  --------')

  // TODO:
  // 看板项中会触发多次 table * 3 => chart => table * 2， 分析中会触发两次 table => chart => table
  const { dataSource, columns } = props

  originSortedList.value = sortedList(dataSource.slice())

  pager.current = 1
  pager.total = dataSource.length

  rColumns.value = columns

  setColumnFixed()
  renderTable()
}

// 排序数据
const sortedList = (arr = []) => {
  const { field, order } = config.sorter

  return !order ? arr.slice() : arr.sort(createSortByOrder(order === 'asc', field))
}
// 监听数据源
watch([() => props.dataSource, () => props.columns], init)

// 监听格式化配置
watch(
  formatterConfig,
  () => {
    updateColumnWidth().then(renderColumns).then(initExpandRowKeys)
  },
  { deep: true }
)

// 表头设置高亮
const isColumnSettingActive = column => {
  const { field } = column
  const { order, field: sField } = config.sorter
  const { code, field: fField } =
    formatterConfig.value.find(t => t.field === column.field) || {}

  return (
    (field === sField && !!order) ||
    (field === fField && code !== FORMAT_DEFAULT_CODE)
  )
}

// 是否分组表格
const isGroupTable = computed(() => {
  return props.options.isGroupTable
})

// 树配置
const treeConfig = computed(() => {
  if (!isGroupTable.value) return

  return {
    transform: true,
    rowField: TREE_ROW_KEY,
    parentField: TREE_ROW_PARENT_KEY,
  }
})
// 是否是第一层父节点
const isFirstLevelParent = t => typeof t[TREE_ROW_PARENT_KEY] === 'undefined'
// 获取第一层父节点id
const getFirstLevelParentId = t => t[TREE_ROW_KEY]
// 分组表格展开行(一级)需要的rowKeys
const shouldExpandRowKeys = computed(() => {
  if (!isGroupTable.value) return []

  return list.value.filter(isFirstLevelParent).map(getFirstLevelParentId)
})
// 收起、展开树节点
const expandTreeRow = (keys = []) => {
  if (!isGroupTable.value) return

  const $table = vTableRef.value
  if (!$table) return

  if (!keys.length) {
    $table.clearTreeExpand()

    return
  }

  const rows = list.value.filter(isFirstLevelParent)

  for (const key of keys) {
    const row = rows.find(t => t[TREE_ROW_KEY] === key)
    if (!row) return

    nextTick(() => {
      $table.setTreeExpand(row, true)
    })
  }
}
// 展开行的RowId
const expandRowKeys = ref([])
// 重置树的展开行
const resetExpandRowKeys = () => {
  expandTreeRow(shouldExpandRowKeys.value)
}
// 初始化树的展开行
const initExpandRowKeys = () => {
  const shouldKeys = shouldExpandRowKeys.value

  if (!isGroupTable.value) {
    expandRowKeys.value = []
  } else {
    if (expandRowKeys.value.length) {
      const oldRowKeys = expandRowKeys.value.filter(t => shouldKeys.includes(t))

      expandRowKeys.value = !oldRowKeys.length ? shouldKeys : oldRowKeys
    } else {
      expandRowKeys.value = shouldKeys
    }

    isTreeExpandAll.value = expandRowKeys.value.length > 0
    expandTreeRow(expandRowKeys.value)
  }
}
// 切换树节点
const onToggleTreeExpand = e => {
  const $table = vTableRef.value
  const expandRows = $table.getTreeExpandRecords()

  expandRowKeys.value = expandRows.map(t => t[TREE_ROW_KEY])
}
// 树节点全部展开
const isTreeExpandAll = ref(false)
const toggleTreeExpandAll = e => {
  if (e) {
    expandTreeRow(shouldExpandRowKeys.value)
  } else {
    vTableRef.value.clearTreeExpand()
    vTableRef.value.clearScroll()
  }
  isTreeExpandAll.value = e
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
      const { code, config } =
        formatterConfig.value.find(t => t.field === field) || {}

      formatterCustomVisible.value = true
      customFormatterField.value = { field, code, config }

      const { right, top, height } =
        settingDropdownTriggerCell.getBoundingClientRect()
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

// 日期的格式化显示
const displayDateCell = (row, column) => {
  return formatDtWithOption(row[column.field], column.params.field)
}

// 分组 groupName 显示
const displayGroupNameCell = (row, column) => {
  const { treeNode, field: renderName, params } = column
  const value = row[renderName]

  if (!isGroupTable.value || !treeNode) return value
  const groupField = params.fields[row._level_]
  const dataType = groupField.dataType
  if (dataType.includes('TIME')) {
    return formatDtWithOption(value, groupField)
  } else {
    return value
  }
}

// 指标的格式化显示
const displayIndex = (row, column) => {
  return formatIndexDisplay(row[column.field], column)
}

// 指标快速计算的显示
const displayQuickIndexCell = (value, column, row) => {
  if (!column.params._quick) return

  if (!row) return value

  return displayQuickCalculateValue({
    row,
    field: column.params.field,
    renderType: props.renderType,
    columns: props.columns,
    listTree: listTree.value,
    summary: props.summaryMap,
    isGroupTable: isGroupTable.value,
  })
}

// 格式化显示
const formatIndexDisplay = (value, column) => {
  const { field: fieldName, params } = column

  const originField = params.field

  if (originField.category !== CATEGORY.INDEX) return value

  const originDatasetField = dataset.value.fields.find(
    t => t.name === originField.name
  )
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

// 底部汇总指标的显示
const displayBottomSummaryIndexCell = column => {
  return formatIndexDisplay(props.summaryMap[column.field], column)
}

// 获取原始字段值
const getVsTarget = (fieldName, row) => {
  const [pre] = fieldName.split(VS_FIELD_SUFFIX)

  return row[pre]
}

// 显示汇总行
const showSummary = computed(() => {
  if (!tableConfig.value.showSummary) return false

  // 交叉表格没有行分组不显示汇总
  if (props.renderType === 'intersectionTable') return !!props.summaryMap

  return true
})
watch(showSummary, () => {
  nextTick(() => {
    vTableRef.value?.updateFooter?.()
  })
})

// 表尾渲染
const renderFooter = () => {
  return [{}]
}

const footerIndexSummarySlot = shallowRef('footer' + upcaseFirst(CATEGORY.INDEX))

const footerPropertySummarySlot = shallowRef(
  'footer' + upcaseFirst(CATEGORY.PROPERTY)
)

// 汇总数据
const bottomSummaryMap = computed(() => {
  // if (!showSummary.value) return {} // 快速计算计算占比时需要使用汇总数据
  // 交叉表格的汇总数据从外部传入
  if (typeof props.summaryMap === 'object' && Object.keys(props.summaryMap).length)
    return props.summaryMap

  const columns = rColumns.value.reduce((acc, col, i) => {
    const { field, fields, _isVs } = col.params

    if (fields) {
      // 对比字段就合并
      if (_isVs) {
        return acc.concat(fields.concat(field))
      } else {
        return acc.concat(fields)
      }
    } else {
      return acc.concat(field)
    }
  }, [])

  const [first = []] = props.summaryRows

  return first.reduce((acc, col, i) => {
    acc[columns[i]?.renderName] = col

    return acc
  }, {})
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
    .merge-header {
      pointer-events: none;
      overflow: hidden;
      .vxe-cell,
      .vxe-cell--title {
        overflow: initial;
      }
      .vxe-cell {
        padding-right: 0 !important;
      }
      .vxe-cell--title {
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 100%;
      }
      .vxe-cell--sort {
        position: absolute;
        bottom: 10px;
        right: 30px;
      }
      .header-cell {
        position: relative;
        display: flex;
        align-items: center;
        flex: 1;
        padding-right: 50px;
        width: 100%;
        pointer-events: auto;
        cursor: pointer;
      }
    }
    .vxe-header--column {
      &:not(.merge-header) {
        cursor: pointer;
      }
      .vxe-cell {
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        padding-right: 30px;
        max-height: initial;
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

.header-cell-text {
  display: inline-block;
  max-width: 100%;
  vertical-align: bottom;
  @extend .ellipsis;
}

.header-cell-column {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  pointer-events: initial;
  &::after {
    content: '';
    position: absolute;
    left: -999px;
    right: -999px;
    bottom: -1px;
    height: 1px;
    background-color: #e8eaec;
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
