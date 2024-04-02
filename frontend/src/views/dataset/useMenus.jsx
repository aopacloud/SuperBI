import { Modal, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { downloadWithBlob } from 'common/utils/file'
import dayjs from 'dayjs'

import {
  exportById,
  postPublishById,
  postOfflineById,
  deleteById,
} from '@/apis/dataset'
import { postFavorite, postUnFavorite } from '@/apis/favorite'

export default function useMenus() {
  const router = useRouter()

  const getAnalysisHref = row => {
    const routeRes = router.resolve({
      name: 'DatasetAnalysis',
      params: { id: row.id },
    })
    if (!routeRes) return '/'

    return routeRes.href
  }

  const openWindowWithParams = row => {
    const folderId = row.folder?.id
    const routeRes = router.resolve({
      name: 'DatasetList',
      query: { type: 'ALL', folderId },
    })
    if (!routeRes) return

    window.open(routeRes.href, '_blank')
  }

  const toAnalysis = (row, _blank) => {
    if (!_blank) {
      router.push({ name: 'DatasetAnalysis', params: { id: row.id } })
    } else {
      const routeRes = router.resolve({
        name: 'DatasetAnalysis',
        params: { id: row.id },
      })
      if (!routeRes) return

      window.open(routeRes.href, _blank ? '_blank' : '_self')
    }
  }

  /**
   * 编辑
   * @param {Record} row 行数据
   * @returns
   */
  const edit = row => {
    const routeRes = router.resolve({
      name: 'DatasetModify',
      params: { id: row.id },
    })
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

      await fn({ position: 'DATASET', targetId: row.id })

      row.favorite = !isFavorite
      message.success(`${isFavorite ? '取消收藏' : '收藏'}成功`)
    } catch (error) {
      console.error('收藏取消收藏错误', error)
    } finally {
      row.loading = false
    }
  }

  // 导出
  const handleExport = async row => {
    try {
      const res = await exportById(row.id)

      await downloadWithBlob(
        res,
        row.name + '-' + dayjs().format('YYYY-MM-DD HH-mm-ss') + '.xlsx'
      )
    } catch (error) {
      console.error('数据集导出错误', error)
    }
  }

  // 发布
  const publish = async row => {
    try {
      await postPublishById(row.id)

      row.status = 'ONLINE'
      message.success('发布成功')
    } catch (error) {
      console.error('数据集发布错误', error)
    }
  }

  // 下线
  const offline = async row => {
    try {
      await postOfflineById(row.id)

      message.success('下线成功')
    } catch (error) {
      console.error('数据集下线错误', error)
    }
  }

  // 删除
  const del = (row, cb) => {
    Modal.confirm({
      content: h(
        <p>
          确认删除数据集<b>【{row.name}】</b>吗？
        </p>
      ),
      onOk() {
        return deleteById(row.id).then(() => {
          message.success('删除成功')

          cb && cb()
        })
      },
    })
  }

  return {
    getAnalysisHref,
    openWindowWithParams,
    toAnalysis,
    edit,
    favor,
    handleExport,
    publish,
    offline,
    del,
  }
}
