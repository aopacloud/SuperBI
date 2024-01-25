export const tableColumns = [
  {
    title: 'Id',
    dataIndex: 'id',
    width: 80,
    sorter: true,
    fixed: 'left',
  },
  {
    title: '账号',
    dataIndex: 'username',
    ellipsis: true,
    sorter: true,
    fixed: 'left',
  },
  {
    title: '显示名',
    dataIndex: 'aliasName',
    ellipsis: true,
    sorter: true,
  },
  {
    title: '加入时间',
    dataIndex: 'createTime',
    width: 200,
    sorter: true,
  },
  {
    title: '最近访问时间',
    dataIndex: 'lastOnlineTime',
    width: 200,
    sorter: true,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 180,
    align: 'right',
    fixed: 'right',
  },
]
