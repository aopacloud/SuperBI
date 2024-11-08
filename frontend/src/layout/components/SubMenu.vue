<template>
  <a-sub-menu :key="menu.id" @titleClick="onSubMenuTitleClick">
    <template #title>
      <span>{{ menu.name }}</span>
    </template>
    <template v-for="child in menu.children">
      <a-menu-item
        v-if="!child.children || !child.children.length"
        :key="child.id"
      >
        <router-link :to="child.url">
          <span>{{ child.name }}</span>
        </router-link>
      </a-menu-item>
      <SubMenu v-else :menu="child" />
    </template>
  </a-sub-menu>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { appPath } from '@/settings'
import { isHttp } from 'common/utils/validate'

const router = useRouter()

const props = defineProps({
  menu: {
    type: Object,
    default: () => ({})
  }
})

const onSubMenuTitleClick = () => {
  const { appPath: menuPath, url, children = [], type, redirect } = props.menu

  if (type === 4 || isHttp(url)) {
    window.open(url, '_blank')
    return
  }

  if (menuPath === '/' + appPath) {
    if (type === 2) {
      const first = children[0]
      router.push(first?.url ?? url)
    } else {
      if (children.length && redirect) {
        router.push(redirect)
      }
    }
  } else {
    window.open(menuPath + '#' + url, '_blank')
  }
}
</script>
