<template>
  <div class="section">
    <img src="@/assets/svg/chartBox_unaccess.svg" style="width: 200px" />

    <p class="font-help">
      很抱歉，
      {{ dataset.expired ? '图表的权限已过期' : '您没有图表的权限' }}
      ，请先去申请数据集 [{{ dataset.name }}] 的权限
    </p>
    <a-button
      type="primary"
      :disabled="!dataset.enableApply || dataset.applying"
      @click="emits('apply')">
      {{ dataset.applying ? '权限申请中' : '申请权限' }}
    </a-button>
  </div>
</template>

<script setup>
const emits = defineEmits(['apply'])
const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({}),
  },
})
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.font-help {
  margin: 0 0 24px;
  color: #999;
}
</style>
