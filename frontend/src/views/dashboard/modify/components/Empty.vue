<template>
  <div class="empty-wrapper">
    <section class="empty-section">
      <main class="content">
        <slot name="image">
          <img
            class="img"
            src="@/views/dashboard/assets/image/empty.svg"
            width=""
            height="80" />
        </slot>
        <slot name="desc"><div class="desc">尚未添加图表</div></slot>
      </main>

      <slot></slot>
    </section>
  </div>
</template>

<script setup>
const props = defineProps({
  detail: {
    type: Object,
    default: () => ({}),
  },
  mode: {
    type: Number,
    default: 0,
    validator: s => {
      // 0 查看，1 编辑，2 预览
      return [0, 1, 2].includes(s)
    },
  },
})
</script>

<style lang="scss" scoped>
.empty-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
  justify-content: center;
  align-items: center;
}
.empty-section {
  display: inline-block;
  min-width: 300px;
  padding: 16px 24px;
  background-color: #fff;
  box-shadow: 0 1px 2px -8px rgba(0, 0, 0, 0.16), 0 3px 6px -4px rgba(0, 0, 0, 0.12),
    0 5px 12px -2px rgba(0, 0, 0, 0.09);
  border-radius: 8px;
  text-align: center;
  transition: box-shadow 0.2s;
  &:hover {
    box-shadow: 0 1px 2px -2px rgba(0, 0, 0, 0.16), 0 3px 6px 0 rgba(0, 0, 0, 0.12),
      0 5px 12px 4px rgba(0, 0, 0, 0.09);
  }
}
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.desc {
  margin: 16px 0;
  font-size: 14px;
  color: #666;
}
</style>
