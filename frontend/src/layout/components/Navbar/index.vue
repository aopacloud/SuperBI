<template>
  <div class="navbar">
    <!-- Logo 应用直达 -->
    <LayoutComponentsNavbarLogo />

    <LayoutComponentsNavbarAppName />

    <!-- 顶部导航 -->
    <Menus class="navs" />

    <!-- 工具栏 -->
    <div class="tools">
      <!-- 集群切换 -->
      <LayoutComponentsCluster class="tool-item" style="text-align: left" />

      <!-- 空间切换 -->
      <Workspace class="tool-item" style="text-align: left" />

      <!-- 消息通知 -->
      <LayoutComponentsNavbarNotification class="tool-item hover" />

      <!-- 文档 -->
      <Document class="tool-item hover" />

      <!-- 用户下拉菜单 -->
      <UserDropdown class="tool-item hover" />
    </div>
  </div>
</template>

<script setup>
import Menus from './Menus.vue'
import Workspace from '../Workspace/index.vue'
import Document from '../Document/index.vue'
import UserDropdown from '../UserDropdown/index.vue'
import { versionVue } from '@/versions'

const {
  LayoutComponentsNavbarLogo,
  LayoutComponentsNavbarAppName,
  LayoutComponentsNavbarNotification,
  LayoutComponentsCluster
} = versionVue
</script>

<style lang="scss" scoped>
.navbar {
  display: flex;
  width: 100%;
  height: inherit;

  .navs {
    flex: 1;
    overflow: auto;
    margin-left: 12px;
  }

  .tools {
    margin-left: auto;
    display: flex;
    line-height: 1;

    &:focus {
      outline: none;
    }
  }

  .tool-item {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 40px;
    padding: 0 8px;
    font-size: 18px;

    &.hover {
      cursor: pointer;
      transition: background 0.3s;

      &:hover {
        background: rgba(0, 0, 0, 0.025);
      }
    }
  }
}
</style>
