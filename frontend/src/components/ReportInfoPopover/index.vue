<script lang="jsx">
import { defineComponent } from 'vue'
import { Popover as APopover } from 'ant-design-vue'

const listKeys = {
  name: '名称',
  creatorAlias: '创建人',
  createTime: '创建时间',
  operatorAlias: '更新人',
  updateTime: '更新时间',
  datasetName: '数据集',
  datasourceName: '数据源',
  description: '描述'
}

export default defineComponent({
  props: {
    chart: {
      type: Object,
      default: () => ({})
    },
    dataset: {
      type: Object,
      default: () => ({})
    }
  },
  setup(props, ctx) {
    const isChart = computed(() => props.chart.id)

    const info = computed(() => (isChart.value ? props.chart : props.dataset))

    const createContent = () => {
      const items = Object.keys(listKeys)
        .filter(t => (isChart.value ? true : t !== 'datasetName'))
        .map(key => {
          let v = info.value[key]

          if (key === 'datasetName') {
            v = info.value.dataset.name
          }

          if (key === 'datasourceName') {
            const datasetInfo = isChart.value ? info.value.dataset : info.value
            v = datasetInfo.config?.refTables.split(',')
          }

          return (
            <div class='item'>
              <div class='item-label'>
                <span class='item-label-text'>{listKeys[key]}</span>
              </div>
              <div class='item-content'>{v}</div>
            </div>
          )
        })

      return (
        <div class='list' style='width: 300px'>
          {items}
        </div>
      )
    }

    return () => {
      const slotDefault = ctx.slots.default
      const slotContent = createContent()

      return (
        <APopover
          overlayClassName='report-info-popover'
          trigger='click'
          placement='left'
          arrow={false}
          content={slotContent}
        >
          {slotDefault()}
        </APopover>
      )
    }
  }
})
</script>

<style lang="scss" scoped>
.item {
  display: flex;
  & + & {
    margin-top: 4px;
  }
  &-label {
    width: 70px;
    text-align: right;
    &-text {
      color: rgba(0, 0, 0, 0.5);
    }
    &::after {
      content: ':';
    }
  }
  &-content {
    flex: 1;
    margin-left: 12px;
    word-break: break-word;
    overflow: auto;
  }
}
</style>
<style lang="scss">
.report-info-popover {
  .ant-popover-title {
    border-bottom: 1px solid #f0f0f0;
    margin: 0px -12px 6px;
    padding: 0 12px 4px;
  }
}
</style>
