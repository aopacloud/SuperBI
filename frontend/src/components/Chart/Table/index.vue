<template>
  <section class="section">
    <main class="main">
      <vxe-grid
        ref="vTableRef"
        class="chart-table"
        min-height="0"
        max-height="100%"
        resizable
        show-header-overflow="title"
        show-overflow="title"
        show-footer-overflow="title"
        footer-row-class-name="summary-row"
        :class="{ 'reversed-table': isReversed, 'has-seq': hasSeq }"
        :show-header="!isReversed"
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
        :seq-config="seqConfig"
        @toggle-tree-expand="onToggleTreeExpand"
        @sort-change="onSortChange"
        @resizable-change="onResizableChange"
      >
        <template #default> default </template>

        <!-- 表头 -->
        <template #header="{ column }">
          <template
            v-if="
              column.params._columnFields && column.params._columnFields.length
            "
          >
            <div
              v-for="col in column.params._columnFields"
              class="header-cell-column"
              @click.stop
            >
              <span class="header-cell-text"> {{ col.title }}</span>
            </div>
          </template>

          <div class="header-cell" v-if="column.title">
            <div class="header-cell-text">
              <span
                v-if="column.treeNode"
                :style="{ visibility: isGroupTable ? 'visible' : 'hidden' }"
              >
                <a-button
                  size="small"
                  type="text"
                  v-if="isTreeExpandAll"
                  :icon="h(CaretDownOutlined)"
                  @click.stop="toggleTreeExpandAll(false)"
                />
                <a-button
                  size="small"
                  type="text"
                  v-else
                  :icon="h(CaretRightOutlined)"
                  @click.stop="toggleTreeExpandAll(true)"
                />
              </span>
              {{ column.title }}
            </div>
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
          {{ displayIndexCell(row, column) }}
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
            :config="config.table"
            :dTarget="displayQuickIndexCell(row[column.field], column, row)"
            :target="getVsTarget(column.field, row)"
            :origin="row[column.field]"
          />
        </template>

        <!-- 表尾汇总 -->
        <template #footerSummary>
          <div class="summary-text">
            <b>
              汇总
              <span v-if="false">
                <!--修复表尾数据自定义插槽显示错位及其他预料之外的显示错误， 原因未知？？？ -->
              </span>
            </b>
          </div>
        </template>

        <!-- 表尾维度汇总单元格  -->
        <template #[footerPropertySummarySlot]></template>

        <!-- 表尾指标汇总单元格 -->
        <template #[footerIndexSummarySlot]="{ column }">
          <!-- 同环比汇总 -->
          <CompareDisplay
            v-if="column.params._isVs"
            isSummary
            :displayed="getVsSummaryDisplayed(column, false)"
            :dataset="dataset"
            :field="column.params.field"
            :compare="config.compare"
            :config="config.table"
            :target="getVsSummary(column)"
            :origin="getVsSummary(column, false)"
          />
          <span v-else>
            {{ displayBottomSummaryIndexCell(column) }}
          </span>
        </template>

        <!-- 列汇总单元格 -->
        <template #columnSummary="{ row }">
          {{ displayColumnSummaryCell(row) }}
        </template>

        <!-- 行列反转的默认插槽 -->
        <template #reversedDefault="{ row, column }">
          <template v-if="row.col0_field?.params?._isVs">
            <CompareDisplay
              :dataset="dataset"
              :field="row.col0_field.params.field"
              :compare="config.compare"
              :config="config.table"
              :target="getVsTargetByReversed({ column, row })"
              :origin="getVsOriginByReversed({ column, row })"
            />
          </template>
          <template v-else>
            {{ displayReversedCell(row, column) }}
          </template>
        </template>

        <!-- 行列反转的列汇总 -> 表尾行汇总 -->
        <template #reversedRowSummary="{ column }">
          {{ displayReversedColumnSummaryCell(column) }}
        </template>

        <!-- 行列反转的行汇总 -> 列汇总 -->
        <template #reversedColumnSummary="{ row }">
          <template v-if="row.col0_field?.params?._isVs">
            <CompareDisplay
              isSummary
              :dataset="dataset"
              :field="row.col0_field.params.field"
              :compare="config.compare"
              :config="config.table"
              :displayed="getVsSummaryReversedDisplayed(row)"
              :target="getVsSummaryReversed(row)"
              :origin="getVsSummaryReversed(row, false)"
            />
          </template>
          <template v-else>
            {{ displayReversedRowSummaryCell(row) }}
          </template>
        </template>
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
      :current="pager.current"
      @change="onPagerChange"
      @showSizeChange="onPagerShowSizeChange"
    />
  </section>
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
  toRaw
} from 'vue'
import { CaretDownOutlined, CaretRightOutlined } from '@ant-design/icons-vue'
import CompareDisplay from './components/CompareDisplay.vue'
import { CATEGORY } from '@/CONST.dict'
import { PAGER_SIZES } from './components/config'
import {
  getByPager,
  getCompareDisplay,
  getFixedPlace,
  listDataToTreeByKeys,
  createSummaryMap,
  displayQuickCalculateValue,
  displayBottomSummaryIndexCell as _displayBottomSummaryIndexCell,
  formatIndexDisplay as _formatIndexDisplay,
  displayColumnSummaryCell as _displayColumnSummaryCell,
  getSummarizedValue,
  getDisplayBySummaryAggregator,
  getCompareDifferArray
} from './utils'
import {
  CELL_HEADER_PADDING,
  CELL_BODY_PADDING,
  CELL_BUFFER_WIDTH,
  CELL_MIN_WIDTH,
  CELL_SEQ_WIDTH
} from './config'
import { VS_FIELD_SUFFIX, formatDtWithOption } from '../utils/index'
import {
  getWordWidth,
  deepClone,
  createSortByOrders,
  deepFind,
  debounce
} from '@/common/utils/help'
import { upcaseFirst } from 'common/utils/string'
import { flat } from 'common/utils/array'
import {
  TREE_ROW_KEY,
  TREE_ROW_PARENT_KEY,
  TREE_GROUP_NAME
} from '../utils/createGroupTable'
import { isRenderTable } from '@/views/analysis/utils'
import { formatterByCustom } from '@/views/dataset/config.field'
import { toThousand, avg, toDigit, toPercent } from 'common/utils/number'
import { COMPARE } from '@/CONST.dict'

defineOptions({
  name: 'ChartTable',
  inheritAttrs: false
})

const emits = defineEmits(['updateTableSorter', 'updateTableColumnsWidth'])

const props = defineProps({
  renderType: {
    type: String,
    default: 'table',
    validate: t => isRenderTable(t)
  },
  size: {
    type: String,
    default: 'mini',
    validator: size => {
      return ['mini', 'small', 'default', 'large'].includes(size)
    }
  },
  columns: {
    type: Array,
    default: () => []
  },
  dataSource: {
    type: Array,
    default: () => []
  },
  options: {
    type: Object,
    default: () => ({})
  },
  summaryRows: {
    type: Array,
    default: () => []
  },
  summaryMap: {
    type: [Boolean, Object]
  },
  // 排序
  sorters: {
    type: [Array, Object], // { row = [], column = [] } }
    default: () => []
  },
  // 格式化
  formatters: {
    type: Array,
    default: () => []
  }
})

const vTableRef = ref(null)

// 当前配置
const config = reactive({
  table: {}, // 表格参数
  sorter: [], // 排序
  compare: {} // 同环比
})
// 渲染列
const rColumns = ref([])

// 初始化
watchEffect(() => {
  const { sorter = [], compare = {}, ...res } = props.options

  config.table = res
  config.sorter = Array.isArray(sorter) ? sorter : [sorter]
  config.compare = compare

  setTimeout(() => {
    updateTableSorter()
  }, 10)
})

const allSorter = computed(() => {
  let sorters = []
  // 交叉表格
  if (props.renderType === 'intersectionTable') {
    const { row = [], column = [] } = deepClone(props.sorters)
    sorters = row
      .map(t => ({ ...t, _group: 'row' }))
      .concat(column.map(t => ({ ...t, _group: 'column' })))
  } else {
    sorters = deepClone(props.sorters)
  }

  const newList = deepClone(config.sorter.filter(t => !!t.order))

  return [...sorters, ...newList].reduce((acc, cur) => {
    const item = acc.find(t => t.fieldAlias === cur.fieldAlias)

    // fieldAlias 字段别名（真实字段名）
    if (!item) {
      acc.push({
        ...cur,
        fieldAlias:
          cur._group === 'column' ? 'title' : (cur.fieldAlias ?? cur.field)
      })
    } else {
      item.order = cur.order
      if (cur.order !== 'custom') {
        item.custom = undefined
      }
    }

    return acc
  }, [])
})

// 更新外层options
watch(
  config,
  c => {
    for (const key in c) {
      if (key !== 'table') {
        props.options[key] = c[key]
      }
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
    // multiple: true,
    sortMethod: () => {},
    defaultSort: config.sorter || []
  }
})
// vxe横向滚动
const vScrollX = computed(() => {
  return {
    enabled: true, // props.renderType !== 'intersectionTable',
    gt: 15
  }
})
// 排序事件
const onSortChange = ({ sortList }) => {
  config.sorter = sortList.map(t => {
    const { field, order, column } = t

    // fieldAlias 字段别名（真实字段名）
    return {
      field,
      fieldAlias:
        field !== TREE_GROUP_NAME
          ? field
          : (column.params.fields[0] || column.params.field)?.field,
      order
    }
  })
  emits('updateTableSorter', [...config.sorter])

  setTimeout(() => {
    updateTableSorter()
  }, 10)
}
// 数据集
const dataset = computed(() => props.options.dataset)
// 表格配置
const tableConfig = computed(() => {
  return config.table || {}
})

const showPager = computed(() => {
  return tableConfig.value.pager?.show !== false && !isReversed.value
})

watch(allSorter, (arr = []) => {
  if (isReversed.value) return // 行列转置下排序变更需重新构建表格数据

  const l = arr.filter(t => !!t.order)

  if (!l.length) {
    originSortedList.value = props.dataSource.slice()
  } else {
    const rows = l.filter(t => t._group !== 'column')
    originSortedList.value = sortedList(props.dataSource.slice(), rows)

    // 列分组
    const columns = l.filter(t => t._group === 'column')
    if (columns.length) {
      rColumns.value = sortedColumns(props.columns.slice(), columns)
    }
  }

  renderTable()
})

// 设置列固定
const setColumnFixed = async () => {
  rColumns.value = rColumns.value.map((col, i, arr) => {
    return {
      ...col,
      fixed:
        col.type === 'seq' || (isReversed.value && col.field === 'col0')
          ? 'left'
          : getFixedPlace(i, arr.length, tableConfig.value.fixed.columnSpan)
    }
  })

  setTimeout(() => {
    updateTableSorter()
  })
}

const updateTableSorter = () => {
  const sortList = config.sorter

  if (!vTableRef.value) return

  sortList.forEach(sorter => {
    vTableRef.value.sort(sorter)
  })
}

/**
 * 获取汇总行列宽
 * @param {列信息} column
 * @param {汇总数据} summary
 * @param {列原始字段} oField
 */
const getSummaryColumnWidth = ({ column, summary }) => {
  // 无汇总行
  if (!showSummary.value) return 0

  const fieldName = column.field
  const oField = column.params?.field
  if (oField.category !== CATEGORY.INDEX) return 0

  // 同环比同时显示
  if (config.compare.merge) {
    const target = formatIndexDisplay(getVsTarget(fieldName, summary), oField)

    // 原值的格式化
    const origin =
      '(' +
      getCompareDisplay({
        target: getVsTarget(fieldName, summary),
        origin: summary[fieldName],
        mode: config.compare.mode
      })(oField, dataset.value.fields) +
      ')'

    return getWordWidth(target) + getWordWidth(origin)
  } else {
    return summary[fieldName]
      ? getWordWidth(formatIndexDisplay(summary[fieldName], oField))
      : 0
  }
}

// 获取同环比格式化后的字符串
const getCompareMergedFormattedStr = ({ origin, target, mode, field }) => {
  const t = formatIndexDisplay(target, field)

  return (
    t +
    '(' +
    getCompareDisplay({ origin, target, mode, config: tableConfig.value })(
      field,
      dataset.value.fields
    ) +
    ')'
  )
}

// 获取列的伸缩宽度
const getResizeWidth = column => {
  const resizeWidth = tableColumnsWidth.value.find(
    t => t.field === column.field
  )

  return resizeWidth?.width
}

// 更新列宽度
const updateColumnWidth = async () => {
  const summary = props.summaryMap

  const columnSummaryConfig = summaryConfig.value.column || {}

  const isColumnSummaryFirst =
    columnSummaryConfig.enable && columnSummaryConfig.position === 'first'

  const columnSummaryData = isReversed.value
    ? list.value.map(displayReversedRowSummaryCell)
    : list.value.map(displayColumnSummaryCell)

  const maxColumnSummaryDataWith = Math.max(
    ...columnSummaryData.map(t => getWordWidth(t))
  )

  // 行列反转的计算方案
  const setAutoWidth2 = (cols = [], data = []) => {
    if (!cols.length) return []

    return cols.map(col => {
      if (col.type === 'seq') {
        return {
          ...col,
          width: CELL_SEQ_WIDTH
        }
      }

      const resizeWidth = getResizeWidth(col)
      if (resizeWidth) {
        return {
          ...col,
          width: resizeWidth
        }
      }

      const colDataWidth = data.map(row => {
        const { col0_field: cField } = row
        const v = row[col.field]

        if (!cField) return

        const oField = cField.params?.field
        if (col.field === 'column_summary') {
          if (cField.params._isVs && config.compare.merge) {
            const tV = summary[cField.field.split(VS_FIELD_SUFFIX)[0]]
            const str = getCompareMergedFormattedStr({
              origin: summary[cField.field],
              target: tV,
              mode: config.compare.mode,
              field: oField
            })

            return getWordWidth(str)
          } else {
            return getWordWidth(displayReversedRowSummaryCell(row))
          }
        } else if (oField?.category === CATEGORY.INDEX) {
          if (cField.params.fastCompute) {
            return getWordWidth(displayQuickIndexCell(v, cField))
          } else {
            const tValueWidth = getWordWidth(formatIndexDisplay(v, oField))
            if (
              cField.params._isVs &&
              config.compare.merge &&
              col.field !== 'col0'
            ) {
              const v2Name =
                col.field +
                VS_FIELD_SUFFIX +
                oField.renderName.split(VS_FIELD_SUFFIX)[1]

              const str = getCompareMergedFormattedStr({
                origin: row[v2Name],
                target: v,
                mode: config.compare.mode,
                field: oField
              })

              return getWordWidth(str)
            } else {
              return tValueWidth
            }
          }
        } else if (oField?.category === CATEGORY.PROPERTY) {
          if (isDate(oField)) {
            return getWordWidth(formatDtWithOption(v, oField))
          } else {
            return getWordWidth(v)
          }
        } else {
          return getWordWidth(v)
        }
      })

      return {
        ...col,
        width: Math.max(
          Math.max(...colDataWidth) + CELL_BODY_PADDING + CELL_BUFFER_WIDTH,
          CELL_MIN_WIDTH
        )
      }
    })
  }

  // 设置自定义宽度
  const setAutoWidth = (cols = [], data = [], initialWidth = 0) => {
    if (!cols.length) return []

    return cols.map((col, index) => {
      if (col.type === 'seq') {
        return {
          ...col,
          width: CELL_SEQ_WIDTH
        }
      }

      // 保存了拖拽的宽度
      const resizeWidth = getResizeWidth(col)
      if (resizeWidth) {
        return {
          ...col,
          width: resizeWidth
        }
      }

      // 列汇总
      if (col.params?._isColumnSummary) {
        const tWidth = getWordWidth(col.title) + CELL_HEADER_PADDING
        const width =
          Math.max(tWidth, maxColumnSummaryDataWith + CELL_BODY_PADDING) +
          CELL_BUFFER_WIDTH

        return {
          ...col,
          children: setAutoWidth(col.children, data),
          width: Math.max(width, CELL_MIN_WIDTH)
        }
      }

      const oField = col.params?.field
      const summaryWidth = getSummaryColumnWidth({
        column: col,
        summary
      })

      // 当前字段的所有数据(当前页)
      const dataArr = data.map(t => t[col.field])

      // 计算数据格式化后的最大宽度
      const contentMaxWidth =
        Math.max(
          ...dataArr.map((t, i) => {
            if (oField?.category === CATEGORY.INDEX) {
              if (col.params.fastCompute) {
                return getWordWidth(displayQuickIndexCell(t, col))
              } else {
                if (col.params._isVs) {
                  if (config.compare.merge) {
                    const str = getCompareMergedFormattedStr({
                      origin: t,
                      target: data[i][col.field.split(VS_FIELD_SUFFIX)[0]],
                      mode: config.compare.mode,
                      field: oField
                    })

                    return getWordWidth(str)
                  } else {
                    const str = getCompareDisplay({
                      origin: t,
                      target: data[i][col.field.split(VS_FIELD_SUFFIX)[0]],
                      mode: config.compare.mode
                    })(oField, dataset.value.fields)
                    return getWordWidth(str)
                  }
                }

                return getWordWidth(formatIndexDisplay(t, oField))
              }
            } else if (oField.category === CATEGORY.PROPERTY) {
              if (isDate(oField)) {
                return getWordWidth(formatDtWithOption(t, oField))
              } else {
                return getWordWidth(t)
              }
            } else {
              return getWordWidth(v)
            }
          })
        ) + CELL_BODY_PADDING

      // 分组表格折叠icon宽
      const treeExpandIconWith =
        (isGroupTable.value || props.renderType === 'groupTable') &&
        index === (isColumnSummaryFirst ? 1 : 0)
          ? 24
          : 0

      // 列分组的各表头宽度
      let _columnFieldsWidth = 0
      if (col.params._columnFields?.length) {
        const titleWidth = col.params._columnFields.map(t =>
          getWordWidth(t.title)
        )
        _columnFieldsWidth = Math.max(...titleWidth)
      }

      // 表头宽度
      const titleWidth =
        Math.max(getWordWidth(col.title), _columnFieldsWidth) +
        CELL_HEADER_PADDING +
        treeExpandIconWith

      // 列宽
      const columnWidth =
        Math.max(titleWidth, contentMaxWidth, summaryWidth, CELL_MIN_WIDTH) +
        CELL_BUFFER_WIDTH

      // 子列
      const childrenColumns = setAutoWidth(col.children, data).map(t => t.width)
      // 子列宽度和
      const childrenWidthSum = childrenColumns.reduce((a, b) => a + b, 0)

      // 子列宽度初始宽度（子列宽度和小于父列宽，则子列平均分配父列宽）
      const childrenInitWidth =
        childrenWidthSum && childrenWidthSum < columnWidth
          ? Math.floor(columnWidth / col.children.length)
          : undefined

      return {
        ...col,
        children: setAutoWidth(col.children, data, childrenInitWidth),
        width: initialWidth ? Math.max(columnWidth, initialWidth) : columnWidth
      }
    })
  }

  // 行列反转的计算方案
  const getContentWidth2 = (cols = [], data = []) => {
    return cols.map(col => {
      if (col.type === 'seq') {
        return CELL_SEQ_WIDTH
      }

      // 保存了拖拽的宽度

      const resizeWidth = getResizeWidth(col)
      if (resizeWidth) {
        return resizeWidth
      }

      const contentWIdth = data.map(row => {
        const { col0, col0_field: cField, ...res } = row
        const oField = cField.params?.field
        const keys = Object.keys(res).filter(t => /^col\d+/.test(t))
        const titleWidth = getWordWidth(col0)

        if (col.field === 'column_summary') {
          if (cField.params._isVs && config.compare.merge) {
            const tV = summary[cField.field.split(VS_FIELD_SUFFIX)[0]]
            const str = getCompareMergedFormattedStr({
              origin: summary[cField.field],
              target: tV,
              mode: config.compare.mode,
              field: oField
            })

            return getWordWidth(str)
          } else {
            return getWordWidth(displayReversedRowSummaryCell(row))
          }
        }

        if (
          cField.params._isVs &&
          config.compare.merge &&
          col.field !== 'col0'
        ) {
          const v2Name =
            col.field +
            VS_FIELD_SUFFIX +
            oField.renderName.split(VS_FIELD_SUFFIX)[1]

          const str = getCompareMergedFormattedStr({
            origin: row[v2Name],
            target: row[col.field],
            mode: config.compare.mode,
            field: oField
          })

          return getWordWidth(str)
        }

        const cWidths = keys.map(k => {
          const v = res[k]
          if (oField.category === CATEGORY.INDEX) {
            return getWordWidth(formatIndexDisplay(v, oField))
          } else if (oField.category === CATEGORY.PROPERTY) {
            if (isDate(oField)) {
              return getWordWidth(formatDtWithOption(v, oField))
            } else {
              return getWordWidth(v)
            }
          } else {
            return getWordWidth(v)
          }
        })

        return Math.max(
          titleWidth + CELL_HEADER_PADDING,
          ...cWidths.map(t => t + CELL_BODY_PADDING)
        )
      })

      return Math.max(...contentWIdth, CELL_MIN_WIDTH)
    })
  }

  // 获取内容宽度
  const getContentWidth = (cols = [], data = []) => {
    if (!cols.length) return []

    // 所有字段的宽度
    return cols.map((col, index) => {
      if (col.type === 'seq') {
        return CELL_SEQ_WIDTH
      }

      // 列汇总
      if (col.params?._isColumnSummary) return maxColumnSummaryDataWith

      const oField = col.params.field || {}
      // 当前字段的所有数据(当前页)
      const dataArr = data.map(t => t[col.field])
      // 计算数据格式化后的最大宽度
      const contentMaxWidth =
        Math.max(
          ...dataArr.map(t => {
            if (oField.category === CATEGORY.INDEX) {
              return getWordWidth(formatIndexDisplay(t, oField))
            } else if (oField.category === CATEGORY.PROPERTY) {
              if (isDate(oField)) {
                return getWordWidth(formatDtWithOption(t, oField))
              } else {
                return getWordWidth(t)
              }
            } else {
              return getWordWidth(v)
            }
          }),
          showSummary.value && index === (isColumnSummaryFirst ? 1 : 0)
            ? 105
            : 0
        ) + CELL_BODY_PADDING

      // 分组表格折叠icon宽
      const treeExpandIconWith =
        (isGroupTable.value || props.renderType === 'groupTable') &&
        index === (isColumnSummaryFirst ? 1 : 0)
          ? 24
          : 0

      // 列分组的各表头宽度
      let _columnFieldsWidth = 0
      if (col.params._columnFields?.length) {
        const titleWidth = col.params._columnFields.map(t =>
          getWordWidth(t.title)
        )
        _columnFieldsWidth = Math.max(...titleWidth)
      }

      // 表头宽度
      const titleWidth =
        Math.max(getWordWidth(col.title), _columnFieldsWidth) +
        CELL_HEADER_PADDING +
        treeExpandIconWith

      const summaryWidth = getSummaryColumnWidth({
        column: col,
        summary
      })

      const childrenContentWidth = getContentWidth(col.children, data)

      return Math.max(
        titleWidth,
        contentMaxWidth,
        summaryWidth,
        ...childrenContentWidth
      )
    })
  }

  // 设置等宽
  const setEqualedWidth = (cols = [], width = CELL_MIN_WIDTH) => {
    if (!cols.length) return
    return cols.map(col => {
      if (col.type === 'seq') {
        return {
          ...col,
          width: CELL_SEQ_WIDTH
        }
      }

      let cWidth
      const resizeWidth = getResizeWidth(col)
      if (resizeWidth) {
        cWidth = resizeWidth
      }

      return {
        ...col,
        children: setEqualedWidth(col.children, width),
        width: cWidth || width
      }
    })
  }

  if (tableConfig.value.layout === 'auto') {
    rColumns.value = isReversed.value
      ? setAutoWidth2(rColumns.value, list.value)
      : setAutoWidth(rColumns.value, list.value)
  } else {
    const contentWidth = isReversed.value
      ? getContentWidth2(rColumns.value, list.value)
      : getContentWidth(rColumns.value, list.value)
    const width = Math.max(...contentWidth, CELL_MIN_WIDTH) + CELL_BUFFER_WIDTH

    rColumns.value = setEqualedWidth(rColumns.value, width)
  }

  setTimeout(() => {
    updateTableSorter()
  }, 40)
}

// 监听列冻结
watch(
  tableConfig.value.fixed,
  () => {
    setColumnFixed().then(renderColumns) //.then(initExpandRowKeys)
  },
  { deep: true }
)

// 监听列宽设置
watch(
  () => tableConfig.value.layout,
  () => {
    updateColumnWidth().then(renderColumns) //.then(initExpandRowKeys)
  }
)

// 分页大小
const pagerSize = props.size === 'mini' ? 'small' : props.size
const pageSizeOptions = shallowRef(PAGER_SIZES)
const pager = reactive({
  current: 1,
  size: props.options.pager?.pageSize || 20,
  total: 0
})

// 监听排序
watch([() => pager.current, () => pager.size], () => renderTable(), {
  deep: true
})

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
const listLoading = ref(true)
// 数据渲染
const renderList = debounce(async () => {
  if (!vTableRef.value) return

  // if (isGroupTable.value)
  listLoading.value = true

  vTableRef.value
    .reloadData(list.value)
    .then(r => {
      initExpandRowKeys()
    })
    .catch(e => {
      console.error('renderList error', e)
    })
    .finally(() => {
      setTimeout(() => {
        listLoading.value = false
      })
    })
}, 30)

// 表头渲染
const columnsLoading = ref(false)
const renderColumns = debounce(async () => {
  if (!vTableRef.value) return

  columnsLoading.value = true
  vTableRef.value
    .loadColumn(rColumns.value)
    .catch(e => {
      console.error('renderColumns error', e)
    })
    .finally(() => {
      setTimeout(() => {
        columnsLoading.value = false
      })
    })
}, 30)

// 渲染表格
const renderTable = debounce(async () => {
  const l = showPager.value
    ? getByPager(originSortedList.value, {
        current: pager.current,
        size: pager.size
      })
    : originSortedList.value

  list.value = !isGroupTable.value ? [...l] : flat(l)

  if (isGroupTable.value) {
    listTree.value = deepClone(originSortedList.value)
  } else {
    const newColumns = props.columns.filter(t => !!t.params?.field)
    const groupKeys = newColumns
      .filter(col => col.params.field.category === CATEGORY.PROPERTY)
      .map(t => t.params.field.renderName)

    // 汇总数据
    const cellSummaryRows = props.summaryRows.slice(+showSummary.value)
    const fields = newColumns.map(t => t.params.field)
    const summaryMap =
      props.summaryMap || createSummaryMap(cellSummaryRows, fields)
    listTree.value = listDataToTreeByKeys({
      list: deepClone(originSortedList.value),
      groupKeys,
      summaryMap
    })
  }

  // 更新列宽
  updateColumnWidth()
  // 渲染列
  renderColumns()
  // 渲染行
  renderList()
}, 60)

// 初始化
const init = () => {
  console.info('--------  Components/Chart/Table init  --------')

  // TODO:
  // 看板项中会触发多次 table * 3 => chart => table * 2， 分析中会触发两次 table => chart => table
  const { dataSource, columns } = props

  pager.current = 1
  pager.total = dataSource.length

  originSortedList.value = sortedList(dataSource.slice())
  rColumns.value = sortedColumns(columns.slice())

  setColumnFixed()

  renderTable()
}

// 排序列表头
const sortedColumns = (columns, sorters = allSorter.value) => {
  sorters = sorters.filter(t => t._group === 'column' && !!t.order)
  if (!sorters.length) return columns

  // 行， 列
  const [rows, cols, summary] = columns.reduce(
    (acc, cur) => {
      if (cur.field === 'column_summary') {
        acc[2] = cur
      } else if (cur.field === TREE_GROUP_NAME) {
        acc[0].push(cur)
      } else {
        acc[1].push(cur)
      }
      return acc
    },
    [[], []]
  )
  // 维度列的所在位置
  const summaryIndex = columns.findIndex(t => t.field === 'column_summary')

  const fn = (list, sorts = []) => {
    const st = sorts.filter(s =>
      list.some(t => s.field === t.params?.field?.renderName)
    )

    return list
      .map(t => {
        if (t.children?.length) {
          return {
            ...t,
            children: fn(t.children, sorts)
          }
        } else {
          return t
        }
      })
      .sort(createSortByOrders(st))
  }

  const newCols = rows.concat(fn(cols, toRaw(sorters)))
  // 将维度列插入到指定位置
  if (summary && summaryIndex > -1) {
    newCols.splice(summaryIndex, 0, summary)
  }
  return newCols
}

// 排序数据
const sortedList = (arr = [], sorters = allSorter.value) => {
  sorters = sorters.filter(t => t._group !== 'column' && !!t.order)
  if (!sorters.length) return arr

  const sortFn = (list = [], sorts = []) => {
    return list
      .map(t => {
        if (t.children?.length) {
          return {
            ...t,
            children: sortFn(t.children, sorts)
          }
        } else {
          return t
        }
      })
      .sort(createSortByOrders(sorts))
  }

  // 添加索引（分组表格的索引需要累加）
  const addSeqIndex = (list = []) => {
    flat(list).forEach((t, i) => {
      const item = deepFind(list, a => a._ROW_ID_ === t._ROW_ID_)
      item._seq = i
    })

    return list
  }

  const sorted = sortFn(arr, toRaw(sorters))

  return props.renderType === 'table' ? sorted : addSeqIndex(sorted)
}
// 监听数据源
watch([() => props.dataSource, () => props.columns], init)

// 监听格式化配置
watch(
  () => props.formatters,
  () => {
    updateColumnWidth().then(renderColumns) //.then(initExpandRowKeys)
  },
  { deep: true }
)

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
    parentField: TREE_ROW_PARENT_KEY
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

// 序号配置
const seqConfig = reactive({
  seqMethod(e) {
    const { seq, row } = e

    if (props.renderType === 'table') {
      const prev = (pager.current - 1) * pager.size
      return prev + seq
    } else {
      return row._seq + 1
    }
  }
})

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
  if (!groupField) return value

  if (isDate(groupField.params.field)) {
    return formatDtWithOption(value, groupField.params.field)
  } else {
    return value
  }
}

// 指标的格式化显示
const displayIndexCell = (row, column) => {
  const field = column.params.field
  return formatIndexDisplay(row[column.field], field)
}

// 指标快速计算的显示
const displayQuickIndexCell = (value, column, row) => {
  if (!row) return value

  return displayQuickCalculateValue({
    row,
    field: column.params.field,
    renderType: props.renderType,
    columns: props.columns,
    listTree: listTree.value,
    summary: props.summaryMap,
    isGroupTable: isGroupTable.value,
    special: tableConfig.value.special
  })
}

// 格式化显示
const formatIndexDisplay = (value, field) => {
  return _formatIndexDisplay({
    value,
    field,
    fields: dataset.value.fields,
    formatters: props.formatters
  })
}

const summaryConfig = computed(() => {
  const s = tableConfig.value.summary

  if (typeof s === 'undefined') return {}

  if (Array.isArray(s)) {
    return {
      row: {
        enable: true,
        list: s
      },
      column: {}
    }
  } else {
    return s
  }
})

watch(
  [
    () => summaryConfig.value.column?.aggregator,
    () => summaryConfig.value.row?.list
  ],
  (n, o) => {
    nextTick(() => {
      updateColumnWidth().then(renderColumns) //.then(initExpandRowKeys)
    })
  },
  { deep: true }
)

// 底部汇总指标的显示
const displayBottomSummaryIndexCell = column => {
  return _displayBottomSummaryIndexCell({
    field: column.params.field,
    list: originSortedList.value,
    summaryOptions: summaryConfig.value.row?.list,
    formatters: props.formatters,
    datasetFields: dataset.value.fields,
    summaryValue: props.summaryMap[column.field]
  })
}

// 列汇总的显示
const displayColumnSummaryCell = row => {
  const v = _displayColumnSummaryCell({
    row,
    columns: props.columns,
    aggregator: summaryConfig.value.column?.aggregator
  })

  const isPercent = String(v).indexOf('%') > -1
  const hasDigit = String(v).indexOf('.') > -1
  if (hasDigit) {
    return formatterByCustom(v, {
      type: isPercent ? 1 : 0,
      digit: 2,
      thousand: true
    })
  } else {
    return toThousand(v)
  }
}

// 获取原始字段值
const getVsTarget = (fieldName, row) => {
  const [pre] = fieldName.split(VS_FIELD_SUFFIX)

  return row[pre]
}

// 同环比的汇总的显示值
const getVsSummaryDisplayed = ({ field: fieldName, params = {} }) => {
  const mode = config.compare.mode
  if (mode === COMPARE.MODE.ORIGIN) return

  const { field: oField } = params

  const [targetFieldName] = fieldName.split(VS_FIELD_SUFFIX)
  const originSummaryValue = props.summaryMap[fieldName]
  const targetSummaryValue = props.summaryMap[targetFieldName]

  const aggregator = getFieldAggregator(oField.name)

  // 自动汇总
  if (!aggregator || aggregator === 'auto') {
    return getCompareDisplay({
      origin: originSummaryValue,
      target: targetSummaryValue,
      mode,
      config: tableConfig.value
    })(oField, dataset.value.fields)
  }

  const targetValueList = originSortedList.value.map(t => t[targetFieldName])
  const originValueList = originSortedList.value.map(t => t[fieldName])

  const differ = getCompareDifferArray(
    targetValueList,
    originValueList,
    mode === COMPARE.MODE.DIFF_PERSENT
  )

  const defaultValue =
    mode === COMPARE.MODE.DIFF_PERSENT
      ? (targetSummaryValue - originSummaryValue) / originSummaryValue
      : targetSummaryValue - originSummaryValue

  const value = getSummarizedValue(differ, aggregator, defaultValue)

  let s = value
  if (!aggregator || aggregator === 'auto') {
    if (mode === COMPARE.MODE.DIFF_PERSENT) {
      s = toPercent(defaultValue, 2)
    } else {
      s = toDigit(defaultValue, 2)
    }
  }

  if (mode === COMPARE.MODE.DIFF_PERSENT) {
    s = toPercent(value, 2)
  } else {
    s = toDigit(value, 2)
  }

  return value > 0 && aggregator !== 'COUNT' && aggregator !== 'COUNT_DISTINCT'
    ? `+${s}`
    : s
}

// 获取字段的汇总值
const getFieldAggregator = fieldName => {
  let summaryOptions = summaryConfig.value.row?.list || []
  summaryOptions = summaryOptions.reduce((acc, cur) => {
    const { name = [], aggregator, _all } = cur
    if (_all) {
      return acc.concat({ name: fieldName, aggregator })
    } else {
      return acc.concat(name.map(t => ({ name: t, aggregator })))
    }
  }, [])

  const item = summaryOptions.find(t => t.name === fieldName)
  return item?.aggregator
}

// 获取同环比的汇总值
const getVsSummary = ({ field: fieldName, params = {} }, isTarget = true) => {
  const pre = isTarget ? fieldName.split(VS_FIELD_SUFFIX)[0] : fieldName,
    oField = params.field,
    summaryValue = props.summaryMap[pre],
    values = originSortedList.value.map(t => t[pre])

  const aggregator = getFieldAggregator(oField.name)

  return getDisplayBySummaryAggregator({
    values,
    aggregator,
    defaultValue: summaryValue
  })({
    field: oField,
    fields: dataset.value.fields,
    formatters: props.formatters
  })
}

// 获取同环比的汇总值 - 行列转置
const getVsSummaryReversedDisplayed = row => {
  const { mode = 0 } = config.compare || {}
  if (mode === COMPARE.MODE.ORIGIN) return

  const cField = row.col0_field
  const oField = cField.params.field || {}
  const fieldName = cField.field //
  const targetName = fieldName.split(VS_FIELD_SUFFIX)[0]
  const targetSummaryValue = props.summaryMap[targetName]
  const originSummaryValue = props.summaryMap[fieldName]

  const aggregator = getFieldAggregator(oField.name)

  // 自动汇总
  if (!aggregator || aggregator === 'auto') {
    return getCompareDisplay({
      origin: originSummaryValue,
      target: targetSummaryValue,
      mode,
      config: tableConfig.value
    })(oField, dataset.value.fields)
  }

  const targetValueList = getValuesFromReversedRow(
    row,
    config.compare.merge
      ? undefined
      : new RegExp(`^col\\d+${VS_FIELD_SUFFIX}TARGET$`)
  )
  const originValueList = getValuesFromReversedRow(
    row,
    config.compare.merge
      ? new RegExp(
          `^col\\d+${VS_FIELD_SUFFIX}${fieldName.split(VS_FIELD_SUFFIX)[1]}$`
        )
      : undefined
  )

  const differ = getCompareDifferArray(
    targetValueList,
    originValueList,
    mode === COMPARE.MODE.DIFF_PERSENT
  )

  const defaultValue =
    mode === COMPARE.MODE.DIFF_PERSENT
      ? (targetSummaryValue - originSummaryValue) / originSummaryValue
      : targetSummaryValue - originSummaryValue

  const value = getSummarizedValue(differ, aggregator, defaultValue)

  let s = value
  if (!aggregator || aggregator === 'auto') {
    if (mode === COMPARE.MODE.DIFF_PERSENT) {
      s = toPercent(defaultValue, 2)
    } else {
      s = toDigit(defaultValue, 2)
    }
  }

  if (mode === COMPARE.MODE.DIFF_PERSENT) {
    s = toPercent(value, 2)
  } else {
    s = toDigit(value, 2)
  }

  return value > 0 && aggregator !== 'COUNT' && aggregator !== 'COUNT_DISTINCT'
    ? `+${s}`
    : s
}

// 获取同环比的汇总 - 行列转置
const getVsSummaryReversed = (row, isTarget = true) => {
  const cField = row.col0_field,
    oName = isTarget ? cField.field.split(VS_FIELD_SUFFIX)[0] : cField.field,
    summaryValue = props.summaryMap[oName],
    pattern = isTarget
      ? config.compare.merge
        ? undefined
        : new RegExp(`^col\\d+${VS_FIELD_SUFFIX}TARGET$`)
      : config.compare.merge
        ? new RegExp(
            `^col\\d+${VS_FIELD_SUFFIX}${oName.split(VS_FIELD_SUFFIX)[1]}$`
          )
        : undefined,
    values = getValuesFromReversedRow(row, pattern)

  const aggregator = getFieldAggregator(oName)

  return getDisplayBySummaryAggregator({
    values,
    aggregator,
    defaultValue: summaryValue
  })({
    field: cField.params.field,
    fields: dataset.value.fields,
    formatters: props.formatters
  })
}

// 获取行列转置下的同环比target
const getVsTargetByReversed = ({ column, row }) => {
  if (config.compare.merge) {
    return row[column.field]
  } else {
    return row[column.field + VS_FIELD_SUFFIX + 'TARGET']
  }
}

// 获取行列转置下的同环比origin
const getVsOriginByReversed = ({ column, row }) => {
  const cField = row.col0_field

  if (config.compare.merge) {
    const oField = cField.params?.field
    const vName = oField.renderName.split(VS_FIELD_SUFFIX)[1]
    return row[column.field + VS_FIELD_SUFFIX + vName]
  } else {
    return row[column.field]
  }
}

// 显示汇总行
const showSummary = computed(() => {
  if ('summary' in tableConfig.value) {
    const summary = tableConfig.value.summary
    if (!summary) return
    if (Array.isArray(summary)) return summary.length > 0

    const { row: rowSummary = {}, column: columnSummary = {} } = summary
    return tableConfig.value.reverse ? columnSummary.enable : rowSummary.enable
  } else {
    if (!tableConfig.value.showSummary) return
  }

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

const footerIndexSummarySlot = shallowRef(
  'footer' + upcaseFirst(CATEGORY.INDEX)
)

const footerPropertySummarySlot = shallowRef(
  'footer' + upcaseFirst(CATEGORY.PROPERTY)
)

const isDate = field => {
  if (!field) return
  return (
    field.category === CATEGORY.PROPERTY && field.dataType?.includes('TIME_')
  )
}

const isReversed = computed(
  () => props.renderType === 'table' && tableConfig.value.reverse
)

const hasSeq = computed(() => props.columns.some(t => t.type === 'seq'))

const getValuesFromReversedRow = (row, pattern) => {
  const { colo, col0_field: cField, ...res } = row
  return Object.keys(res)
    .filter(t => (pattern ? pattern.test(t) : /^col\d+$/.test(t)))
    .map(t => row[t])
    .filter(
      t => typeof t !== 'undefined' && t !== null && typeof t !== 'string'
    )
}

const displayReversedCell = (row, column) => {
  const { col0_field: cField } = row
  const value = row[column.field]
  if (!cField) return value

  const oField = cField.params.field
  if (!oField) return value

  // 快速计算
  if (cField.params.fastCompute) {
    if (!row) return value
    return displayQuickCalculateValue({
      row: { [oField.renderName]: row[column.field] },
      field: oField,
      listTree: getValuesFromReversedRow(row).map(t => ({
        [oField.renderName]: t
      })),
      summary: props.summaryMap,
      special: tableConfig.value.special
    })
  } else {
    if (isDate(oField)) {
      return formatDtWithOption(value, oField)
    } else if (oField.category === CATEGORY.INDEX) {
      return formatIndexDisplay(value, oField)
    } else {
      return value
    }
  }
}

// 反转列汇总 => 底部汇总行
const displayReversedColumnSummaryCell = column => {
  const values = originSortedList.value.map(t => t[column.field])
  const v = getSummarizedValue(values, summaryConfig.value.column?.aggregator)

  const isPercent = String(v).indexOf('%') > -1
  const hasDigit = String(v).indexOf('.') > -1

  if (hasDigit) {
    return formatterByCustom(v, {
      type: isPercent ? 1 : 0,
      digit: 2,
      thousand: true
    })
  } else {
    return toThousand(v)
  }
}

// 反转行汇总 => 列汇总
const displayReversedRowSummaryCell = row => {
  const cField = row.col0_field
  if (!cField) return

  if (cField.params.field.category !== CATEGORY.INDEX) return

  return _displayBottomSummaryIndexCell({
    field: row.col0_field.params.field,
    values: getValuesFromReversedRow(row),
    summaryOptions: summaryConfig.value.row?.list,
    formatters: props.formatters,
    datasetFields: dataset.value.fields,
    summaryValue: props.summaryMap[cField.field]
  })
}

const tableColumnsWidth = computed(() => {
  const flatColumns = flat(deepClone(props.columns))
  if (typeof tableConfig.value.columnsWidth === 'undefined') {
    tableConfig.value.columnsWidth = flatColumns.map(t => ({ field: t.field }))
  }
  return tableConfig.value.columnsWidth.filter(t =>
    flatColumns.some(c => c.field === t.field)
  )
})
const onResizableChange = ({ column = {}, resizeWidth }) => {
  const item = tableColumnsWidth.value.find(t => t.field === column.field)
  if (item) {
    item.width = resizeWidth
  } else {
    tableConfig.value.columnsWidth.push({
      field: column.field,
      width: resizeWidth
    })
  }

  emits('updateTableColumnsWidth', toRaw(tableColumnsWidth.value))
}
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
    .vxe-cell--title {
      flex: 1;
    }
    .merge-header {
      // pointer-events: none;
      overflow: hidden;
      .c--title {
        pointer-events: none;
      }
      .vxe-cell,
      .vxe-cell--title {
        overflow: initial;
      }
      .vxe-cell--title {
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 100%;
      }
      .vxe-cell--sort {
        position: absolute;
        bottom: 6px;
        right: 8px;
      }
      .header-cell {
        position: relative;
        display: flex;
        align-items: center;
        flex: 1;
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
        max-height: initial;
      }
      &:hover {
        background-color: #f1f1f2;
      }
    }

    .summary-row {
      background-color: #f8f8f9;
    }

    .summary-cell {
      background-color: #f8f8f9;
    }
  }

  &.reversed-table {
    :deep(.vxe-table) {
      .vxe-body--column:nth-child(1) {
        background-color: #f8f8f9;
      }
    }
    &.has-seq {
      :deep(.vxe-table) {
        .vxe-body--column:nth-child(2) {
          background-color: #f8f8f9;
        }
      }
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
