import { versionJs } from '@/versions'
export const WEEK = 'WEEK'

export const DAY = 'DAY'

// 默认日期分组方式
export const DEFAULT_DATEGROUP = 'ORIGIN'
// 日期维度聚合方式
export const dateGroupOptions = [
  {
    label: '原值',
    value: DEFAULT_DATEGROUP,
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
export const DEAULT_RATIO_TYPE = 'CHAIN'
// 对比类型
export const ratioOptions = [
  { label: '环比', value: DEAULT_RATIO_TYPE }, // 1
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
