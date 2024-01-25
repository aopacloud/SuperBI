<template>
  <contextHolder />
</template>

<script setup lang="jsx">
import { ref, onMounted, onUnmounted } from 'vue'
import { notification } from 'ant-design-vue'
import { latest } from '@/apis/releases'
import { version } from '~/package.json'
import { storagePrefix } from '@/settings'

// 版本提示已读
const VERSION_NOTIFY_READED = 'version-notify-readed'

const [notificationApi, contextHolder] = notification.useNotification()

// 版本信息
const releaseRes = ref()

/**
 * 获取最新版本
 */
const getLatest = async () => {
  try {
    const { tag_name, ...res } = await latest()

    if (isLatestVersion(tag_name)) {
      setReaded(false)

      return
    }

    const storageReaded = localStorage.getItem(storagePrefix + VERSION_NOTIFY_READED)
    if (+storageReaded) return

    releaseRes.value = res
    openNotification()
  } catch (error) {
    console.error('获取最新版本失败', error)
  }
}

/**
 * 版本字符串转换
 * @param {string} str
 * @returns {string}
 * @example _transferText(v1.1.0) => 1.1.0
 */
const _transferText = str => str.replace(/v|V/, '')

/**
 * 是否是最新版本
 * @param {string} latestVersion 最新版本
 * @returns {boolean}
 */
const isLatestVersion = latestVersion => {
  return _transferText(version) === _transferText(latestVersion)
}

const getInfoContent = () => {
  const { name, body, html_url } = releaseRes.value

  return {
    title: (
      <div style='display: flex;align-items: center'>
        <b>
          新版本提示
          <a style='margin: 0 4px;' target='_blank' href={html_url}>
            {name}
          </a>
        </b>
      </div>
    ),
    description: (
      <div style='max-height: 400px;overflow: auto;'>
        <pre style='line-height: 1.2;margin: 0;'>{body}</pre>
      </div>
    ),
  }
}

// 提示
const openNotification = () => {
  const { title, description } = getInfoContent()
  notificationApi.info({
    style: {
      width: 'auto',
      maxWidth: '520px',
    },
    message: title,
    description,
    placement: 'bottomRight',
    duration: null,
    onClose: () => {
      setReaded()
    },
  })
}

/**
 * 设置新版本提示已读
 * @param {boolean} read 已读
 */
const setReaded = (read = true) => {
  if (read) {
    localStorage.setItem(storagePrefix + VERSION_NOTIFY_READED, 1)
  } else {
    localStorage.removeItem(storagePrefix + VERSION_NOTIFY_READED)
  }
}

const onVisibilityChanged = () => {
  if (document.visibilityState === 'visible') {
    const storageReaded = localStorage.getItem(storagePrefix + VERSION_NOTIFY_READED)

    if (+storageReaded) notificationApi.destroy()
  }
}

onMounted(() => {
  getLatest()

  window.addEventListener('visibilitychange', onVisibilityChanged)
})

onUnmounted(() => {
  window.removeEventListener('visibilitychange', onVisibilityChanged)
})
</script>
