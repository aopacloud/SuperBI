/**
 * v-permission 操作权限处理
 * arg: a
 * modifiers: { b }
 * value: c
 */
import useUserStore from '@/store/modules/user'

// 删除节点
const removeEl = el => {
  // 在绑定元素上存储父级元素
  el._parentNode = el.parentNode
  // 在绑定元素上存储一个注释节点
  el._placeholderNode = document.createComment('v-permission')
  // 使用注释节点来占位
  el.parentNode?.replaceChild(el._placeholderNode, el)
}

// 添加节点
const addEl = el => {
  // 替换掉给自己占位的注释节点
  el._parentNode?.replaceChild(el, el._placeholderNode)
}

// 判断是否有权限
const hasPermission = value => {
  if (useUserStore().isSuperAdmin) return true

  if (typeof value === 'boolean') {
    return value
  }

  if (typeof value === 'function') {
    return !!value()
  }

  return useUserStore().hasPermission(value)
}

export default {
  mounted(el, binding) {
    // 无权限则移除当前元素
    if (!hasPermission(binding.value)) {
      removeEl(el)
    }
  },
  // 权限变化重新判断元素渲染
  updated(el, binding) {
    const valueNotChange = binding.value === binding.oldValue
    const oldHasPermission = hasPermission(binding.oldValue)
    const newHasPermission = hasPermission(binding.value)
    const permissionNotChange = oldHasPermission === newHasPermission

    if (valueNotChange && permissionNotChange) return

    if (newHasPermission) {
      addEl(el)
    } else {
      removeEl(el)
    }
  },
}
