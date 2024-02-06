import useAppStore from '@/store/modules/app'

const WORKSPACEID = 'workspaceId'

const hasKey = (obj, key) => key in obj && obj[key] !== undefined && obj[key] !== null

export default function () {
  this.interceptors.request.use(config => {
    const { params = {}, data = {} } = config

    if (!hasKey(params, WORKSPACEID) && !hasKey(data, WORKSPACEID)) {
      params[WORKSPACEID] = useAppStore().workspaceId

      config.params = params
    }

    return config
  })
}
