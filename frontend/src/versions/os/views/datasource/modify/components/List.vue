<template>
  <div class="wrapper">
    <ul class="list">
      <li v-for="item in engines" :key="item.name">
        <div class="box" :class="{ disabled: item.disabled }" @click="toCreate(item)">
          <div class="img-box">
            <img :src="item.img" alt="item.name" class="img" />
          </div>
          <div class="desc">{{ item.name }}</div>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { engines } from '../config'

const emits = defineEmits(['click'])

const toCreate = row => {
  if (row.disabled) return

  emits('click', row.engine)
}
</script>

<style lang="scss" scoped>
.wrapper {
  width: 950px;
  margin: 0 auto;
}
.list {
  display: grid;
  grid-template-columns: repeat(5, 1fr); /* 设置4列 */
  grid-gap: 20px; /* 设置列之间的间距 */
}

.box {
  display: flex;
  flex-direction: column;
  margin: 0 auto;
  width: 172px;
  height: 172px;
  padding: 12px;
  border: 1px solid #e5e7e9;
  border-radius: 4px;
  text-align: center;
  cursor: pointer;

  &:hover {
    background-color: #eee;
  }

  &.disabled {
    position: relative;
    background-color: #f1f1f1;
    border-color: transparent;
    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background-color: rgba(255, 255, 255, 0.65);
      z-index: 1;
      cursor: not-allowed;
    }
    &::after {
      content: '暂未开放';
      position: absolute;
      inset: 0;
      line-height: 172px;
      z-index: 1;
      cursor: not-allowed;
    }
  }
}

.img-box {
  flex: 1;
  background: #fff;
  overflow: hidden;
}
.img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.desc {
  margin-top: 12px;
  color: #999;
}
</style>
