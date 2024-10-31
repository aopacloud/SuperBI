<script lang="jsx">
import { h, computed, inject, getCurrentInstance } from 'vue'
import { Compact, Space, Button } from 'ant-design-vue'
import {
  PlusOutlined,
  MinusOutlined,
  CopyOutlined,
  FilterOutlined,
} from '@ant-design/icons-vue'

const getRootParentByCurrent = (component, cb) => {
  if (
    component &&
    component.type.name === 'RelationList' &&
    (cb ? cb(component) : true)
  ) {
    return component
  }

  if (component && component.parent) {
    return getRootParentByCurrent(component.parent, cb)
  }
}

export default {
  inject: ['relation'],
  props: {
    index: {
      type: Number,
      default: 0,
    },
    level: {
      type: Number,
      default: 1,
    },
    item: {
      type: Object,
      default: () => ({}),
    },
  },

  setup(props, { emit }) {
    // 第一层级的所有孙子节点的长度，
    const { allLength } = inject('relation')

    // 父级根组件
    const rootParent = getRootParentByCurrent(
      getCurrentInstance(),
      component => component.props.level === 1
    )
    // 父级根组件的props、slots
    const { props: rootParentProps, slots: rootParentSlots } = rootParent

    // 父级根组件的item、action插槽
    const { item: rootItemSlot, action: rootActionSlot } = rootParentSlots

    const addHandler = () => emit('add')

    const copyHandler = () => emit('copy')

    const splitHandler = () => emit('split')

    const deleteHandler = () => emit('delete')

    // 父级根组件的maxLevel - 计算可操作性, 是否可拆分，是否可复制
    const { maxLevel, max, size, splitable, copyable, hasRelation } = rootParentProps

    // 是否可添加
    const addable = computed(() => !max || allLength.value < max)

    // 是否可向下拆分 - 当前层级是否小于最大层级
    const itemSplitable = computed(
      () => addable.value && (!maxLevel || props.level < maxLevel)
    )

    // 可删除
    const deletable = computed(() => allLength.value > 1)

    function defaultAction() {
      return (
        <Space size={0} style='margin-left: 6px'>
          <Button
            icon={h(PlusOutlined)}
            type='text'
            disabled={!addable.value}
            onClick={addHandler}
          />

          {copyable ? (
            <Button
              icon={h(CopyOutlined)}
              type='text'
              disabled={!addable.value}
              onClick={copyHandler}
            />
          ) : null}

          {splitable && hasRelation ? (
            <Button
              icon={h(FilterOutlined)}
              type='text'
              disabled={!itemSplitable.value}
              onClick={splitHandler}
            />
          ) : null}

          <Button
            icon={h(MinusOutlined)}
            type='text'
            disabled={!deletable.value}
            onClick={deleteHandler}
          />
        </Space>
      )
    }

    return () => {
      const { item, index } = props

      const itemSlot = rootItemSlot?.(item, index)
      const actionSlot =
        rootActionSlot?.({
          index,
          add: addHandler,
          copy: copyHandler,
          split: splitHandler,
          del: deleteHandler,
          addable: addable.value,
          deletable: deletable.value,
          splitable: itemSplitable.value,
        }) || defaultAction()

      return (
        <Compact class='relation-item' block size={size}>
          {itemSlot}
          {actionSlot}
        </Compact>
      )
    }
  },
}
</script>
