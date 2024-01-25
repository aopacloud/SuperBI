import { Modal, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

import {
  postPublishById,
  postOnlineById,
  postOfflineById,
  deleteById,
} from '@/apis/dashboard'
import { postFavorite, postUnFavorite } from '@/apis/favorite'

export default function useMenus() {
  const router = useRouter()

  const getPreviewlHref = row => {
    const routeRes = router.resolve({ name: 'DashboardPreview', params: { id: row.id } })
    if (!routeRes) return

    return routeRes.href
  }

  const openWindow = (row, _blank) => {
    const routeRes = router.resolve({ name: 'DashboardPreview', params: { id: row.id } })
    if (!routeRes) return

    window.open(routeRes.href, _blank ? '_blank' : '_self')
  }

  const openWindowWithParams = row => {
    const folderId = row.folder?.id
    const routeRes = router.resolve({
      name: 'DashboardList',
      query: { type: 'ALL', folderId },
    })
    if (!routeRes) return

    window.open(routeRes.href, '_blank')
  }

  const edit = row => {
    const routeRes = router.resolve({ name: 'DashboardModify', params: { id: row.id } })
    if (!routeRes) return

    window.open(routeRes.href, '_blank')
  }

  /**
   * 收藏\取消收藏
   * @param {Record} row 行数据
   */
  const favor = async row => {
    if (row.loading) return

    try {
      row.loading = true

      const isFavorite = row.favorite
      const fn = isFavorite ? postUnFavorite : postFavorite
      await fn({ position: 'DASHBOARD', targetId: row.id })

      row.favorite = !isFavorite
      message.success(`${isFavorite ? '取消收藏' : '收藏'}成功`)
    } catch (error) {
      console.error('收藏取消收藏错误', error)
    } finally {
      row.loading = false
    }
  }

  // 发布
  const publish = async row => {
    if (row.loading) return

    try {
      row.loading = true
      await postPublishById(row.id)

      record.status = 'ONLINE'
      message.success('发布成功')
    } catch (error) {
      console.error('看板发布错误', error)
    } finally {
      row.loading = false
    }
  }

  // 下线
  const offline = async row => {
    try {
      await postOfflineById(row.id)

      message.success('下线成功')
    } catch (error) {
      console.error('看板下线错误', error)
    }
  }

  // 上线
  const online = async row => {
    try {
      await postOnlineById(row.id)

      row.status = 'ONLINE'
      message.success('发布成功')
    } catch (error) {
      console.error('看板发布错误', error)
    }
  }

  // 删除
  const del = (row, cb) => {
    Modal.confirm({
      title: '提示',
      content: '确定删除看板【 ' + row.name + ' 】?',
      okType: 'danger',
      onOk() {
        return deleteById(row.id).then(() => {
          message.success('删除成功')

          cb && cb()
        })
      },
    })
  }

  return {
    getPreviewlHref,
    openWindow,
    openWindowWithParams,
    edit,
    favor,
    publish,
    offline,
    online,
    del,
  }
}
