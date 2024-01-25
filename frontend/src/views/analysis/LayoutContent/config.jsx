import { Badge, Tooltip } from 'ant-design-vue'
import { InfoCircleOutlined } from '@ant-design/icons-vue'

const STATUS = {
  SUCCESS: ['success', 'SUCCESS'],
  FAILED: ['error', 'FAIL'],
}

// 历史查询表格列
export const historyTableColums = [
  {
    title: '查询时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '查询代码',
    dataIndex: 'sql',
    minWidth: 300,
  },
  {
    title: '查询状态',
    dataIndex: 'status',
    width: 120,
    customRender: ({ text, record }) => {
      const [status, txt] = STATUS[text] || []

      return [
        <div>
          <Badge status={status} text={txt} />
          {record.status === 'FAILED' ? (
            <Tooltip
              title={record.errorLog || record.status}
              overlayStyle={{ maxWidth: '800px' }}>
              <InfoCircleOutlined style='margin-left: 10px' />
            </Tooltip>
          ) : null}
        </div>,
      ]
    },
  },
  {
    title: '查询耗时',
    dataIndex: 'elapsed',
    width: 100,
  },
  {
    title: '操作',
    dataIndex: 'action',
    fixed: 'right',
    align: 'center',
    width: 130,
  },
]
