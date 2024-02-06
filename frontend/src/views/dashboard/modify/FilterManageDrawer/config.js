// 过滤类型
export const filterTypeOptions = [
  { label: '文本输入框', value: 'TEXT' },
  { label: '日期过滤', value: 'TIME' },
  {
    label: '数字输入框',
    value: 'NUMBER',
  },
  {
    label: '枚举过滤',
    value: 'ENUM',
  },
  {
    label: '自定义输入',
    value: 'CUSTOM',
  },
]

// 过滤方式
export const filterMethodsOptions = [
  { label: '单条件', value: 'EQUAL' },
  { label: '或条件', value: 'OR' },
  { label: '且条件', value: 'AND' },
]
// 默认过滤项
export const defaultFilterItem = {
  name: '',
  chartIntellect: true,
  filterType: 'TEXT',
  required: false,
  single: false,
  filterMethod: 'EQUAL',
  enumResourceType: undefined,
  enumList: [],
  enumList_input: [],
  setDefault: false,
  value: [{ operator: 'EQUAL', value: '' }],
  charts: {},
}
