export const approveStatusEnum = [
  {
    text: '已发起',
    value: 'INIT',
    color: 'blue',
  },
  {
    text: '审核中',
    value: 'UNDER_REVIEW',
    color: 'blue',
  },
  {
    text: '已通过',
    value: 'PASSED',
    color: 'green',
  },
  {
    text: '已驳回',
    value: 'REJECTED',
    color: 'red',
  },
  {
    text: '已撤回',
    value: 'DELETE',
    color: 'red',
  },
]

export const authorizeStatusEnum = [
  {
    text: '未授权',
    value: 'NOT_AUTHORIZED',
    color: 'blue',
  },
  {
    text: '已授权',
    value: 'AUTHORIZED',
    color: 'green',
  },
  {
    text: '已驳回',
    value: 'REJECTED',
    color: 'red',
  },
  {
    text: '已过期',
    value: 'EXPIRED',
    color: 'gray',
  },
]

export const myApplyUnderwayApplyColumns = [
  {
    title: '序号',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '申请数据集',
    dataIndex: 'datasetName',
    key: 'datasetName',
    width: 260,
  },
  {
    title: '申请理由',
    dataIndex: 'reason',
    key: 'reason',
    width: 260,
  },
  {
    title: '申请人',
    dataIndex: 'usernameAlias',
    key: 'usernameAlias',
    width: 200,
  },
  {
    title: '审批状态',
    dataIndex: 'approveStatus',
    key: 'approveStatus',
    width: 130,
  },
  {
    title: '发起时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 240,
    sorter: true,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 240,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

export const myApplyFinishApplyColumns = [
  {
    title: '序号',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '申请数据集',
    dataIndex: 'datasetName',
    key: 'datasetName',
    width: 260,
  },
  {
    title: '申请理由',
    dataIndex: 'reason',
    key: 'reason',
    width: 260,
  },
  {
    title: '申请人',
    dataIndex: 'usernameAlias',
    key: 'usernameAlias',
    width: 200,
  },
  {
    title: '审批状态',
    dataIndex: 'approveStatus',
    key: 'approveStatus',
    width: 130,
  },
  {
    title: '授权状态',
    dataIndex: 'authorizeStatus',
    key: 'authorizeStatus',
    width: 130,
  },
  {
    title: '数据集创建人',
    dataIndex: 'datasetCreatorAliasName',
    key: 'datasetCreatorAliasName',
    width: 200,
  },
  {
    title: '发起时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 240,
    sorter: true,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 240,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

export const myApproveUnderwayApplyColumns = [
  {
    title: '序号',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '申请数据集',
    dataIndex: 'datasetName',
    key: 'datasetName',
    width: 260,
  },
  {
    title: '申请理由',
    dataIndex: 'reason',
    key: 'reason',
    width: 260,
  },
  {
    title: '申请人',
    dataIndex: 'usernameAlias',
    key: 'usernameAlias',
    width: 200,
  },
  {
    title: '申请期限',
    dataIndex: 'expireDuration',
    key: 'expireDuration',
    width: 150,
  },
  {
    title: '审批状态',
    dataIndex: 'approveStatus',
    key: 'approveStatus',
    width: 130,
  },
  {
    title: '授权状态',
    dataIndex: 'authorizeStatus',
    key: 'authorizeStatus',
    width: 130,
    filters: authorizeStatusEnum,
  },
  {
    title: '发起时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 240,
    sorter: true,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 240,
  },
  {
    title: '操作',
    key: 'action',
    width: 240,
    fixed: 'right',
  },
]

export const myApproveFinishApplyColumns = [
  {
    title: '序号',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '申请数据集',
    dataIndex: 'datasetName',
    key: 'datasetName',
    width: 260,
  },
  {
    title: '申请理由',
    dataIndex: 'reason',
    key: 'reason',
    width: 120,
  },
  {
    title: '申请人',
    dataIndex: 'usernameAlias',
    key: 'usernameAlias',
    width: 200,
  },
  {
    title: '申请期限',
    dataIndex: 'expireDuration',
    key: 'expireDuration',
    width: 150,
  },
  {
    title: '审批状态',
    dataIndex: 'approveStatus',
    key: 'approveStatus',
    width: 130,
    filters: [
      {
        label: '已通过',
        text: '已通过',
        value: 'PASSED',
      },
      {
        label: '已驳回',
        text: '已驳回',
        value: 'REJECTED',
      },
      {
        label: '已撤回',
        text: '已撤回',
        value: 'DELETE',
      },
    ],
  },
  {
    title: '授权状态',
    dataIndex: 'authorizeStatus',
    key: 'authorizeStatus',
    width: 130,
    filters: authorizeStatusEnum,
  },
  {
    title: '发起时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 240,
    sorter: true,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 240,
  },
  {
    title: '操作',
    key: 'action',
    width: 260,
    fixed: 'right',
  },
]

export const applyManageUnderwayApplyColumns = [
  {
    title: '序号',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '申请数据集',
    dataIndex: 'datasetName',
    key: 'datasetName',
    width: 260,
  },
  {
    title: '申请理由',
    dataIndex: 'reason',
    key: 'reason',
    width: 260,
  },
  {
    title: '申请人',
    dataIndex: 'usernameAlias',
    key: 'usernameAlias',
    width: 200,
  },
  {
    title: '审批状态',
    dataIndex: 'approveStatus',
    key: 'approveStatus',
    width: 130,
  },
  {
    title: '发起时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 240,
    sorter: true,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 240,
    sorter: true,
  },
  {
    title: '数据集创建人',
    dataIndex: 'datasetCreatorAliasName',
    key: 'datasetCreatorAliasName',
    width: 200,
  },
  {
    title: '数据集空间',
    dataIndex: 'workspaceName',
    key: 'workspaceName',
    width: 200,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

export const applyManageFinishApplyColumns = [
  {
    title: '序号',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '申请数据集',
    dataIndex: 'datasetName',
    key: 'datasetName',
    width: 260,
  },
  {
    title: '申请理由',
    dataIndex: 'reason',
    key: 'reason',
    width: 260,
  },
  {
    title: '申请人',
    dataIndex: 'usernameAlias',
    key: 'usernameAlias',
    width: 200,
  },
  {
    title: '审批状态',
    dataIndex: 'approveStatus',
    key: 'approveStatus',
    width: 130,
    filters: [
      {
        label: '已通过',
        text: '已通过',
        value: 'PASSED',
      },
      {
        label: '已驳回',
        text: '已驳回',
        value: 'REJECTED',
      },
      {
        label: '已撤回',
        text: '已撤回',
        value: 'DELETE',
      },
    ],
  },
  {
    title: '授权状态',
    dataIndex: 'authorizeStatus',
    key: 'authorizeStatus',
    width: 130,
    filters: authorizeStatusEnum,
  },
  {
    title: '发起时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 240,
    sorter: true,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 240,
    sorter: true,
  },
  {
    title: '数据集创建人',
    dataIndex: 'datasetCreatorAliasName',
    key: 'datasetCreatorAliasName',
    width: 200,
  },
  {
    title: '数据集空间',
    dataIndex: 'workspaceName',
    key: 'workspaceName',
    width: 200,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

export const computeExpireDuration = (duration = 0) => {
  if (duration === 0) {
    return '永久'
  }
  return duration / 3600 / 24 + ' 天'
}

export const displayApproveStatus = status => {
  if (!status) return {}

  const { text, value, color } = approveStatusEnum.find(item => item.value === status)
  return { text, color }
}

export const displayAuthorizeStatus = status => {
  if (!status) return {}

  const { text, value, color } = authorizeStatusEnum.find(item => item.value === status)
  return { text, color }
}
