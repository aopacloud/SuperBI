<template>
  <section class="setting-layout">
    <header class="setting-layout-header">
      <slot name="title">{{ title }}</slot>
    </header>
    <main class="setting-layout-main"><slot /></main>
  </section>
</template>
<script setup>
defineProps({
  title: String
})
</script>
<style lang="scss" scoped>
.setting-layout {
  display: flex;
  flex-direction: column;
  padding: 10px;
  overflow: auto;
  & + & {
    margin-top: 12px;
  }
}

.setting-layout-header {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}
.setting-layout-main {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
  & > :deep(.ant-collapse) {
    flex: 1;
    overflow: auto;
  }
}
</style>
