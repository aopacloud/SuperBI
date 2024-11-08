<template>
  <section class="express">
    <header class="tools flex">
      <Compact>
        <a-button
          type="text"
          v-for="item in tools"
          :key="item"
          @click="insert(item)">
          {{ item }}
        </a-button>
      </Compact>
    </header>
    <main class="content">
      <textarea
        ref="textarea"
        class="textarea"
        :placeholder="placeholder"
        v-model="modelValue" />
    </main>
  </section>
</template>

<script setup>
import { ref, watch, watchEffect } from 'vue'
import { Compact } from 'ant-design-vue'
import { insertText } from './utils'

const emits = defineEmits(['update:value', 'change'])
const props = defineProps({
  value: {
    type: String,
  },
  tools: {
    type: Array,
    default: () => ['+', '-', '*', '/', '(', ')'],
  },
  placeholder: {
    type: String,
    default: '请输入',
  },
})

const modelValue = ref('')
watchEffect(() => {
  modelValue.value = props.value
})
watch(modelValue, v => {
  emits('update:value', v)
  emits('change', v)
})

const textarea = ref(null)
const insert = (str, pos = []) => {
  const v = insertText(textarea.value, str, pos)
  modelValue.value = v
}
const setPosition = (pos = []) => {
  textarea.value.setSelectionRange(...pos)
}

defineExpose({
  insert,
  setPosition,
})
</script>

<style lang="scss" scoped>
.express {
  display: flex;
  flex-direction: column;
  border: 1px solid #d8d8d8;
  border-radius: 2px;
}
.tools {
  border-bottom: 1px solid #d8d8d8;
}
.content {
  flex: 1;
}
.textarea {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 200px;
  padding: 10px 12px;
  outline: none;
  border: none;
  resize: none;
  font-size: inherit;
}
</style>
