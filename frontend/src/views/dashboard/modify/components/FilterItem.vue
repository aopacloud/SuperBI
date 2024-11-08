<template>
  <div
    class="draggable-handler filter-layout"
    :class="{ readonly: mode !== 'EDIT' }"
  >
    <div class="filter-tools" v-if="mode === 'EDIT'">
      <a-tooltip title="编辑">
        <EditOutlined
          class="tools-btn"
          style="padding-left: 4px"
          type="edit"
          @click="edit"
        />
      </a-tooltip>
      <a-dropdown :getPopupContainer="node => node.parentNode">
        <MoreOutlined
          class="tools-btn"
          style="padding-right: 4px"
          type="more"
        />
        <template #overlay>
          <a-menu style="width: 70px" @click="onMoreClick">
            <a-menu-item key="copy" style="padding: 2px 0">
              <CopyOutlined type="copy" /> 复制
            </a-menu-item>
            <a-menu-item key="del" style="padding: 2px 0">
              <DeleteOutlined type="delete" /> 删除
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>

    <div class="filter-form-layout">
      <div class="filter-form" :class="{ filterEditable: mode === 'EDIT' }">
        <template v-if="!list.length">
          <div class="no-data">
            {{
              mode !== 'EDIT' ? '暂无查询条件' : '点击编辑按钮进入查询条件配置'
            }}
          </div>
        </template>

        <template v-else>
          <a-form layout="inline">
            <a-form-item
              v-for="item in list"
              class="filter-item"
              :key="item.id"
              :label="item.name"
              :class="{ required: item.required }"
            >
              <!-- {{ item.value }} -->
              <!-- v-model:value="item.value" -->
              <FilterRender
                from="filterItem"
                :dId="dId"
                :item="item"
                :size="item.size"
                :resizable="mode === 'EDIT'"
                v-model:value="item.value"
                @sizeChange="e => onSizeChange(e, item)"
              />
            </a-form-item>
          </a-form>
        </template>
      </div>

      <div class="filter-btns">
        <div
          class="query-btn reset"
          style="margin-right: 8px"
          :class="{ disabled: !unQueryable }"
          @click="reset"
        >
          重置
        </div>
        <div
          class="query-btn"
          :class="{ disabled: !unQueryable }"
          @click="query"
        >
          查询
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import {
  EditOutlined,
  MoreOutlined,
  CopyOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import FilterRender from './FilterRender.vue'
import { deepClone } from 'common/utils/help'
import { nextTick } from 'vue'
import { onMounted } from 'vue'

const emits = defineEmits([
  'copy',
  'delete',
  'edit',
  'query',
  'reset',
  'update:data-source'
])
const props = defineProps({
  id: {
    type: [String, Number]
  },
  dataSource: {
    // 渲染数据源
    type: Array,
    default: () => [] //.slice(0, 2),
  },
  mode: {
    type: String,
    default: 'READONLY'
  },
  dId: [String, Number], // 看板ID
  // 可查询的，防止看板未加载完重复点击导致不可预料的渲染错误
  unQueryable: {
    type: Boolean,
    default: true
  }
})

const list = ref([])
const init = () => {
  list.value = deepClone(props.dataSource)
}

watchEffect(() => {
  init()
})

onMounted(() => {})

const onMoreClick = ({ key }) => {
  if (key === 'copy') {
    emits('copy', { _id: props.id, list: list.value })
  }

  if (key === 'del') {
    emits('delete', { _id: props.id, list: list.value })
  }
}

const edit = () => {
  emits('edit', { _id: props.id, list: list.value })
}

// 必填项的校验
const unValidator = item => {
  const { filterType, required, single, value } = item

  if (!required) return false

  if (filterType === 'TEXT' || filterType === 'NUMBER') {
    return value.some(
      v => typeof v.value === 'undefined' || !v.value.trim().length
    )
  } else if (filterType === 'ENUM') {
    return typeof value === 'undefined' || !value.length
  } else if (filterType === 'TIME') {
    const { extra, offset, mode, date } = value

    if (extra.dt) {
      return false
    } else {
      return mode === 0 ? !offset.length : !date.length
    }
  } else if (filterType === 'CUSTOM') {
    return value.length === 0
  }
}

const reset = () => {
  if (!props.unQueryable) return

  init()
  query()
}
const query = () => {
  if (!props.unQueryable) return

  if (list.value.some(unValidator)) {
    message.warn('必填项不能为空')

    return false
  }

  const _getData = data => {
    return data.map(item => {
      const { filterType, filterMethod, charts = [], value } = item

      return {
        filterType,
        filterMethod,
        charts,
        value: deepClone(value)
      }
    })
  }
  emits('query', _getData(toRaw(list.value)))
}

const onSizeChange = (size, item) => {
  const t = props.dataSource.find(t => t.id === item.id)
  t.size = size
}
</script>

<style scoped lang="scss">
.btn {
  background-color: #6363ff;
  color: #fff;
  text-align: center;
  border: 1px solid transparent;
}

.filter-layout {
  position: relative;
  background-color: #fff;
  padding: 12px;
  border-radius: 2px;
  .no-data {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
  &:not(.readonly):hover {
    outline: 1px dashed #6363ff;
    .filter-tools {
      display: block;
    }
  }
}
.filter-tools {
  display: none;
  position: absolute;
  top: -10px;
  right: -1px;
  border-radius: 4px 4px 0 0;
  z-index: 9999;
  @extend .btn;
}

.filter-form-layout {
  display: flex;
}
.filter-form {
  flex: 1;
  margin-top: -12px;
  overflow: auto;
  :deep(.ant-form) {
    overflow: auto;
    .ant-form-item {
      margin-top: 12px;
      margin-left: 2px;
      .ant-row {
        line-height: 1;
        align-items: center;
      }
      .ant-form-item-label {
        position: relative;
        padding-left: 10px;
      }
      &.required .ant-form-item-label {
        &:before {
          content: '*';
          position: absolute;
          left: 2px;
          top: 2px;
          color: red;
        }
      }
    }
  }
}
.filter-btns {
  align-self: flex-end;
  display: flex;
  align-items: center;
  margin-left: 10px;
  .query-btn {
    @extend .btn;
    line-height: 1;
    padding: 8px 12px;
    border-radius: 4px;
    cursor: pointer;
    &.disabled {
      cursor: not-allowed;
    }
    &.reset {
      background-color: initial;
      color: #6363ff;
      border-color: #6363ff;
    }
  }
}
.tools {
  @extend .btn;
  &-btn {
    padding: 2px 2px;
    cursor: pointer;
  }
}

.filter-item {
  border-radius: 2px;
  border: 1px dashed transparent;
}
.filter-form.filterEditable {
  .filter-item:hover {
    border-color: #6363ff;
  }
}
</style>
