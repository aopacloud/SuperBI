<template>
  <Watermark
    class="watermark-wrapper"
    :zIndex="99999"
    :content="watermarkContent"
    :font="watermarkFontStyle"
  >
    <section id="page-wrapper" class="page-content">
      <!-- <Access> -->
      <router-view />
      <!-- </Access> -->
    </section>
  </Watermark>
</template>

<script setup>
import { reactive, computed } from 'vue'
import { Watermark } from 'ant-design-vue'
import Access from './Access.vue'
import useUserStore from '@/store/modules/user'
import dayjs from 'dayjs'

const userStore = useUserStore()

const watermarkContent = computed(() => {
  return [userStore.userInfo.aliasName, dayjs().format('YYYY-MM-DD HH:mm:ss')]
})
const watermarkFontStyle = reactive({
  fontSize: 12,
  color: 'rgba(0, 0, 0, 0.05)'
})
</script>

<style lang="scss" scoped>
.watermark-wrapper {
  width: 100%;
  height: 100%;
}
.page-content {
  position: relative;
  height: 100%;
  background-color: #fff;
  overflow: auto;
}
</style>
