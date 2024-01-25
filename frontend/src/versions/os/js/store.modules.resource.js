import { getPermissionByWorkspaceId } from '@/apis/workspace'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import { listToTree, sortTree } from '@/common/utils/help'

export async function fetchResourceByVersion() {
  try {
    await useUserStore().getInfo()

    const { menus = [], resourceCodes = [] } = await getPermissionByWorkspaceId(
      useAppStore().workspaceId
    )

    // 设置权限code
    useUserStore().setPermissionCodes(resourceCodes)

    this.setResource(menus)
    this.setNavbarResouce(sortTree(listToTree(menus)))

    return Promise.resolve()
  } catch (error) {
    console.error('获取所有资源失败', error)
  }
}
