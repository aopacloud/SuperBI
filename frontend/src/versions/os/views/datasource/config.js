// 数据源列表表列配置
export const dataSourceListTableColumns = [
  {
    title: '数据表',
    dataIndex: 'tableName',
  },
  {
    title: '数据集数量',
    dataIndex: 'datasetCount',
    width: 150,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 150,
  },
]

export const datasourceTableColumns = [
  {
    title: '数据集',
    dataIndex: 'name',
    width: 240,
  },
  {
    title: '描述',
    dataIndex: 'description',
    width: 340,
  },
  {
    title: '创建人',
    dataIndex: 'creatorAlias',
    width: 200,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: 200,
  },
]

export const datasetColumns = [
  {
    title: '字段名',
    dataIndex: 'name',
    width: 220,
  },
  {
    title: '字段描述',
    dataIndex: 'description',
    width: 320,
  },
  {
    title: '字段类型',
    dataIndex: 'databaseDataType',
    width: 120,
  },
]
