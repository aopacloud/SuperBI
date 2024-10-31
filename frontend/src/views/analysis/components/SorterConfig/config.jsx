export const columns = [
  {
    title: '',
    key: 'label',
    width: 90,
    align: 'right',
    cellStyle: {
      border: 'none',
      backgroundColor: 'unset',
    },
    customRender: (text, { rowIndex }) => {
      if (rowIndex === 0) return '主要排序'

      return <span style='color: rgba(0, 0, 0, 0.38)'>次要排序</span>
    },
  },
  { title: '字段', key: 'field' },
  { title: '排序', key: 'order', width: 110 },
  {
    title: '',
    key: 'action',
    width: 80,
    cellStyle: {
      border: 'none',
      backgroundColor: 'unset',
    },
  },
]

export const orderOptions = shallowReadonly([
  { value: 'asc', label: '升序' },
  { value: 'desc', label: '降序' },
  { value: 'custom', label: '自定义' },
])
