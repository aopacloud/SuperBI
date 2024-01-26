<template>
  <a-sub-menu :key="menu.id" @titleClick="onSubMenuTitleClick">
    <template #title>
      <span>{{ menu.name }}</span>
    </template>
    <template v-for="child in menu.children">
      <a-menu-item v-if="!child.children || !child.children.length" :key="child.id">
        <!-- <a-icon v-if="child.icon" :type="child.icon" /> -->
        <span>{{ child.name }}</span>
      </a-menu-item>
      <SubMenu v-else :menu="child" />
    </template>
  </a-sub-menu>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { appPath } from '@/settings'

const router = useRouter()

const props = defineProps({
  menu: {
    type: Object,
    default: () => ({}),
  },
})

const onSubMenuTitleClick = () => {
  const { appPath: menuPath, url, children = [] } = props.menu
  if (menuPath === '/' + appPath) {
    const first = children[0]

    router.push(first ? first.url : url)
  } else {
    window.open(menuPath + '#' + url, '_blank')
  }
}
</script>
