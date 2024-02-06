import request from '@/utils/request'
import { repository } from '~/package.json'

const repositoryURL = repository?.url
  ?.replace(/\.git$/, '')
  .replace(/^https?:\/\/github\.com\//, '')

const [own, repo] = repositoryURL?.split('/') || []

export const latest = () =>
  request({
    url: `https://api.github.com/repos/${own}/${repo}/releases/latest`,
    thirdParty: true,
    hideErrorMessage: true,
  })
