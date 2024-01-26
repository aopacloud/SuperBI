<template>
  <a-popover v-bind="props" trigger="click" v-model:open="visible">
    <slot>录入枚举值</slot>
    <template #content>
      <ul class="enum-list">
        <li class="enum-item header" style="font-weight: bold">
          <div class="enum-item--col">
            <span>属性值名称</span>
          </div>
          <div class="enum-item--col">
            <span>属性值描述</span>
          </div>
        </li>

        <li class="enum-item" v-for="(item, index) in list">
          <div class="enum-item--col input-cell">
            <input
              class="input"
              placeholder="请输入属性值名称"
              v-model="item.name"
              @input="throttleBlur"
              @blur="e => validatorName(item.name, index, e)" />
          </div>
          <div class="enum-item--col input-cell">
            <input class="input" placeholder="请输入属性值描述" v-model="item.desc" />
          </div>
          <div class="enum-item--actions">
            <a-space compact>
              <MinusOutlined
                v-if="list.length !== 1"
                class="action-icon"
                @click="handleDel(index)" />
              <PlusOutlined class="action-icon" @click="handleAdd(index)" />
            </a-space>
          </div>
        </li>
      </ul>

      <div style="margin-top: 10px">
        <a-space>
          <a-button size="small" @click="handleCancel">取消</a-button>
          <a-button size="small" type="primary" @click="handleOk"> 确定 </a-button>
        </a-space>
      </div>
    </template>
  </a-popover>
</template>

<script>
import { throttle } from 'common/utils/help'
import { PlusOutlined, MinusOutlined } from '@ant-design/icons-vue'

const defaultItem = {
  name: '',
  desc: '',
}

export default {
  name: 'enum-resouces',
  components: { PlusOutlined, MinusOutlined },
  props: {
    dataSource: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      visible: false,
      list: [],
    }
  },
  computed: {
    props() {
      return { ...this.$props, ...this.$attrs }
    },
  },
  watch: {
    visible(bool) {
      if (bool) {
        this.init()
      }
    },
  },
  mounted() {
    this.throttleBlur = throttle(this.onNameBlur)
  },
  methods: {
    init() {
      const list = this.dataSource

      this.list = list.length
        ? list.map(t => {
            return {
              name: t.value,
              desc: t.label,
            }
          })
        : [{ ...defaultItem }]
    },
    handleCancel() {
      this.visible = false
    },
    handleOk() {
      if (!this.validator()) return

      const listStr = JSON.stringify(this.list)

      this.$emit('ok', listStr)
      this.handleCancel()
    },
    handleAdd(index) {
      this.list.splice(index + 1, 0, { ...defaultItem })
    },
    handleDel(index) {
      this.list.splice(index, 1)
    },
    onNameBlur(evt) {
      const {
        target,
        target: { value },
      } = evt

      if (value.trim().length) {
        target.classList.remove('has-error')
      } else {
        target.classList.add('has-error')
      }
    },
    validatorName(value, index, evt) {
      const str = value.trim()

      if (!str.length) return

      if (this.list.some((item, idx) => item.name === str && idx !== index)) {
        this.$message.warning('属性名称不能重复')

        evt.target.select()
      }
    },
    validator() {
      if (this.list.some(item => !(item.name + '').trim().length)) {
        this.$message.warning('属性名称不能为空')

        return false
      } else {
        const names = this.list.map(item => (item.name + '').trim())

        const uniqueNames = [...new Set(names)]

        if (names.length !== uniqueNames.length) {
          this.$message.warning('属性名称不能重复')

          return false
        }
      }
      return true
    },
  },
}
</script>

<style scoped lang="scss">
$borderColor: #e8e8e8;
.enum-item {
  display: flex;
  align-items: center;
  // &:not(:last-child) {
  // 	border-bottom: 1px solid #e8e8e8
  // }
  &:first-child {
    .enum-item--col {
      border-top: 1px solid $borderColor;
    }
  }
  &--col {
    width: 150px;
    padding: 2px 10px;
    border-bottom: 1px solid $borderColor;
    border-left: 1px solid $borderColor;
    & + & {
      border-right: 1px solid $borderColor;
    }

    &.input-cell {
      padding: 0;
    }

    & > .input {
      border: none;
      width: 100%;
      min-height: 26px;
      padding: 0 10px;
      border-radius: 0;
    }

    .ant-input.has-error {
      border-color: #f5222d;
      &:focus {
        outline: none;
        box-shadow: 0 0 0 2px rgba(245, 34, 45, 0.2);
      }
    }
  }
  &--actions {
    width: 46px;
    margin-left: 6px;
    border: none !important;
  }
  .action-icon {
    padding: 0 2px;
    color: #b9b9b9;
    &:hover {
      color: #a9a9a9;
    }
  }
}
</style>
