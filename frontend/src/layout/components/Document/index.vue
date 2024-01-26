<template>
  <div>
    <a-tooltip title="帮助文档">
      <a target="_blank" style="display: block; color: inherit" :href="href">
        <ReadOutlined style="font-size: 20px" />
      </a>
    </a-tooltip>
  </div>
</template>

<script setup>
import { ReadOutlined } from '@ant-design/icons-vue'
import { versionJs } from '@/versions'

const href = versionJs.LayoutDocument.documentSrc
</script>

<style lang="scss" scoped></style>
