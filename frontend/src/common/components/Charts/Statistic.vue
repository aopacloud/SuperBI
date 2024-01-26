<template>
  <div class="statistic">
    <div class="title">
      <slot name="title">{{ title }}</slot>
    </div>
    <div class="sub-title" v-if="subTitle || $slots['sub-title']">
      <slot name="sub-title"> {{ subTitle }}</slot>
    </div>
    <div class="value">
      <slot>{{ value }}</slot>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  title: {
    type: String,
    default: '标题',
  },
  subTitle: {
    type: String,
  },
  value: {
    type: [String, Number],
    default: '-',
  },
  options: {
    type: Function,
  },
})
</script>

<style lang="scss" scoped>
.statistic {
  width: 100%;
  height: 100%;
  display: inline-flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 90px;
  min-width: 160px;
  .title {
    font-size: 16px;
    color: #666;
  }
  .sub-title {
    margin-top: 8px;
    font-size: 14px;
    color: #999;
  }
  .value {
    font-size: 30px;
    font-weight: 600;
    color: #333;
  }

  .title + .value,
  .sub-title + .value {
    margin-top: 12px;
  }
}
</style>
