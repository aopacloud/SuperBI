export const MEMBER_TABLE_COLUMNS = [
  {
    title: '用户',
    dataIndex: 'aliasName',
    width: 260,
  },
  {
    title: '角色',
    dataIndex: 'sysRoleId',
    width: 120,
  },
  {
    title: '添加时间',
    dataIndex: 'joinTime',
    width: 200,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 120,
    fixed: 'right',
  },
]

export const ROLE_TABLE_COLUMNS = [
  {
    title: '成员组',
    dataIndex: 'name',
    key: 'name',
    width: 260,
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    width: 400,
    customRender: ({ value }) => {
      return <div style='word-break: break-all'>{value}</div>
    },
  },
  {
    title: '成员数量',
    dataIndex: 'userNum',
    key: 'userNum',
    width: 200,
  },
  {
    title: '创建人',
    dataIndex: 'creatorAliasName',
    key: 'creatorAliasName',
    width: 200,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 200,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

export const timezoneList = [
  {
    label: 'UTC-12:00',
    value: '-12',
  },
  {
    label: 'UTC-11:00',
    value: '-11',
  },
  {
    label: 'UTC-10:00',
    value: '-10',
  },
  {
    label: 'UTC-09:00',
    value: '-9',
  },
  {
    label: 'UTC-08:00',
    value: '-8',
  },
  {
    label: 'UTC-07:00',
    value: '-7',
  },
  {
    label: 'UTC-06:00',
    value: '-6',
  },
  {
    label: 'UTC-05:00',
    value: '-5',
  },
  {
    label: 'UTC-04:00',
    value: '-4',
  },
  {
    label: 'UTC-03:00',
    value: '-3',
  },
  {
    label: 'UTC-02:00',
    value: '-2',
  },
  {
    label: 'UTC-01:00',
    value: '-1',
  },
  {
    label: 'UTC+01:00',
    value: '+1',
  },
  {
    label: 'UTC+02:00',
    value: '+2',
  },
  {
    label: 'UTC+03:00',
    value: '+3',
  },
  {
    label: 'UTC+04:00',
    value: '+4',
  },
  {
    label: 'UTC+05:00',
    value: '+5',
  },
  {
    label: 'UTC+06:00',
    value: '+6',
  },
  {
    label: 'UTC+07:00',
    value: '+7',
  },
  {
    label: 'UTC+08:00',
    value: '+8',
  },
  {
    label: 'UTC+09:00',
    value: '+9',
  },
  {
    label: 'UTC+10:00',
    value: '+10',
  },
  {
    label: 'UTC+11:00',
    value: '+11',
  },
  {
    label: 'UTC+12:00',
    value: '+12',
  },
  {
    label: 'UTC+13:00',
    value: '+13',
  },
  {
    label: 'UTC+14:00',
    value: '+14',
  },
]

export const memberLevelList = [
  {
    label: '空间管理员',
    value: 'ADMIN',
  },
  {
    label: '数据生产者',
    value: 'WORKER',
  },
  {
    label: '业务分析人员',
    value: 'ANALYZER',
  },
]
