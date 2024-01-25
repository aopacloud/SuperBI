<template>
  <a-modal
    title="行排序"
    :open="open"
    :confirm-loading="confirmLoading"
    @cancel="cancel"
    @ok="ok">
    <div class="tree-list">
      <li class="tree-item" v-for="(node, i) in list" :key="node.id">
        <div class="tree-item--title">
          <CaretRightOutlined
            class="action-icon"
            :class="{ open: node.open }"
            @click="toggle(node)" />
          {{ node.name }}
        </div>

        <draggable
          :class="['tree-item--children', node.open ? 'open' : '']"
          itemKey="id"
          :componentData="{
            name: 'fade',
            type: 'transition-group',
          }"
          :list="node.children">
          <template #item="{ element, index }">
            <div
              class="tree-item--child text-overflow"
              :key="element.id"
              :title="`${element.displayName}(${element.name})`">
              <span class="order-icon"></span>
              {{ `${element.displayName}(${element.name})` }}
            </div>
          </template>
        </draggable>
      </li>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, watch } from 'vue'
import draggable from 'vuedraggable'
import { CaretRightOutlined } from '@ant-design/icons-vue'
import { flat } from 'common/utils/help'

const emits = defineEmits(['update:open', 'ok'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  propertyField: {
    type: Object,
    default: () => {},
  },
  indexField: {
    type: Object,
    default: () => {},
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
})

const list = ref([])
const confirmLoading = ref(false)

watch(
  () => props.open,
  visible => {
    if (visible) {
      init()
    } else {
      confirmLoading.value = false
    }
  }
)

const init = () => {
  const pFiedls = props.dataSource.filter(t => t.category === props.propertyField.id)
  const iFields = props.dataSource.filter(t => t.category === props.indexField.id)

  list.value = [
    { ...props.propertyField, children: pFiedls, open: true },
    { ...props.indexField, children: iFields, open: true },
  ]
}

const cancel = () => {
  emits('update:open', false)
}
const ok = () => {
  confirmLoading.value = true

  setTimeout(() => {
    const res = flat(list.value)
      .filter(t => t.id !== props.propertyField.id && t.id !== props.indexField.id)
      .map((t, i) => {
        return {
          ...t,
          sort: i,
        }
      })

    emits('ok', res)
    cancel()
  }, 300)
}
const toggle = node => {
  const open = node.open

  node.open = !open
}
</script>

<style scoped lang="scss">
.tree-list {
  max-height: 600px;
  overflow: auto;
}
.tree-item {
  line-height: 26px;
  &--title {
    position: sticky;
    top: 0;
    padding: 0 10px;
    background-color: #fff;
  }
  &--children {
    display: none;
    margin-left: 20px;
    &.open {
      display: block;
    }
  }
  &--child {
    padding: 0 10px 0 12px;
    cursor: move;
    user-select: none;
    &:hover {
      background-color: #e6f7ff;
    }
    .order-icon {
      display: inline-block;
      width: 4px;
      height: 12px;
      margin-right: 3px;
      vertical-align: middle;
      border: 1px dashed #c8c8c8;
      border-top: none;
      border-bottom: none;
      color: #b8b8b8;
    }
  }
  .action-icon {
    margin-right: 5px;
    &.open {
      transform: rotate(90deg);
    }
  }
}
</style>
