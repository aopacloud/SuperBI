<template>
  <div ref="item" class="vue-grid-layout" :style="mergedStyle">
    <slot></slot>
    <grid-item
      class="vue-grid-placeholder"
      v-show="isDragging"
      :x="placeholder.x"
      :y="placeholder.y"
      :w="placeholder.w"
      :h="placeholder.h"
      :i="placeholder.i"
    ></grid-item>
  </div>
</template>
<style>
.vue-grid-layout {
  position: relative;
  transition: height 200ms ease;
}
</style>
<script>
import emittor from '@/common/plugins/emittor'
import elementResizeDetectorMaker from 'element-resize-detector'

import {
  bottom,
  compact,
  getLayoutItem,
  moveElement,
  validateLayout,
  cloneLayout,
  getAllCollisions
} from '../helpers/utils'
import {
  getBreakpointFromWidth,
  getColsFromBreakpoint,
  findOrGenerateResponsiveLayout
} from '../helpers/responsiveUtils'
//var eventBus = require('./eventBus');

import GridItem from './GridItem.vue'
import {
  addWindowEventListener,
  removeWindowEventListener
} from '../helpers/DOM'
import { getRandomKey } from '@/common/utils/string'

export default {
  name: 'GridLayout',
  provide() {
    return {
      eventBus: null,
      layout: this,
    }
  },
  components: {
    GridItem
  },
  props: {
    // If true, the container height swells and contracts to fit contents
    autoSize: {
      type: Boolean,
      default: true
    },
    colNum: {
      type: Number,
      default: 12
    },
    rowHeight: {
      type: Number,
      default: 150
    },
    maxRows: {
      type: Number,
      default: Infinity
    },
    margin: {
      type: Array,
      default: function () {
        return [10, 10]
      }
    },
    isDraggable: {
      type: Boolean,
      default: true
    },
    isResizable: {
      type: Boolean,
      default: true
    },
    isMirrored: {
      type: Boolean,
      default: false
    },
    useCssTransforms: {
      type: Boolean,
      default: true
    },
    verticalCompact: {
      type: Boolean,
      default: true
    },
    layout: {
      type: Array,
      required: true
    },
    responsive: {
      type: Boolean,
      default: false
    },
    responsiveLayouts: {
      type: Object,
      default: function () {
        return {}
      }
    },
    breakpoints: {
      type: Object,
      default: function () {
        return { lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 }
      }
    },
    cols: {
      type: Object,
      default: function () {
        return { lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 }
      }
    },
    preventCollision: {
      type: Boolean,
      default: false
    },
    useStyleCursor: {
      type: Boolean,
      default: true
    }
  },
  data: function () {
    return {
      uniquePrefix: this.uniquePrefix + '' + getRandomKey() + '-',
      width: null,
      mergedStyle: {},
      lastLayoutLength: 0,
      isDragging: false,
      placeholder: {
        x: 0,
        y: 0,
        w: 0,
        h: 0,
        i: -1
      },
      layouts: {}, // array to store all layouts from different breakpoints
      lastBreakpoint: null, // store last active breakpoint
      originalLayout: null // store original Layout
    }
  },
  created() {
    const self = this

    // Accessible refernces of functions for removing in beforeDestroy
    self.resizeEventHandler = function (eventType, i, x, y, h, w) {
      self.resizeEvent(eventType, i, x, y, h, w)
    }

    self.dragEventHandler = function (eventType, i, x, y, h, w) {
      self.dragEvent(eventType, i, x, y, h, w)
    }
    if (!self._provided) self._provided = {}

    self._provided.eventBus = emittor
    self.eventBus = self._provided.eventBus
    emittor.on(this.uniquePrefix + 'resizeEvent', self.resizeEventHandler)
    emittor.on(this.uniquePrefix + 'dragEvent', self.dragEventHandler)
    self.$emit('layout-created', self.layout)
  },
  beforeDestroy: function () {
    //Remove listeners
    emittor.off(this.uniquePrefix + 'resizeEvent', this.resizeEventHandler)
    emittor.off(this.uniquePrefix + 'dragEvent', this.dragEventHandler)
    this.eventBus.destroy()
    removeWindowEventListener('resize', this.onWindowResize)
    if (this.erd) {
      this.erd.uninstall(this.$refs.item)
    }
  },
  beforeMount: function () {
    this.$emit('layout-before-mount', this.layout)
  },
  mounted: function () {
    this.$emit('layout-mounted', this.layout)
    this.$nextTick(function () {
      validateLayout(this.layout)

      this.originalLayout = this.layout
      const self = this
      this.$nextTick(function () {
        self.onWindowResize()

        self.initResponsiveFeatures()

        //self.width = self.$el.offsetWidth;
        addWindowEventListener('resize', self.onWindowResize)

        compact(self.layout, self.verticalCompact)

        self.$emit('layout-updated', self.layout)

        self.updateHeight()
        self.$nextTick(function () {
          this.erd = elementResizeDetectorMaker({
            strategy: 'scroll', //<- For ultra performance.
            // See https://github.com/wnr/element-resize-detector/issues/110 about callOnAdd.
            callOnAdd: false
          })
          this.erd.listenTo(self.$refs.item, function () {
            self.onWindowResize()
          })
        })
      })
    })
  },
  watch: {
    width: function (newval, oldval) {
      const self = this
      this.$nextTick(function () {
        //this.$broadcast("updateWidth", this.width);
        emittor.emit(this.uniquePrefix + 'updateWidth', this.width)
        if (oldval === null) {
          /*
                            If oldval == null is when the width has never been
                            set before. That only occurs when mouting is
                            finished, and onWindowResize has been called and
                            this.width has been changed the first time after it
                            got set to null in the constructor. It is now time
                            to issue layout-ready events as the GridItems have
                            their sizes configured properly.

                            The reason for emitting the layout-ready events on
                            the next tick is to allow for the newly-emitted
                            updateWidth event (above) to have reached the
                            children GridItem-s and had their effect, so we're
                            sure that they have the final size before we emit
                            layout-ready (for this GridLayout) and
                            item-layout-ready (for the GridItem-s).

                            This way any client event handlers can reliably
                            invistigate stable sizes of GridItem-s.
                        */
          this.$nextTick(() => {
            this.$emit('layout-ready', self.layout)
          })
        }
        this.updateHeight()
      })
    },
    layout: function () {
      this.layoutUpdate()
    },
    colNum: function (val) {
      emittor.emit(this.uniquePrefix + 'setColNum', val)
    },
    rowHeight: function () {
      emittor.emit(this.uniquePrefix + 'setRowHeight', this.rowHeight)
    },
    isDraggable: function () {
      emittor.emit(this.uniquePrefix + 'setDraggable', this.isDraggable)
    },
    isResizable: function () {
      emittor.emit(this.uniquePrefix + 'setResizable', this.isResizable)
    },
    responsive() {
      if (!this.responsive) {
        this.$emit('update:layout', this.originalLayout)
        emittor.emit(this.uniquePrefix + 'setColNum', this.colNum)
      }
      this.onWindowResize()
    },
    maxRows: function () {
      emittor.emit(this.uniquePrefix + 'setMaxRows', this.maxRows)
    },
    margin() {
      this.updateHeight()
    }
  },
  methods: {
    layoutUpdate() {
      if (this.layout !== undefined && this.originalLayout !== null) {
        if (this.layout.length !== this.originalLayout.length) {
          // console.log("### LAYOUT UPDATE!", this.layout.length, this.originalLayout.length);

          let diff = this.findDifference(this.layout, this.originalLayout)
          if (diff.length > 0) {
            // console.log(diff);
            if (this.layout.length > this.originalLayout.length) {
              this.originalLayout = this.originalLayout.concat(diff)
            } else {
              this.originalLayout = this.originalLayout.filter(obj => {
                return !diff.some(obj2 => {
                  return obj.i === obj2.i
                })
              })
            }
          }

          this.lastLayoutLength = this.layout.length
          this.initResponsiveFeatures()
        }

        compact(this.layout, this.verticalCompact)
        emittor.emit(this.uniquePrefix + 'updateWidth', this.width)
        this.updateHeight()

        this.$emit('layout-updated', this.layout)
      }
    },
    updateHeight: function () {
      this.mergedStyle = {
        height: this.containerHeight()
      }
    },
    onWindowResize: function () {
      if (
        this.$refs !== null &&
        this.$refs.item !== null &&
        this.$refs.item !== undefined
      ) {
        this.width = this.$refs.item.offsetWidth
      }
      emittor.emit(this.uniquePrefix + 'resizeEvent')
    },
    containerHeight: function () {
      if (!this.autoSize) return
      // console.log("bottom: " + bottom(this.layout))
      // console.log("rowHeight + margins: " + (this.rowHeight + this.margin[1]) + this.margin[1])
      const containerHeight =
        bottom(this.layout) * (this.rowHeight + this.margin[1]) +
        this.margin[1] +
        'px'
      return containerHeight
    },
    dragEvent: function (eventName, id, x, y, h, w) {
      //console.log(eventName + " id=" + id + ", x=" + x + ", y=" + y);
      let l = getLayoutItem(this.layout, id)
      //GetLayoutItem sometimes returns null object
      if (l === undefined || l === null) {
        l = { x: 0, y: 0 }
      }

      if (eventName === 'dragmove' || eventName === 'dragstart') {
        this.placeholder.i = id
        this.placeholder.x = l.x
        this.placeholder.y = l.y
        this.placeholder.w = w
        this.placeholder.h = h
        this.$nextTick(function () {
          this.isDragging = true
        })
        //this.$broadcast("updateWidth", this.width);
        emittor.emit(this.uniquePrefix + 'updateWidth', this.width)
      } else {
        this.$nextTick(function () {
          this.isDragging = false
        })
      }

      // Move the element to the dragged location.
      this.$emit(
        'update:layout',
        moveElement(this.layout, l, x, y, true, this.preventCollision)
      )
      compact(this.layout, this.verticalCompact)
      // needed because vue can't detect changes on array element properties
      emittor.emit(this.uniquePrefix + 'compact')
      this.updateHeight()
      if (eventName === 'dragend') this.$emit('layout-updated', this.layout)
    },
    resizeEvent: function (eventName, id, x, y, h, w) {
      let l = getLayoutItem(this.layout, id)
      //GetLayoutItem sometimes return null object
      if (l === undefined || l === null) {
        l = { h: 0, w: 0 }
      }
      // console.log("LLLLLLLLLLL => " + this.preventCollision)

      let hasCollisions
      if (this.preventCollision) {
        const collisions = getAllCollisions(this.layout, {
          ...l,
          x,
          y,
          w,
          h
        }).filter(layoutItem => layoutItem.i !== l.i)
        hasCollisions = collisions.length > 0
        // console.log(JSON.stringify(collisions))

        // If we're colliding, we need adjust the placeholder.
        if (hasCollisions) {
          // adjust w && h to maximum allowed space
          let leastX = Infinity,
            leastY = Infinity
          collisions.forEach(layoutItem => {
            if (layoutItem.x > l.x) leastX = Math.min(leastX, layoutItem.x)
            if (layoutItem.y > l.y) leastY = Math.min(leastY, layoutItem.y)
          })

          if (Number.isFinite(leastX)) l.w = leastX - l.x
          if (Number.isFinite(leastY)) l.h = leastY - l.y
        }
      }

      if (!hasCollisions) {
        l.x = x
        l.y = y
        // Set new width and height.
        l.w = w
        l.h = h
      }

      if (eventName === 'resizestart' || eventName === 'resizemove') {
        this.placeholder.i = id
        this.placeholder.x = l.x
        this.placeholder.y = l.y
        this.placeholder.w = l.w
        this.placeholder.h = l.h
        // console.log("PLACEHOLDER => " + JSON.stringify(this.placeholder))
        this.$nextTick(function () {
          this.isDragging = true
        })
        //this.$broadcast("updateWidth", this.width);
        emittor.emit(this.uniquePrefix + 'updateWidth', this.width)
      } else {
        this.$nextTick(function () {
          this.isDragging = false
        })
      }

      if (this.responsive) this.responsiveGridLayout()

      compact(this.layout, this.verticalCompact)
      emittor.emit(this.uniquePrefix + 'compact')
      this.updateHeight()

      if (eventName === 'resizeend') this.$emit('layout-updated', this.layout)
    },

    // finds or generates new layouts for set breakpoints
    responsiveGridLayout() {
      let newBreakpoint = getBreakpointFromWidth(this.breakpoints, this.width)
      let newCols = getColsFromBreakpoint(newBreakpoint, this.cols)

      // save actual layout in layouts
      if (this.lastBreakpoint != null && !this.layouts[this.lastBreakpoint])
        this.layouts[this.lastBreakpoint] = cloneLayout(this.layout)

      // Find or generate a new layout.
      let layout = findOrGenerateResponsiveLayout(
        this.originalLayout,
        this.layouts,
        this.breakpoints,
        newBreakpoint,
        this.lastBreakpoint,
        newCols,
        this.verticalCompact
      )

      // Store the new layout.
      this.layouts[newBreakpoint] = layout

      if (this.lastBreakpoint !== newBreakpoint) {
        this.$emit('breakpoint-changed', newBreakpoint, layout)
      }

      // new prop sync
      this.$emit('update:layout', layout)

      this.lastBreakpoint = newBreakpoint
      emittor.emit(
        this.uniquePrefix + 'setColNum',
        getColsFromBreakpoint(newBreakpoint, this.cols)
      )
    },

    // clear all responsive layouts
    initResponsiveFeatures() {
      // clear layouts
      this.layouts = Object.assign({}, this.responsiveLayouts)
    },

    // find difference in layouts
    findDifference(layout, originalLayout) {
      //Find values that are in result1 but not in result2
      let uniqueResultOne = layout.filter(function (obj) {
        return !originalLayout.some(function (obj2) {
          return obj.i === obj2.i
        })
      })

      //Find values that are in result2 but not in result1
      let uniqueResultTwo = originalLayout.filter(function (obj) {
        return !layout.some(function (obj2) {
          return obj.i === obj2.i
        })
      })

      //Combine the two arrays of unique entries#
      return uniqueResultOne.concat(uniqueResultTwo)
    }
  }
}
</script>
