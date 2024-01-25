<template>
  <div tabindex="-1" class="tags-input ant-input" :class="size" @click="onFocus">
    <div :class="['tags', size]">
      <div class="tag" v-for="(tag, index) in tags" :key="index">
        <span class="tag-span">{{ tag }}</span>

        <CloseOutlined
          v-if="!readonly && !disabled"
          class="tag-remove"
          @click="onTagDel(index)" />
      </div>
      <a-input
        class="input"
        ref="inputRef"
        type="text"
        :size="size"
        :placeholder="readonly ? '' : placeholder"
        :readonly="readonly"
        :disabled="disabled"
        v-model:value="inputVal"
        @keydown="onKeydown"
        @focus="emits('focus')"
        @blur="emits('blur')" />

      <CloseCircleOutlined v-if="tags.length" class="close-icon" @click="reset" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, watchEffect } from 'vue'
import { CloseOutlined, CloseCircleOutlined } from '@ant-design/icons-vue'

const emits = defineEmits(['focus', 'blur', 'change', 'update:value'])
const props = defineProps({
  size: {
    type: String,
    default: 'default',
    validator: s => {
      return ['large', 'default', 'small'].includes(s)
    },
  },
  value: {
    type: [Array, String],
  },
  placeholder: {
    type: String,
    default: '回车分隔',
  },
  // 分隔符
  separate: {
    type: String,
    default: ',',
  },
  // 校验方法
  validator: {
    type: Function,
  },
  readonly: {
    type: Boolean,
  },
  disabled: {
    type: Boolean,
  },
  // 触发分割的 keycode
  triggerKeyCode: {
    type: Number,
    default: 13,
  },
})

const inputRef = ref(null)
const tags = ref([])
const inputVal = ref()

const reset = () => {
  tags.value = []
}

const keycodes = computed(() => {
  return typeof props.triggerKeyCode === 'number'
    ? [props.triggerKeyCode]
    : props.triggerKeyCode
})

watchEffect(() => {
  if (typeof props.value === 'undefined') {
    tags.value = []
  } else {
    tags.value = Array.isArray(props.value)
      ? props.value
      : props.value.split(props.separate).filter(Boolean)
  }
  inputVal.value = ''
})

watch(
  tags,
  list => {
    emits('update:value', list)
    emits('change', list)
  },
  { immediate: true, deep: true }
)

const onFocus = () => {
  inputRef.value.focus()
}

const onTagDel = i => {
  tags.value.splice(i, 1)
}

const add = e => {
  if (props.disabled) return

  const value = e.target.value

  if (!value.trim().length) {
    inputVal.value = ''

    return
  }

  // 若有校验方法则通过检验后添加
  if (
    typeof props.validator === 'undefined' ||
    (typeof props.validator === 'Function' && props.validator(value))
  ) {
    tags.value.push(value)
    inputVal.value = ''
  }
}

const onKeyDelete = e => {
  if (props.disabled) return

  const value = e.target.value || ''

  if (!value.length) {
    tags.value.pop()
    e.preventDefault()
  }
}

/**
 * [handleKeydown 输入时事件]
 * @param  {MouseEvent} e [description]
 */
const onKeydown = e => {
  const { key, keyCode } = e

  if (keyCode === 8) {
    // 删除
    onKeyDelete(e)
  } else {
    // 配置的触发keyCode、输入的键key与分隔符相等
    if (keycodes.value.includes(keyCode) || key === props.separate) {
      add(e)

      e.preventDefault()
    }
  }
}
</script>

<style scoped lang="scss">
.tags-input {
  position: relative;
  padding: 0 6px 3px;
  background-color: #ffffff;
  background-image: none;
  border-width: 1px;
  border-style: solid;
  border-color: #d9d9d9;
  border-radius: 6px;
  transition: all 0.2s;

  &:hover {
    border-color: #4096ff;
    .close-icon {
      display: block;
    }
  }
  &:focus {
    box-shadow: 0 0 0 2px rgba(5, 145, 255, 0.1);
  }

  .close-icon {
    position: absolute;
    right: 4px;
    bottom: 6px;
    padding: 2px;
    background-color: #fff;
    display: none;
    cursor: pointer;
  }

  &.large {
    .tag,
    .input {
      height: 30px;
      line-height: 30px;
      font-size: 16px;
    }
    .close-icon {
      bottom: 8px;
    }
  }
  &.small {
    .tag,
    .input {
      height: 16px;
      line-height: 16px;
      font-size: 13px;
    }
    .tag {
      padding: 0 4px;
    }

    .tag-remove {
      margin-left: 6px;
      font-size: 10px;
      color: rgba(0, 0, 0, 0.25);
    }
    .close-icon {
      bottom: 2px;
    }
  }
}

.tags {
  display: flex;
  flex-wrap: wrap;
  cursor: default;
}
.tag {
  display: inline-flex;
  justify-content: space-between;
  align-items: center;
  height: 22px;
  line-height: 22px;
  margin-top: 4px;
  padding: 0 4px 0 8px;
  background-color: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 2px;
  margin-right: 4px;

  &-span {
    margin-right: 4px;
  }
}
.input {
  flex: 1;
  align-self: flex-end;
  min-width: 10px;
  height: 23px;
  margin-top: 4px;
  padding: 4px 16px 4px 10px;
  border: none;
  box-shadow: none;
  outline: none;
  box-shadow: none;
  &:focus {
    box-shadow: none;
  }
  &:input-placeholder {
    color: red;
  }
}
.tag-remove {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
</style>
