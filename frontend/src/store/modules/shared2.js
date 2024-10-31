import { onMounted, shallowRef } from 'vue'
import { getAllUser } from '@/apis/user'

export default function useShared() {
  const allUsers = shallowRef([])

  const getAllUsers = () => {
    return getAllUser({ pageSize: 10000 })
      .then(({ data }) => {
        allUsers.value = data
      })
      .catch(e => {
        console.error('获取全局用户失败', e)
      })
  }

  onMounted(() => {
    getAllUsers()
  })

  return {
    allUsers,
    getAllUsers
  }
}
