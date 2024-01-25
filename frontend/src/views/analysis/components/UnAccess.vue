<template>
  <div class="unaccess">
    <img src="@/assets/svg/chartBox_unaccess.svg" style="max-width: 400px" />

    <a-button
      type="primary"
      :disabled="!dataset.enableApply || dataset.applying"
      @click="toApply">
      {{ dataset.applying ? '权限申请中' : '申请权限' }}
    </a-button>
  </div>

  <ApplyModal v-model:open="applyModalOpen" :initData="dataset" @ok="onApplyOk" />
</template>

<script setup>
import ApplyModal from '@/components/ApplyModal/index.vue'

const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({}),
  },
})

const applyModalOpen = ref(false)
const toApply = () => {
  applyModalOpen.value = true
}
const onApplyOk = e => {
  props.dataset.applying = true
}
</script>

<style lang="scss" scoped>
.unaccess {
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: center;
  align-items: center;
}
</style>
