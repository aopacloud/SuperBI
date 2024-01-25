<template>
  <ConfigProvider :locale="locale === 'en' ? enUS : zhCN">
    <router-view />
  </ConfigProvider>
</template>

<script setup>
import { ref } from 'vue'
import { StyleProvider, ConfigProvider } from 'ant-design-vue'
import enUS from 'ant-design-vue/es/locale/en_US'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
dayjs.locale(zhCN.locale)

const locale = ref(zhCN.locale)
</script>
