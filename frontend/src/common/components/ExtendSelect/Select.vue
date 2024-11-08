<script lang="jsx">
import {
  h,
  ref,
  computed,
  toRaw,
  watchEffect,
  defineComponent,
  useAttrs
} from 'vue'
import { Select as ASelect, Divider, Checkbox, Tooltip } from 'ant-design-vue'

export default defineComponent({
  name: 'ExtendAntSelect',
  props: {
    // 显示全选
    showAllCheck: {
      type: Boolean,
      default: true
    },
    value: {
      type: [Array, String]
    },
    // 显示tooltip
    showTooltip: Boolean,
    allSelectedLabel: String
  },
  emits: [
    'update:value',
    'search',
    'dropdownVisibleChange',
    'change',
    'allSelected'
  ],
  setup(props, ctx) {
    const { emit, attrs, slots } = ctx
    const { mode, filterOption, options } = attrs

    const selectRef = ref(null)

    // 是否多选
    const multiple = computed(() => mode === 'multiple' || mode === 'tags')

    // 搜索方法
    const filterFn = computed(() => {
      if (filterOption) return filterOption

      return (s, t) => {
        const label = t.label || t.children().map(t => t.children.toLowerCase())

        return label.includes(s) || String(t.value).toLowerCase().includes(s)
      }
    })

    // 归一选项
    const normalizedOptions = computed(() => {
      const optionsSlots = slots.default?.().filter(t => t.type.isSelectOption)
      if (!optionsSlots?.length) {
        return attrs.options
      } else {
        return optionsSlots.map(t => {
          const { value, disabled: d } = t.props
          const disabled = typeof d === 'boolean' ? d : d === '' ? true : false

          return {
            disabled,
            value,
            label: t.children
              .default()
              .map(t => t.children)
              .join('')
          }
        })
      }
    })

    const filteredOptions = computed(() => {
      const s = searchValue.value.trim()

      if (!s.length) {
        return normalizedOptions.value
      } else {
        return normalizedOptions.value.filter(t => filterFn.value(s, t))
      }
    })

    // 所有有效的值
    const validValues = computed(() =>
      filteredOptions.value
        .filter(t => !t.disabled)
        .map(t => t.value ?? t.props.value)
    )

    // 当前值
    const modelValue = ref()
    watchEffect(() => {
      const v = props.value
      if (multiple.value) {
        modelValue.value = Array.isArray(v) ? v : []
      } else {
        modelValue.value = v
      }
    })

    // 显示label
    const displayLabel = computed(() => {
      if (!multiple.value || !modelValue.value) return
      if (multiple.value && !modelValue.value.length) return

      const val = modelValue.value
        ? multiple.value
          ? [...modelValue.value]
          : [modelValue.value]
        : undefined

      if (!val) return

      if (multiple.value && props.allSelectedLabel) {
        if (
          val.length === filteredOptions.value.length &&
          val.every(v => filteredOptions.value.some(t => t.value === v))
        ) {
          return props.allSelectedLabel
        }
      }

      return val
        .map(t => {
          const item = filteredOptions.value.find(o => o.value === t)
          return item?.label ?? t
        })
        .join(',')
    })

    const tooltipLabel = computed(() => {
      if (!multiple.value) return
      if (!props.showTooltip) return

      return displayLabel.value
    })

    // 渲染的选项
    const selectOptions = ref([])
    // 生成选项
    const generateSelectOptions = () => {
      const l = [...filteredOptions.value]

      if (!multiple.value) {
        selectOptions.value = l
        return
      }

      const values = toRaw(modelValue.value)
      if (values?.length) {
        values.forEach((val, i) => {
          const index = l.findIndex(t => t?.value === val)
          if (index > -1) {
            const tem = l[i]

            l[i] = l[index]
            l[index] = tem
          }
        })
      }
      selectOptions.value = l
    }

    const onDropdownMousedown = e => {
      if (e.target.tagName === 'INPUT') return
      e.preventDefault()
    }

    // 全选、全不选
    const onCheckedChange = e => {
      const checked = e.target.checked
      const hasSearched = searchValue.value.trim().length > 0

      if (!hasSearched) {
        modelValue.value = checked ? [...validValues.value] : []
        emit('update:value', toRaw(modelValue.value))
      } else {
        const preVal = modelValue.value.filter(
          t => !validValues.value.includes(t)
        )

        modelValue.value = checked ? [...preVal, ...validValues.value] : preVal
        emit('update:value', toRaw(modelValue.value))
      }
      emit('allSelected', checked)
      emit('change', toRaw(modelValue.value))
    }

    const onDropdownVisibleChange = e => {
      isFocus.value = e
      if (e) {
        generateSelectOptions()
      } else {
        selectRef.value.blur()
      }
      searchValue.value = ''

      emit('dropdownVisibleChange', e)
    }

    const onChange = e => {
      modelValue.value = e
      emit('update:value', e)
      emit('change', e)
    }

    const isFocus = ref(false)
    const onFocus = () => {
      isFocus.value = true
    }

    const onBlur = () => {
      searchValue.value = ''
      isFocus.value = false
    }

    // 搜索值
    const searchValue = ref('')
    const onSearch = e => {
      searchValue.value = e
      emit('search', e)
    }

    // 全选
    const allChecked = computed(() =>
      validValues.value.length
        ? validValues.value.every(t => modelValue.value.includes(t))
        : false
    )

    // 半选
    const indeterminate = computed(() => {
      if (!modelValue.value.length) return false
      if (allChecked.value) return false

      return true
    })

    /**
     * 创建一个下拉菜单渲染函数
     * @param options 选项对象
     * @param options.menuNode 下拉菜单节点
     * @returns 下拉菜单渲染函数
     */
    const createDropdownRender = ({ menuNode }) => {
      return () => [
        <div className='dropdown-container' onMousedown={onDropdownMousedown}>
          <div className='title'>
            {indeterminate.value}
            <div>
              <Checkbox
                checked={allChecked.value}
                indeterminate={indeterminate.value}
                onChange={onCheckedChange}
              >
                全选
              </Checkbox>
            </div>
            <span>
              {modelValue.value.length} / {validValues.value.length}
            </span>
          </div>
          <Divider style='margin: 4px 0' />
        </div>,
        h(menuNode)
      ]
    }

    const selectTextClassName = computed(() => {
      return {
        'is-focus': isFocus.value,
        'search-active': searchValue.value.trim().length > 0
      }
    })

    return () => {
      if (multiple.value && props.showAllCheck) {
        slots.dropdownRender = e => createDropdownRender(e)()
      }
      const ats = useAttrs()

      return (
        <div className='extend-ant-select-container'>
          <Tooltip title={tooltipLabel.value}>
            <ASelect
              style='width: 100%'
              ref={e => (selectRef.value = e)}
              {...{ ...ats }}
              value={modelValue.value}
              options={selectOptions.value}
              popupClassName='extend-ant-select-overlay'
              searchValue={searchValue.value}
              filterOption={filterFn.value}
              onDropdownVisibleChange={onDropdownVisibleChange}
              onSearch={onSearch}
              onChange={onChange}
              onFocus={onFocus}
              onBlur={onBlur}
            >
              {slots}
            </ASelect>
          </Tooltip>
          {multiple.value ? (
            <div className='extend-select-label'>
              <div
                class={['extend-select-label-text', selectTextClassName.value]}
              >
                {displayLabel.value}
              </div>
            </div>
          ) : null}
        </div>
      )
    }
  }
})
</script>

<style lang="scss">
.extend-ant-select-container {
  position: relative;

  .ant-select-selection-overflow-item:not(
      .ant-select-selection-overflow-item-suffix
    ) {
    display: none;
  }
}
.extend-ant-select-overlay {
  min-width: 120px !important;
  .dropdown-container {
    .title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 6px;
      &-input {
        width: 200px;
        margin-left: 16px;
      }
    }
  }
}
.extend-select-label {
  position: absolute;
  width: 100%;
  top: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  padding: 0 24px 0 8px;
  pointer-events: none;
  z-index: 2;
  .extend-select-label-text {
    display: block;
    &.is-focus {
      opacity: 0.4;
    }
    &.search-active {
      opacity: 0.1;
    }
    @extend .ellipsis;
  }
}
</style>
