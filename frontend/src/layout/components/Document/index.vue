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

const href = 'https://docs.google.com/document/d/1c5StTRki_2p1cBMrc5DAYUr9gf2e329m/'
</script>

<style lang="scss" scoped></style>
