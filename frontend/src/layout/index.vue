<template>
  <a-layout class="app-layout">
    <a-layout-header
      :style="{
        height: UINavbar.height,
        lineHeight: UINavbar.height,
        padding: '0 10px',
        backgroundColor: UINavbar.backgroundColor,
        color: UINavbar.color,
      }">
      <Navbar />
    </a-layout-header>

    <a-layout>
      <LayoutComponentsSidebar />

      <a-layout>
        <a-layout-header
          style="height: initial; padding: 0; background-color: transparent">
        </a-layout-header>

        <a-layout-content>
          <AppMain />
        </a-layout-content>
      </a-layout>
    </a-layout>

    <LayoutComponentsReleasesNotify />
  </a-layout>
</template>

<script setup>
import { computed } from 'vue'
import Navbar from './components/Navbar/index.vue'
import AppMain from './components/AppMain/index.vue'
import { UIStyle } from '@/settings'
import { versionVue } from '@/versions'

const { LayoutComponentsSidebar, LayoutComponentsReleasesNotify } = versionVue

const UINavbar = computed(() => UIStyle.navbar)
</script>

<style lang="scss" scoped>
.app-layout {
  height: 100%;
}
</style>
