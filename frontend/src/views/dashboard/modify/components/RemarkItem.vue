<template>
  <section class="remark-layout draggable-handler">
    <header class="title">
      <h4 style="margin: 0" class="title-text">{{ initialValue.title }}</h4>

      <a-button
        v-if="mode === 'EDIT'"
        class="edit-btn"
        size="small"
        type="text"
        :icon="h(EditOutlined)"
        @click="onEdit"
      ></a-button>
    </header>
    <main class="content" v-if="initialValue.content">
      <RichInput
        style="background-color: transparent; line-height: 21px"
        readonly
        :contentStyle="{ minHeight: 'initial', padding: 0 }"
        v-model:value="initialValue.content"
        @tag-click="onTagClick"
      />
    </main>
  </section>
</template>
<script setup>
import { h, watch, getCurrentInstance } from 'vue'
import RichInput from 'common/components/RichInput'
import { EditOutlined } from '@ant-design/icons-vue'

const { ctx } = getCurrentInstance()
const emits = defineEmits(['edit', 'change', 'update'])
const props = defineProps({
  id: {
    type: [String, Number]
  },
  mode: {
    type: String,
    default: 'EDIT'
  },
  initialValue: {
    type: Object,
    default: () => ({})
  }
})

watch(
  () => props.initialValue,
  () => {
    emits('update', { target: ctx.$el })
  }
)

const onEdit = () => {
  emits('edit', { _id: props.id, content: props.initialValue })
}

// 标签节点点击
const onTagClick = e => {
  const dataHref = e.getAttribute('data-href')

  if (!!dataHref) {
    window.open(dataHref, '_blank')
  }
}
</script>

<style lang="scss" scoped>
.remark-layout {
  padding: 16px 12px;
  background-color: #fefcef;
  &:hover {
    .edit-btn {
      opacity: 1;
    }
  }
}
.title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 34px;

  &-text {
    font-size: 16px;
  }
}
.content {
  margin: 10px 0 3px 0;
  font-size: 14px;
}

.edit-btn {
  opacity: 0;
  transition: all 0.2s;
}
</style>
