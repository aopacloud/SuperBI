import { ref } from 'vue'
import { deepClone } from '@/common/utils/help'

export default function useRelationList({ getDefaultItem = () => {} } = {}) {
  const list = ref([])

  const add = index => {
    if (typeof index === 'undefined') {
      list.value.push(getDefaultItem())
    } else {
      list.value.splice(index + 1, 0, getDefaultItem())
    }
  }

  const copy = (index, item) => {
    list.value.splice(index + 1, 0, getDefaultItem(deepClone(item)))
  }

  const remove = index => {
    list.value.splice(index, 1)
  }

  const split = index => {
    const item = list.value[index]
    list.value.splice(index, 1, {
      children: [item, getDefaultItem()],
    })
  }

  return { list, add, copy, split, remove }
}
