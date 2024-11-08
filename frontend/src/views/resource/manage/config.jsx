import { Badge } from 'ant-design-vue'
import useAppStore from '@/store/modules/app'
import { STATUS as datasetStatus } from '@/views/dataset/config'

export const resourceTypeMap = {
  DATASET: '数据集',
  DASHBOARD: '看板',
  REPORT: '图表'
}

export const statusMap = {
  DATASET: Object.keys(datasetStatus).map(key => ({
    text: datasetStatus[key][1],
    value: key
  }))
}

export const datasetTableColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    sorter: true,
    fixed: 'left',
    width: 300
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
    title: '状态',
    dataIndex: 'status',
    customFilterDropdown: true,
    // filters: statusMap.DATASET,
    customRender({ text }) {
      if (!text) return '-'

      const [status, txt] = datasetStatus[text] || []
      return <Badge status={status} text={txt} />
    },
    width: 120
  },
  {
    title: '图表数量',
    dataIndex: 'reportNum',
    sorter: true,
    width: 120
  },
  {
    title: '授权人数',
    dataIndex: 'authNum',
    sorter: true,
    width: 120
  },
  {
    title: '近30天查询人数',
    dataIndex: 'queryNum',
    sorter: true,
    width: 160
  },
  {
    title: '空间',
    dataIndex: 'workspaceIds',
    customFilterDropdown: true,
    customRender: ({ record }) =>
      useAppStore().workspaceList.find(t => t.id === record.workspaceId)?.name,
    // filters: () =>
    //   useAppStore().workspaceList.map(t => ({ text: t.name, value: t.id })),
    width: 200
  },
  {
    title: '更新时间',
    dataIndex: 'sourceUpdateTime',
    sorter: true,
    width: 260
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    align: 'right',
    width: 180
  }
]

export const dashboardTableColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    sorter: true,
    fixed: 'left',
    width: 300
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
    title: '状态',
    dataIndex: 'status',
    filters: statusMap.DATASET,
    customRender({ text }) {
      if (!text) return '-'

      const [status, txt] = datasetStatus[text] || []
      return <Badge status={status} text={txt} />
    },
    width: 120
  },
  {
    title: '数据集数量',
    dataIndex: 'datasetNum',
    sorter: true,
    width: 120
  },
  {
    title: '图表数量',
    dataIndex: 'reportNum',
    sorter: true,
    width: 120
  },
  {
    title: '授权人数',
    dataIndex: 'authNum',
    sorter: true,
    width: 120
  },
  {
    title: '近30天访问人数',
    dataIndex: 'visitNum',
    sorter: true,
    width: 160
  },
  {
    title: '空间',
    dataIndex: 'workspaceIds',
    customFilterDropdown: true,
    customRender: ({ record }) =>
      useAppStore().workspaceList.find(t => t.id === record.workspaceId)?.name,
    // filters: () =>
    //   useAppStore().workspaceList.map(t => ({ text: t.name, value: t.id })),
    width: 200
  },
  {
    title: '更新时间',
    dataIndex: 'sourceUpdateTime',
    sorter: true,
    width: 260
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    align: 'right',
    width: 180
  }
]

export const reportTableColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    sorter: true,
    fixed: 'left',
    width: 300
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
    title: '数据集',
    dataIndex: 'datasetName',
    width: 180
  },
  {
    title: '应用看板',
    dataIndex: 'dashboards',
    customFilterDropdown: true,
    width: 180
  },
  {
    title: '空间',
    dataIndex: 'workspaceIds',
    customFilterDropdown: true,
    customRender: ({ record }) =>
      useAppStore().workspaceList.find(t => t.id === record.workspaceId)?.name,
    // filters: () =>
    //   useAppStore().workspaceList.map(t => ({ text: t.name, value: t.id })),
    width: 200
  },
  {
    title: '更新时间',
    dataIndex: 'sourceUpdateTime',
    sorter: true,
    width: 260
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    align: 'right',
    width: 180
  }
]

export const tableColumns = []
