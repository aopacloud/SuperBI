<template>
  <a-breadcrumb class="breadcrumb" separator="/">
    <a-breadcrumb-item v-for="(item, index) in list" :key="item.path">
      <span
        v-if="item.redirect === 'noRedirect' || index == list.length - 1"
        class="no-redirect"
        >{{ item.meta.title }}
      </span>
      <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
    </a-breadcrumb-item>
  </a-breadcrumb>
</template>

<script setup>
import { watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const list = ref([])

function getBreadcrumb() {
  // Only show routes with meta.title and meta.breadcrumb !== false
  let matched = route.matched.filter(
    item => item.meta?.title && item.meta?.breadcrumb !== false
  )
  // Judge is Index page
  if (!isDashboard(matched[0])) {
    matched = [{ path: '/', meta: { title: '首页' } }].concat(matched)
  }

  list.value = matched
}
function isDashboard(route) {
  const name = route && route.name
  if (!name) return false

  return name.trim() === 'Index'
}
function handleLink(item) {
  const { redirect, path } = item

  if (redirect) {
    router.push(redirect)
  } else {
    router.push(path)
  }
}

watchEffect(() => {
  // if you go to the redirect page, do not update the breadcrumbs
  if (route.path.startsWith('/redirect/')) {
    return
  }
  getBreadcrumb()
})
getBreadcrumb()
</script>
