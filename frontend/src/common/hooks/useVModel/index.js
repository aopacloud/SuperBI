import { computed } from 'vue'

export function useVModel(props, propName, emit) {
  return computed({
    get() {
      return new Proxy(props[propName], {
        get(target, key) {
          return Reflect.get(target, key)
        },
        set(target, key, value) {
          emit(`update:${propName}`, {
            ...target,
            [key]: value,
          })

          return true
        },
      })
    },
    set(value) {
      emit(`update:${propName}`, value)
    },
  })
}

export default useVModel
