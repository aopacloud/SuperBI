import { deepClone, getRandomKey } from './help'

export function flat(list = [], key = 'children') {
  if (!Array.isArray(list)) {
    throw Error(`flat require 'array' type params, please check it`)
  }

  return deepClone(list).reduce((acc, pre) => {
    const children = pre[key]
    delete pre[key]

    return acc.concat(pre, Array.isArray(children) ? flat(children, key) : [])
  }, [])
}

export const flatWithOption = (
  list = [],
  parentId,
  { childrenKey = 'children', parentKey = 'parentId', idKey = 'id' }
) => {
  if (!Array.isArray(list)) {
    throw TypeError(`flat require 'array' type params, please check it`)
  }

  return deepClone(list).reduce((acc, item) => {
    const children = item[childrenKey]
    delete item[childrenKey]

    if (!Array.isArray(children)) {
      item[parentKey] = parentId
      item[idKey] = getRandomKey(6)

      return acc.concat(item)
    } else {
      item[parentKey] = parentId
      item[idKey] = getRandomKey(6)

      return acc.concat(
        item,
        flatWithOption(children, item[idKey], { childrenKey, parentKey, idKey })
      )
    }
  }, [])
}
