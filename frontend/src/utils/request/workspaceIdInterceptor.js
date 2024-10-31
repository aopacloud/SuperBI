import useAppStore from '@/store/modules/app'

const WORKSPACE_ID = 'workspaceId'

const hasKey = (obj, key) => key in obj && !!obj[key]

export default function () {
  this.interceptors.request.use(config => {
    const { params = {}, data = {}, forceWorkspaceId } = config

    if (!hasKey(params, WORKSPACE_ID) && !hasKey(data, WORKSPACE_ID)) {
      params[WORKSPACE_ID] = useAppStore().workspaceId
    } else {
      if (forceWorkspaceId) {
        params[WORKSPACE_ID] =
          params[WORKSPACE_ID] ??
          data[WORKSPACE_ID] ??
          useAppStore().workspaceId
      }
    }

    config.params = params

    return config
  })
}
