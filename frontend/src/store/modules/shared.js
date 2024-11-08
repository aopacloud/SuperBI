import { shallowRef } from 'vue'
import { defineStore } from 'pinia'
import { getAllUser } from '@/apis/user'

export const shareUsers = defineStore('shareUsers', () => {
  const loaded = ref(false)
  const list = shallowRef([])

  const get = () => {
    if (loaded.value) return list.value

    return getAllUser({ pageSize: 10000 })
      .then(({ data }) => {
        list.value = data
      })
      .catch(e => {
        console.error('获取全局用户失败', e)
      })
      .finally(() => {
        loaded.value = true
      })
  }

  const update = () => {
    loaded.value = false
    get()
  }

  return {
    list,
    get,
    update
  }
})
