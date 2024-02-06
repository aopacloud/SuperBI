<template>
  <div>
    <a-tooltip title="消息中心">
      <router-link to="/system/notification" style="color: inherit">
        <a-badge
          style="color: inherit"
          :offset="[2, -2]"
          :numberStyle="{
            minWidth: '18px',
            height: '18px',
            lineHeight: '18px',
          }"
          :count="value">
          <BellOutlined style="font-size: 20px" />
        </a-badge>
      </router-link>
    </a-tooltip>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { BellOutlined } from '@ant-design/icons-vue'
import { getCount } from '@/apis/system/notification'

// 轮询间隔
const REQUEST_INTERVAL = 1000 * 30

const value = ref(0)
const timer = ref()
const fetchData = async () => {
  try {
    const total = await getCount()

    value.value = total

    if (!timer.value) {
      timer.value = setTimeout(() => {
        clearTimer()

        fetchData()
      }, REQUEST_INTERVAL)
    }
  } catch (error) {
    console.error('消息中心获取失败', error)
  }
}

const clearTimer = () => {
  clearTimeout(timer.value)
  timer.value = null
}

const createTimer = () => {
  fetchData()
}

const onVisibilityChanged = () => {
  if (document.visibilityState === 'hidden') {
    clearTimer()
  } else {
    createTimer()
  }
}
onMounted(() => {
  fetchData()

  window.addEventListener('visibilitychange', onVisibilityChanged)
})

onUnmounted(() => {
  clearTimer()

  window.removeEventListener('visibilitychange', onVisibilityChanged)
})
</script>
