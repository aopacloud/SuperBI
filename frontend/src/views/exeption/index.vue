<template>
  <a-result class="unaccess" v-bind="attrs" :status="status" :title="title" />
</template>

<script setup>
import { useAttrs } from 'vue'
import { versionJs } from '@/versions'

const attrs = useAttrs()

const props = defineProps({
  status: {
    type: [Number, String],
    default: 403,
  },
  title: {
    type: String,
    default: `未开通系统权限，${versionJs.Permission.contactText}`,
  },
})
</script>

<style lang="scss" scoped>
.unaccess {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  :deep(.ant-result-title) {
    margin-top: 32px;
  }
  :deep(.ant-result-image) {
    margin: initial;
  }
}
</style>
