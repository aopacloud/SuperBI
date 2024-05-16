/*
 * @Author: huanghe Huang.He@olaola.chat
 * @Date: 2024-04-07 12:04:18
 * @LastEditors: huanghe Huang.He@olaola.chat
 * @LastEditTime: 2024-04-07 12:13:48
 * @FilePath: /dm-BDP-front-feat/src/versions/os/js/store.modules.resource.js
 */
import { getPermissionByWorkspaceId } from '@/apis/workspace'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import { listToTree, sortTree } from '@/common/utils/help'

export async function fetchResourceByVersion() {
  try {
    await useUserStore().getInfo()

    return fetchPermissionByWorkspaceId.bind(this)()
  } catch (error) {
    console.error('获取所有资源失败', error)
  }
}

/**
 * 根据空间Id更新权限资源
 * @returns
 */
export async function fetchPermissionByWorkspaceId(
  workspaceId = useAppStore().workspaceId
) {
  const { menus = [], resourceCodes = [] } = await getPermissionByWorkspaceId(
    workspaceId
  )

  // 设置权限code
  useUserStore().setPermissionCodes(resourceCodes)

  this.setResource(menus)
  this.setNavbarResouce(sortTree(listToTree(menus)))

  return Promise.resolve()
}
