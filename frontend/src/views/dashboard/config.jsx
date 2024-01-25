import { Badge } from 'ant-design-vue'

export const statusMap = {
  UN_PUBLISH: ['default', '未发布'],
  ONLINE: ['success', '已发布'],
  OFFLINE: ['default', '已下线'],
}

export const tableColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    width: 260,
    sorter: true,
    fixed: 'left',
  },
  {
    title: '描述',
    dataIndex: 'description',
    width: 300,
    sorter: true,
  },
  {
    title: '图表数量',
    dataIndex: 'reportCount',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 120,
    customRender: ({ text }) => {
      const [status, txt] = statusMap[text] || []

      return <Badge status={status} text={txt} />
    },
  },
  {
    title: '创建人',
    dataIndex: 'creatorAlias',
    width: 200,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 200,
    sorter: true,
  },
  {
    title: '更新人',
    dataIndex: 'operatorAlias',
    width: 200,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: 200,
    sorter: true,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 120,
    align: 'right',
    fixed: 'right',
  },
]
