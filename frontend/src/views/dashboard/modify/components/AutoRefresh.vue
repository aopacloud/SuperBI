<template>
  <a-tooltip
    v-if="!writeable"
    :title="tooltipTitle"
    :overlayStyle="{ maxWidth: 'initial' }">
    <a-button
      size="small"
      style="width: 28px; height: 26px"
      :type="frequency ? 'primary' : ''"
      :icon="h(SyncOutlined)"
      :loading="refreshLoading"
      @click="autoHandler" />
  </a-tooltip>

  <a-dropdown-button
    v-else
    class="auto-fresh-dropdown"
    size="small"
    trigger="click"
    :type="frequency ? 'primary' : 'default'"
    @click="autoHandler">
    <a-tooltip
      placement="bottom"
      :title="tooltipTitle"
      :overlayStyle="{ maxWidth: 'initial' }">
      <SyncOutlined :spin="refreshLoading" />
    </a-tooltip>

    <template #overlay>
      <a-menu
        selectable
        :selectedKeys="[refreshInterval]"
        @click="onAutoOptionChange">
        <a-menu-item :key="0">关闭</a-menu-item>
        <a-menu-item v-for="item in autoRefreshOptions" :key="item.value">
          {{ item.label }}
        </a-menu-item>
      </a-menu>
    </template>
    <template #icon>
      <DownOutlined style="font-size: 12px" />
    </template>
  </a-dropdown-button>
</template>

<script setup>
import { h, ref, computed, inject, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { SyncOutlined, DownOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { autoRefreshOptions } from '../config'
import { postRefreshIntervalById } from '@/apis/dashboard'

const props = defineProps({
  detail: { type: Object, default: () => ({}) },
  writeable: { type: Boolean },
})

const { allDone, refresh, detail } = inject('index')
const { loading: refreshLoading, run: refreshRun } = refresh

const refreshInterval = computed(() => detail.get('refreshIntervalSeconds'))

// 刷新频率, 0 不自动刷新
const frequency = ref(0)

watchEffect(() => {
  frequency.value = refreshInterval.value
})

const tooltipTitle = computed(() => {
  if (!frequency.value) return '刷新'
  const item = autoRefreshOptions.find(e => e.value === frequency.value)
  if (!item) return '自动刷新（可点击手动刷新）'

  return `自动刷新: 间隔${item.title}(可点击手动刷新)`
})

const onAutoOptionChange = ({ key }) => {
  updateAutoRefresh(key)
}

const submitLoading = ref(false)
// 更新看板的自动刷新配置
const updateAutoRefresh = async value => {
  try {
    submitLoading.value = true

    await postRefreshIntervalById(props.detail.id, { refreshIntervalSeconds: value })

    detail.set('refreshIntervalSeconds', value)
    message.success('自动刷新配置成功')

    reset()
  } catch (error) {
    console.error('更新看板自动刷新配置失败', error)
  } finally {
    submitLoading.value = false
  }
}

const autoHandler = async () => {
  refreshRun()
}

let timer = null
const s2ms = s => s * 1000
watch(allDone, e => {
  // 全部完成且页面可见
  if (e && document.visibilityState !== 'hidden') {
    if (refreshLoading) {
      reset()
    } else {
      start()
    }
  }
})

const start = () => {
  if (!refreshInterval.value) return

  timer = setTimeout(refreshRun, s2ms(refreshInterval.value))
}

const stop = () => {
  clearTimeout(timer)
  timer = null
}

const reset = () => {
  stop()
  start()
}

const handler = () => {
  if (document.visibilityState === 'hidden') {
    // 页面不可见时
    stop()
  } else {
    if (!refreshInterval.value) return

    refreshRun()
    start()
  }
}

onMounted(() => {
  window.addEventListener('visibilitychange', handler)
})

onBeforeUnmount(() => {
  stop()
  window.removeEventListener('visibilitychange', handler)
})
</script>

<style lang="scss" scoped>
.auto-fresh-dropdown {
  :deep(.ant-btn) {
    height: 28px;
  }
  :deep(.ant-dropdown-trigger) {
    width: 20px;
  }
}
</style>
