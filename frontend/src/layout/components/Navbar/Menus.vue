<template>
  <a-menu
    mode="horizontal"
    theme="dark"
    :style="{ backgroundColor: 'transparent' }"
    :selectedKeys="selectedKeys"
    @click="onMenuClick">
    <template v-for="item in menus">
      <a-menu-item v-if="!item.children || !item.children.length" :key="item.id">
        <span>{{ item.name }}</span>
      </a-menu-item>

      <SubMenu v-else :menu="item" />
    </template>

    <a-sub-menu
      v-if="authorizeMenu"
      :key="authorizeMenu.id"
      @titleClick="onAuthorityMenuTitleClick">
      <template #title>
        <span>{{ authorizeMenu.name }}</span>

        <a-badge class="menu-item-badge" :count="countTotal" />
      </template>
      <a-menu-item v-for="child in authorizeMenu.children" :key="child.id">
        <router-link :to="child.url">
          <a-badge
            style="color: inherit"
            :offset="[20, 7]"
            :numberStyle="{
              minWidth: '16px',
              height: '16px',
              lineHeight: '16px',
              padding: '0 4px',
            }"
            :count="getCountByItem(child.routeName)">
            {{ child.name }}
          </a-badge>
        </router-link>
      </a-menu-item>
    </a-sub-menu>
  </a-menu>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import SubMenu from '../SubMenu.vue'
import useResourceStore from '@/store/modules/resource'
import { deepFind, flat } from 'common/utils/help'
import { appPath } from '@/settings'
import { getAuthorizeMesage } from '@/apis/dataset/apply'

const resourceStore = useResourceStore()
const router = useRouter()
const route = useRoute()

// 所有资源
const navbarResources = computed(() => resourceStore.navbarResources)

// 当前应用
const appInfo = computed(
  () => deepFind(navbarResources.value, t => t.appPath === '/' + appPath) || {}
)

// 父级应用
const parentAppInfo = computed(() => {
  return navbarResources.value.find(t => t.id === appInfo.value.parentId)
})

// 授权中心菜单url
const AUTHORITY_PATH = '/authority'

// 授权中心菜单
const authorizeMenu = computed(() =>
  navbarResources.value.find(t => t.url === AUTHORITY_PATH)
)

// 顶部菜单(一级)
const menus = computed(() => {
  if (!parentAppInfo.value) {
    return navbarResources.value.filter(t => !t.hidden && t.url !== AUTHORITY_PATH)
  } else {
    return parentAppInfo.value.children
      .filter(t => !t.hidden || t.url !== AUTHORITY_PATH)
      .map(item => {
        return {
          ...item,
          children: item.children?.filter(t => !t.hidden),
        }
      })
  }
})

const getSelectKeys = (list = []) => {
  const path = route.path.split('?')[0]
  const item = flat(list).find(t => t.url === path)
  if (!item) return []

  return [item.id]
}

// 选中项
const selectedKeys = computed(() => {
  if (!parentAppInfo.value) {
    return getSelectKeys(navbarResources.value)
  } else {
    const parentItem = navbarResources.value.find(t => t.id === parentAppInfo.value.id)
    if (!parentItem) return []

    const appItem = parentItem.children.find(t => t.id === appInfo.value.id)
    if (!appItem) return []

    const list = appItem.children || []

    return getSelectKeys(list)
  }
})

// 点击事件
const onMenuClick = ({ key }) => {
  const item = deepFind(navbarResources.value, t => t.id === key)
  if (!item) return

  const { appPath: _appPath, url, type } = item

  // 外部链接
  if (url.startsWith('http') || type === 4) {
    window.open(url, '_blank')

    return
  }

  // 应用内
  if (_appPath === '/' + appPath) {
    router.push(url)
  } else {
    window.open(_appPath + '#' + url, '_blank')
  }
}

const onAuthorityMenuTitleClick = () => {
  if (!authorizeMenu.value.children?.length) return

  const first = authorizeMenu.value.children[0]

  router.push(first.url)
}

// 轮询间隔
const REQUEST_INTERVAL = 1000 * 60

// 授权消息中心
const countMsg = ref({})
const countMap = {
  AuthorityApply: 'applyingCount',
  AuthorityApprove: 'reviewCount',
  AuthorityManage: 'operationCount',
}
const getCountByItem = name => {
  return countMsg.value[countMap[name]] || 0
}
const countTotal = ref(0)
const timer = ref()
const fetchMessageCount = async () => {
  try {
    const { total, ...res } = await getAuthorizeMesage()

    countTotal.value = total
    countMsg.value = res

    if (!timer.value) {
      timer.value = setTimeout(() => {
        clearTimer()

        fetchMessageCount()
      }, REQUEST_INTERVAL)
    }
  } catch (error) {
    console.error('授权消息获取失败', error)
  }
}

const clearTimer = () => {
  clearTimeout(timer.value)
  timer.value = null
}

const createTimer = () => {
  if (!authorizeMenu.value) return

  fetchMessageCount()
}

const onVisibilityChanged = () => {
  if (document.visibilityState === 'hidden') {
    clearTimer()
  } else {
    createTimer()
  }
}

onMounted(() => {
  createTimer()

  window.addEventListener('visibilitychange', onVisibilityChanged)
})

onUnmounted(() => {
  clearTimer()

  window.removeEventListener('visibilitychange', onVisibilityChanged)
})
</script>
