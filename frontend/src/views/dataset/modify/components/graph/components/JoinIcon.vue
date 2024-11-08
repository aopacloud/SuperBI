<template>
  <a-tooltip :title="title">
    <SvgIcon
      :name="name"
      :style="{ backgroundColor: '#fff', fontSize: fontSize }"
      @click="onClick"
    />
  </a-tooltip>
</template>

<script setup>
import { computed } from 'vue'
import { joinOptions } from '../../../config'

const props = defineProps({
  edge: {
    type: Object,
    default: () => ({})
  },
  joinType: {
    type: String,
    default: 'LEFT'
  },
  size: {
    type: [String, Number],
    default: 24
  },
  onClick: Function
})

const fontSize = computed(() => parseInt(props.size) + 'px')

const map = {
  LEFT: 'leftJoin',
  INNER: 'innerJoin',
  FULL: 'allJoin',
  RIGHT: 'rightJoin'
}
const name = computed(() => map[props.joinType])
const title = computed(() => {
  const item = joinOptions.find(t => t.value === props.joinType)
  return item?.label ?? props.joinType
})

const onClick = () => {
  props.onClick && props.onClick(props.edge)
}
</script>

<style lang="scss" scoped></style>
