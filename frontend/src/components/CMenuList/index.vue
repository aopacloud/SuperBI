<template>
  <div class="list">
    <div
      class="item"
      v-for="item in list"
      :key="item.value"
      :disabled="item.disabled"
      :class="{
        active: isActive(item.value),
        disabled: item.disabled,
      }"
      @click.stop="handleClick(item)">
      <slot v-bind="item">
        <span>{{ item.label }}</span>
      </slot>

      <template v-if="item.children && item.children.length">
        <RightOutlined style="position: absolute; right: 8px; top: 8px" />

        <CMenuList
          :style="{ width: parseInt(item.width) + 'px' }"
          :list="item.children"
          :value="value.slice(1)"
          @change="e => handleSubListClick(e, item.value)" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { RightOutlined } from '@ant-design/icons-vue'

defineOptions({
  name: 'CMenuList',
})

const emits = defineEmits(['change'])
const props = defineProps({
  list: {
    type: Array,
    default: () => [],
  },
  value: {
    type: Array,
    default: () => [],
  },
})

const isActive = val => {
  return props.value.includes(val)
}

const handleClick = item => {
  if (item.disabled) return

  const [a, ...c] = props.value
  emits('change', [item.value, ...c])
}

const handleSubListClick = (e, pValue) => {
  emits('change', [pValue, ...e])
}
</script>

<style lang="scss" scoped>
.list {
  display: block;
  min-width: 80px;
}

.item {
  position: relative;
  padding: 5px 12px 5px 30px;
  cursor: pointer;
  transition: all 0.2s;
  &.disabled {
    color: rgba(0, 0, 0, 0.25);
    cursor: not-allowed;
  }
  &.active {
    @extend .selected-with-icon-left;
  }
  &:not(.disabled):hover {
    background-color: rgba(0, 0, 0, 0.04);

    & > .list {
      display: block;
    }
  }
  & > .list {
    display: none;
    position: absolute;
    left: 100%;
    transform: translateX(10px);
    top: 0;
    padding: 4px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12),
      0 9px 28px 8px rgba(0, 0, 0, 0.05);
    &::before {
      content: '';
      position: absolute;
      width: 20px;
      height: 100%;
      right: 100%;
      opacity: 0;
    }
  }
}
</style>
