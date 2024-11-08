<script lang="jsx">
import { shallowRef } from 'vue'
import { defineComponent } from 'vue'

export default defineComponent({
  props: {
    size: {
      type: String,
      default: 'small',
      validator: value => ['small', 'middle', 'large'].includes(value),
    },
    bordered: {
      type: Boolean,
      default: false,
    },
    align: {
      type: String,
      default: 'left',
      validator: value => ['left', 'center', 'right'].includes(value),
    },
    rowKey: {
      type: String,
      default: 'id',
    },
    columns: {
      type: Array,
      default: () => [],
    },
    dataSource: {
      type: Array,
      default: () => [],
    },
    minColumnWidth: {
      type: Number,
      default: 30,
    },
  },

  setup(props, { slots }) {
    const { size, bordered, minColumnWidth, columns, dataSource } = props

    const getCellStyle = col => {
      return {
        width: col.width + 'px',
        textAlign: col.align,
        ...col.cellStyle,
      }
    }

    const renderTHead = list => {
      const createTr = cols => <div class='tr'>{cols.map(renderTh)}</div>
      const renderTh = col => {
        return (
          <div class='th' style={getCellStyle(col)}>
            {col.title}
          </div>
        )
      }

      return <div class='thead'>{createTr(list)}</div>
    }

    const renderTBody = (list, columns) => {
      const cellRender = slots.cellRender

      const createTr = rows => {
        return rows.map((row, rowIndex) => {
          return (
            <div class='tr'>
              {columns.map((column, columnIndex) =>
                renderTd({ row, rowIndex, column, columnIndex })
              )}
            </div>
          )
        })
      }
      const renderTd = ({ row, rowIndex, column, columnIndex }) => {
        const customRender = column.customRender
        const text = row[column.key]
        const payload = {
          text,
          rowIndex,
          record: row,
          column,
          columnIndex,
        }
        return (
          <div class='td' style={getCellStyle(column)}>
            {cellRender
              ? cellRender(text, payload)
              : customRender
              ? customRender(text, payload)
              : row[column.key]}
          </div>
        )
      }

      return <div class='tbody'>{createTr(list)}</div>
    }

    const renderColumns = shallowRef([])

    const initColumnsWithWidth = () => {
      renderColumns.value = columns.map(col => {
        const width = col.width ? Math.max(col.width, minColumnWidth) : undefined
        return {
          ...col,
          width,
        }
      })
    }

    return () => {
      initColumnsWithWidth()
      const tHead = renderTHead(renderColumns.value)
      const tBody = renderTBody(props.dataSource, renderColumns.value)
      return (
        <div class={['table', size, { bordered }]}>
          {tHead}
          {tBody}
        </div>
      )
    }
  },
})
</script>

<style lang="scss" scoped>
$borderColor: #f0f0f0;
$tHeaderBgColor: #fafafa;

.table {
  display: inline-block;
  &:not(.bordered) {
    .thead {
      border-bottom: 1px solid $borderColor;
    }
  }
  &.bordered {
    border-radius: 4px;

    .tr + .tr {
      margin-top: -1px;
    }
    .th,
    .td {
      border: 1px solid $borderColor;
    }
  }
  &.small {
    .th,
    .td {
      padding: 4px 8px;
    }
  }
  &.large {
    .th,
    .td {
      padding: 14px 8px;
    }
  }
  &.left {
    .th,
    .td {
      text-align: left;
    }
  }
  &.center {
    .th,
    .td {
      text-align: center;
    }
  }
  &.right {
    .th,
    .td {
      text-align: right;
    }
  }
}

.thead {
  .th {
    background-color: $tHeaderBgColor;
  }
}
.tbody {
  margin-top: -1px;
}
.tr {
  display: table;
  table-layout: fixed;
  border-collapse: collapse;
  width: 100%;
  & + .tr {
    .th,
    .td {
      border-top: 1px solid $borderColor;
    }
  }
}
.th {
  font-weight: 400;
}
.th,
.td {
  display: table-cell;
  vertical-align: middle;
  padding: 10px 8px;
  word-break: break-all;
  border: 1px solid transparent;
}
.border {
  border: 1px solid #e8eaec;
}
.tr + .tr .td.border {
  margin-top: -1px;
}
.th,
.td {
  & + & {
    margin-left: -1px;
  }
}
</style>
