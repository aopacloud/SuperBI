import { CATEGORY } from '@/CONST.dict'
import { versionJs } from '@/versions'

// 字段选中区域label
export const sectionListLabelMap = {
  default: {
    [CATEGORY.PROPERTY]: '分组',
    [CATEGORY.INDEX]: '指标',
    [CATEGORY.FILTER]: '筛选条件',
  },
  table: {
    [CATEGORY.PROPERTY]: '分组',
    [CATEGORY.INDEX]: '指标',
  },
  bar: {
    [CATEGORY.PROPERTY]: '横轴/分组',
    [CATEGORY.INDEX]: '纵轴/值',
  },
  line: {
    [CATEGORY.PROPERTY]: '横轴/分组',
    [CATEGORY.INDEX]: '纵轴/值',
  },
  pie: {
    [CATEGORY.PROPERTY]: '扇形占比/指标',
    [CATEGORY.INDEX]: '扇形数量/分组',
  },
  statistic: {
    [CATEGORY.PROPERTY]: '分组',
    [CATEGORY.INDEX]: '指标',
  },
  groupTable: {
    [CATEGORY.PROPERTY]: '分组',
    [CATEGORY.INDEX]: '指标',
  },
  intersectionTable: {
    [CATEGORY.PROPERTY + '_ROW']: '行分组',
    [CATEGORY.PROPERTY + '_COLUMN']: '列分组',
    [CATEGORY.INDEX]: '指标',
  },
}

/**
 * 获取字段选中区域label
 * @param {string} renderType 渲染类型
 * @returns {(category: string) => string} category => {}
 */
export const getSectionListLabel = renderType => {
  const labelObj = sectionListLabelMap[renderType] || sectionListLabelMap['default']
  const filterObj = { [CATEGORY.FILTER]: sectionListLabelMap['default'][CATEGORY.FILTER] }

  return category => ({ ...labelObj, ...filterObj }[category])
}

export const WEEK = 'WEEK'

export const DAY = 'DAY'

// 默认日期分组方式
export const DEFAULT_DATE_GROUP = 'ORIGIN'
// 日期维度聚合方式
export const dateGroupOptions = [
  {
    label: '原值',
    value: DEFAULT_DATE_GROUP,
  },
  {
    label: '年',
    value: 'YEAR',
  },
  {
    label: '季度',
    value: 'QUARTER',
  },
  {
    label: '月',
    value: 'MONTH',
  },
  {
    label: '周',
    value: WEEK,
    children: [
      {
        label: '周一 至 周日',
        value: 1,
      },
      {
        label: '周二 至 周一',
        value: 2,
      },
      {
        label: '周三 至 周二',
        value: 3,
      },
      {
        label: '周四 至 周三',
        value: 4,
      },
      {
        label: '周五 至 周四',
        value: 5,
      },
      {
        label: '周六 至 周五',
        value: 6,
      },
      {
        label: '周日 至 周六',
        value: 7,
      },
    ],
  },
  {
    label: '日',
    value: 'DAY',
  },
]

// 默认日期展示
export const DEFAULT_WEEK_DISPLAY = 'WEEK_SEQUENCE'
export const DEFAULT_DAY_DISPLAY = 'DAY_SEQUENCE'

// 日期展示
// WEEK_SEQUENCE(1, '第x周')
// WEEK_SEQUENCE_YEAR(2, 'xxxx年第x周')
// WEEK_RANGE(3, '2022/04/17 - 2022/04/23')
// WEEK_RANGE_WITHOUT_YEAR(4, '04/17 - 04/23')
// DAY_SEQUENCE(5, 'xxxx-xx-xx')
// DAY_WEEK(6, '周x')
export const dateDisplayOptions = [
  {
    label: '第 * 周',
    value: DEFAULT_WEEK_DISPLAY,
    group: 'WEEK',
  },
  {
    label: 'YYYY 年 第 * 周',
    value: 'WEEK_SEQUENCE_YEAR',
    group: 'WEEK',
  },
  {
    label: 'YYYY/MM/DD - YYYY/MM/DD',
    value: 'WEEK_RANGE',
    group: 'WEEK',
  },
  {
    label: 'MM/DD - MM/DD',
    value: 'WEEK_RANGE_WITHOUT_YEAR',
    group: 'WEEK',
  },
  {
    label: 'YYYY/MM/DD',
    group: 'DAY',
    value: DEFAULT_DAY_DISPLAY,
  },
  {
    label: 'YYYY/MM/DD（周 *）',
    group: 'DAY',
    value: 'DAY_WEEK',
  },
]

// 对比字段
export const toContrastFiled = field => versionJs.ViewsAnalysis.isDateField(field)

// 默认对比类型
export const DEFAULT_RATIO_TYPE = 'CHAIN'

export const COMPARE_RATIO_PERIOD = {
  SAME: 'SAME',
  WHOLE: 'WHOLE',
  DEFAULT: 'SAME',
}

// 对比类型
export const ratioOptions = [
  { label: '环比', value: DEFAULT_RATIO_TYPE }, // 1
  { label: '周同比', value: 'WEEK_ON_WEEK' }, // 2
  { label: '月同比', value: 'MONTH_ON_MONTH' }, // 3
  { label: '年同比', value: 'YEAR_ON_YEAR' }, // 4
]

// 查询展示前条数
export const queryTotalOptions = [
  { label: 10000, value: 10000 },
  { label: 50000, value: 50000 },
  { label: 100000, value: 100000 },
]

/**
 * 对比字段的聚合方式
 * {key}: '' 原值，1 年，2 季，3 月，4 周，5 天
 * {value}: 对比 ratioOptions 映射关系
 */
export const dateGroupTypeMap = {
  // 原值
  ORIGIN: {
    CHAIN: [{ label: '前一天', value: COMPARE_RATIO_PERIOD.SAME }],
    WEEK_ON_WEEK: [{ label: '上周同一天', value: COMPARE_RATIO_PERIOD.SAME }],
    MONTH_ON_MONTH: [{ label: '上月同一天', value: COMPARE_RATIO_PERIOD.SAME }],
    YEAR_ON_YEAR: [{ label: '去年同一天', value: COMPARE_RATIO_PERIOD.SAME }],
  },
  // 年
  YEAR: {
    CHAIN: [
      { label: '去年整年', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  // 季
  QUARTER: {
    CHAIN: [
      { label: '上季整季', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上季同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    YEAR_ON_YEAR: [
      { label: '去年同季', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  // 月
  MONTH: {
    CHAIN: [
      { label: '上月整月', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上月同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    YEAR_ON_YEAR: [
      { label: '去年同月', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  // 周
  WEEK: {
    CHAIN: [
      { label: '上周整周', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上周同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    YEAR_ON_YEAR: [
      { label: '去年同周', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  // 天
  DAY: {
    CHAIN: [{ label: '前一天', value: COMPARE_RATIO_PERIOD.SAME }],
    WEEK_ON_WEEK: [{ label: '上周同一天', value: COMPARE_RATIO_PERIOD.SAME }],
    MONTH_ON_MONTH: [{ label: '上月同一天', value: COMPARE_RATIO_PERIOD.SAME }],
    YEAR_ON_YEAR: [{ label: '去年同一天', value: COMPARE_RATIO_PERIOD.SAME }],
  },
}
