<template>
  <div class="tabs">
    <div class="tabs-content">
      <div
        class="tab"
        v-if="tabOnly !== 1"
        :class="{ active: tab === 0 }"
        @click="onTabClick(0)">
        动态时间
      </div>
      <div
        class="tab"
        v-if="tabOnly !== 0"
        :class="{ active: tab === 1 }"
        @click="onTabClick(1)">
        静态时间
      </div>
    </div>

    <div class="extra">
      <div class="desc">
        <slot name="desc">2023-08-17</slot>

        <CloseCircleOutlined
          v-show="value && value.length"
          class="clear-icon"
          @click="reset" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { CloseCircleOutlined } from '@ant-design/icons-vue'

const emits = defineEmits(['update:tab', 'reset'])
const props = defineProps({
  tab: {
    type: Number,
    default: 1,
  },
  value: {
    type: Array,
  },
  tabOnly: {
    type: Number,
  },
})

const onTabClick = key => {
  emits('update:tab', key)
}

const reset = () => {
  emits('reset')
}
</script>

<style lang="scss" scoped>
.tabs {
  display: flex;
  height: 36px;
  line-height: 36px;
  align-items: center;
  border-bottom: 1px solid #e8e8e8;
  font-size: 14px;
  &-content {
    display: flex;
    min-width: 210px;
  }
  .tab {
    padding: 0 12px;
    margin: 0 12px;
    border-bottom: 1px solid transparent;
    transition: all 0.2s;
    cursor: pointer;
    &.active {
      color: #1677ff;
      font-weight: 600;
      border-color: currentColor;
    }
  }
  .extra {
    display: flex;
    align-items: center;
    flex: 1;
    margin-left: 30px;
  }
  .desc {
    color: #aaa;
    &:hover {
      .clear-icon {
        display: inline-block;
      }
    }
    .clear-icon {
      display: none;
      margin-left: 12px;
      font-size: 16px;
      cursor: pointer;
    }
  }
}
</style>
