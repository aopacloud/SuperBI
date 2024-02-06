export const tableColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    width: 260,
  },
  {
    title: '描述',
    dataIndex: 'description',
    width: 300,
  },
  {
    title: '数据集',
    dataIndex: 'datasetName',
    width: 260,
  },
  {
    title: '应用看板',
    dataIndex: 'dashboardCount',
    width: 100,
  },
  {
    title: '创建人',
    dataIndex: 'creator',
    width: 220,
    sorter: true,
    customRender: ({ column, record }) => {
      return record[column.dataIndex + 'Alias']
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 200,
    sorter: true,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 110,
    align: 'right',
    fixed: 'right',
  },
]
