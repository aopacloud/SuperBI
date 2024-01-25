<template>
  <Exeption v-if="isExeption" v-bind="exeptionObj" />
  <slot v-else></slot>
</template>

<script setup>
import { computed } from 'vue'
import Exeption from '@/views/exeption/index.vue'
import useAppStore from '@/store/modules/app'

const appStore = useAppStore()

const isExeption = computed(() => appStore.unAccessable)
const exeptionObj = computed(() => appStore.unAccessableInfo)
</script>

<style lang="scss" scoped></style>
