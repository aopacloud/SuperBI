import useAppStore from '@/store/modules/app'

export const resourceTypeMap = {
  DATASET: '数据集',
  DASHBOARD: '看板',
  REPORT: '图表'
}

export const tableColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    sorter: true,
    fixed: 'left',
    width: 400
  },
  {
    title: '负责人',
    dataIndex: 'creators',
    width: 240,
    customFilterDropdown: true,
    customRender: ({ record }) => {
      return record.creatorAlias
    }
  },
  {
    title: '剩余天数',
    dataIndex: 'days',
    width: 180
  },
  {
    title: '所属空间',
    dataIndex: 'workspaceName',
    width: 240,
    dataIndex: 'workspaceIds',
    customRender: ({ record }) =>
      useAppStore().workspaceList.find(t => t.id === record.workspaceId)?.name,
    filters: () =>
      useAppStore().workspaceList.map(t => ({ text: t.name, value: t.id }))
  },
  {
    title: '删除时间',
    dataIndex: 'updateTime',
    width: 260,
    sorter: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 120,
    align: 'right',
    fixed: 'right'
  }
]
