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
        :scroll-x="{ enabled: true, gt: 10 }"
        :scroll-y="{ enabled: true, gt: 10 }"
        :show-footer="showSummary"
        :footer-method="renderFooter"
        :tree-config="treeConfig"
        @toggle-tree-expand="onToggleTreeExpand"
        @sort-change="onSortChange">
        <template></template>

        <!-- 表头 -->
        <template #header="{ column }">
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

            {{ column.title }}
          </template>
          <template v-else>{{ column.title }}</template>

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

        <template #groupName="{ column, row }">
          {{ displayGroupName(row, column) }}
        </template>

        <template #date="{ column, row }">
          {{ formatDtWithOption(row[column.field], column.params.field) }}
        </template>

        <!-- 维度插槽 -->
        <template #[CATEGORY.PROPERTY]="{ column, row }">
          {{ row[column.field] }}
        </template>

        <!-- 指标插槽 -->
        <template #[CATEGORY.INDEX]="{ column, row }">
          {{ formatIndexDisplay(row[column.field], column) }}
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
        <template #footerSummary>
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
        <template #[footerIndexSummarySlot]="{ column }">
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
<script setup lang="jsx">
import {
  h,
  ref,
  reactive,
  shallowRef,
  computed,
  watchEffect,
  watch,
  nextTick,
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
import { getByPager, getCompareDisplay, getFixedPlace, getSummary } from './utils'
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
import { getWordWidth, deepClone, debounce } from '@/common/utils/help'
import { upcaseFirst } from 'common/utils/string'
import { flat } from '@/common/utils/array'
import { TREE_ROW_KEY, TREE_ROW_PARENT_KEY } from '../utils/createGroupTable'

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

    // 排序需要重置展开行
    initList().then(resetExpandRowKeys)
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
      getCompareDisplay(
        getVsTarget(column.field, summaryMap),
        summaryMap[column.field]
      )(field, dataset.value.fields) +
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
            if (originField?.category === CATEGORY.INDEX) {
              return getWordWidth(formatIndexDisplay(t, col))
            } else {
              if (originField?.dataType?.includes('TIME')) {
                return getWordWidth(formatDtWithOption(t, col.params.field))
              } else {
                return getWordWidth(t)
              }
            }
          }),
          showSummary.value && i === 0 ? 105 : 0
        ) + CELL_BODY_PADDING

      // 分组表格折叠icon宽
      const treeExpandIconWith = isGroupTable.value && i === 0 ? 24 : 0
      // 表头宽度
      const titleWidth =
        getWordWidth(col.title) + CELL_HEADER_PADDING + treeExpandIconWith

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
                return getWordWidth(formatDtWithOption(t, col.params.field))
              } else {
                return getWordWidth(t)
              }
            }
          }),
          showSummary.value && i === 0 ? 105 : 0
        ) + CELL_BODY_PADDING
      // 分组表格折叠icon宽
      const treeExpandIconWith = isGroupTable.value && i === 0 ? 24 : 0
      // 表头宽度
      const titleWidth =
        getWordWidth(col.title) + CELL_HEADER_PADDING + treeExpandIconWith
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
  }, 40)
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
    initList().then(initExpandRowKeys)
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
// 列缓存 - list 会处理为扁平，无法计算分组表格的汇总
const listBak = ref([])
// 初始化list
const initList = async () => {
  const l = showPager.value
    ? getByPager(originSortedList.value, {
        current: pager.current,
        size: pager.size,
      })
    : originSortedList.value

  listBak.value = l
  list.value = !isGroupTable.value ? l : flat(l)

  return shouldExpandRowKeys.value
}

// 表头渲染
const columnsLoading = ref(false)
const renderColumns = cols => {
  // 不使用防抖不用判断列的渲染状态
  // || columnsLoading.value
  if (!vTableRef.value) return

  columnsLoading.value = true
  vTableRef.value.reloadColumn(cols).then(r => {
    setTimeout(() => {
      columnsLoading.value = false
    })
  })
}
// 数据加载时会计算列宽，重新渲染列，使用防抖会在表格间切换瞬间表头撕裂
// const renderColumnsDebounced = debounce(renderColumns, 1000, false)
watch(rColumns, renderColumns)

// 数据渲染
const listLoading = ref(false)
const renderList = ls => {
  if (!vTableRef.value) return

  if (isGroupTable.value) listLoading.value = true

  vTableRef.value.reloadData(ls).then(r => {
    setTimeout(() => {
      listLoading.value = false
    })
  })
}
const renderListDebounce = debounce(renderList, 10, false)
watch(list, renderListDebounce)

// 初始化
const init = () => {
  console.log('--------  Components/Chart/Table init  --------')

  // TODO:
  // 看板项中会触发多次 table * 3 => chart => table * 2， 分析中会触发两次 table => chart => table
  const { dataSource, columns } = props

  originSortedList.value = sortedList(dataSource.slice())

  pager.current = 1
  pager.total = dataSource.length

  rColumns.value = columns

  initList().then(initExpandRowKeys)
  setColumnFixed()
  updateColumnWidth()
}

// 排序数据
const sortedList = (arr = []) => {
  const { field, order } = config.sorter

  return !order ? arr.slice() : arr.sort(createSortByOrder(order === 'asc', field))
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
const initExpandRowKeys = (shouldExpandKeys = []) => {
  if (!isGroupTable.value) {
    expandRowKeys.value = []
  } else {
    if (expandRowKeys.value.length) {
      const oldRowKeys = expandRowKeys.value.filter(t =>
        shouldExpandKeys.includes(t)
      )

      expandRowKeys.value = !oldRowKeys.length ? shouldExpandKeys : oldRowKeys
    } else {
      expandRowKeys.value = shouldExpandKeys
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

const displayGroupName = (row, column) => {
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

// 获取原始字段值
const getVsTarget = (fieldName, row) => {
  const [pre, ...res] = fieldName.split(VS_FIELD_SUFFIX)

  return row[pre]
}

// 显示汇总行
const showSummary = computed(() => {
  return tableConfig.value.showSummary
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
        // 同环比字段时pop了原字段所属的列，此处需要先push进来计算同环比原字段的汇总
        const [targetField] = col.field.split(VS_FIELD_SUFFIX)

        if (acc.every(t => t.field !== targetField)) {
          acc.push({
            ...col,
            field: targetField,
          })
        }
      }

      acc.push(col)

      return acc
    }, [])

  const keys = summaryColumns.map(t => t.field)

  // 汇总的数据范围
  const summaryList =
    summaryScope.value === 'ALL' ? originSortedList.value : listBak.value

  return {
    ...getSummary({
      keys,
      columns: summaryColumns,
      list: summaryList,
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
