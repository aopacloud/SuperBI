<template>
  <a-tree
    checkable
    :disabled="disabled"
    :selectable="false"
    :field-names="{
      key: 'id',
      title: 'name',
    }"
    :treeData="treeData"
    v-model:expandedKeys="expandedKeys"
    v-model:checkedKeys="modelValue"
    @check="onCheck">
  </a-tree>
</template>

<script setup>
import { ref, watch, watchEffect } from 'vue'
import { deepClone } from 'common/utils/help'

const emits = defineEmits(['update:value'])

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false,
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
  value: {
    type: Array,
    default: () => [],
  },
})

const treeData = ref([])
const expandedKeys = ref([])

const modelValue = ref([])
const halfCheckedValue = ref([])

watch(modelValue, e => {
  emits('update:value', e)
})

const getData = () => {
  return [...modelValue.value, ...halfCheckedValue.value]
}
defineExpose({ getData })

const onCheck = (v, evt) => {
  halfCheckedValue.value = evt.halfCheckedKeys
}

const _listToTree = list => {
  const res = []

  for (let i = 0; i < list.length; i++) {
    const item = list[i]
    const resItem = res.find(t => t.id === item.id)
    const parentItem = list.find(t => t.id === item.parentId)

    if (!resItem) {
      if (!parentItem) {
        res.push(item)
      } else {
        if (typeof parentItem.children === 'undefined') {
          expandedKeys.value.push(parentItem.id)
          parentItem.children = []
        }

        parentItem.children.push(item)
      }
    }
  }

  return res
}

const _sort = (list, key = 'sort') => {
  return list
    .map(t => {
      const children = t.children || []

      return {
        ...t,
        children: _sort(children),
      }
    })
    .sort((a, b) => a[key] - b[key])
}

const _transformValues = (list = [], value = []) => {
  return value.reduce(
    (acc, cur) => {
      const item = list.find(t => t.id === cur)
      if (!item) return acc

      const parent = list.find(t => t.id === item.parentId)
      if (parent) {
        acc[1].push(parent.id)
      }

      acc[0].push(item.id)

      acc[1] = [...new Set(acc[1])]
      acc[0] = acc[0].filter(t => !acc[1].includes(t))

      return acc
    },
    [[], []]
  )
}

watchEffect(() => {
  const deepCloned = deepClone(props.dataSource.sort((a, b) => a.id - b.id))

  expandedKeys.value = []
  treeData.value = _sort(_listToTree(deepCloned))

  const [v0, v1] = _transformValues(props.dataSource, props.value)

  modelValue.value = v0
  halfCheckedValue.value = v1
})
</script>
