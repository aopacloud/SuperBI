<script lang="jsx">
/**
 * 解决 ant-design-vue Space 在analysis页面中莫名引起splitpanes的初始化伸缩问题
 */
import { h } from 'vue'

export default {
  name: 'p-a-space',
  functional: true,
  props: {
    gap: {
      type: Number,
      default: 8,
    },
    vertical: {
      type: Boolean,
      default: false,
    },
  },
  setup({ vertical, gap }, { slots }) {
    const listStyle = { 'flex-direction': vertical ? 'column' : 'row', gap: gap + 'px' }
    // 插槽内容
    const getDefaultSlots = () =>
      slots.default().map((slot, i) => {
        return <div class='p-a-space-item'>{slot}</div>
      })

    return () =>
      h(
        <div class='p-a-space' style={listStyle}>
          {getDefaultSlots()}
        </div>
      )
  },
}
</script>

<style lang="scss" scoped>
.p-a-space {
  display: inline-flex;
  align-items: center;
}
</style>
