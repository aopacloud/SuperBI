<template>
  <div class="logo" href="/">
    <img class="logo-img" src="@/assets/images/logo.png" alt="logo" />
  </div>
</template>

<script setup>
import { CaretDownOutlined } from '@ant-design/icons-vue'
</script>

<style lang="scss" scoped>
.logo {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  height: inherit;
  max-width: 140px;
  font-size: 18px;
  color: #fff;
}
.logo-img {
  max-height: 50%;
}
</style>
