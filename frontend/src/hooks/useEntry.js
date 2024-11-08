import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute } from 'vue-router'
import useAppStore from '@/store/modules/app'

export default function useEntry() {
  // const route = useRoute()
  // const appStore = useAppStore()

  // 来源
  const origin = ref()
  // 超级令牌
  const pToken = ref()
  const initEntry = () => {
    const { o, st } = useRoute().query

    if (o || st) {
      useAppStore().setEntryInfo({ origin: o, pToken: st })
    }
    origin.value = o
    pToken.value = st
  }

  const uninitEntry = () => {
    useAppStore().setEntryInfo()
  }
  onMounted(initEntry)
  onBeforeUnmount(uninitEntry)

  // 有超级令牌
  const isSuperToken = computed(() => !!pToken.value)
  // 来自iframe
  const isIframe = computed(() => origin.value === 'iframe')
  // 来自监控
  const isMonitor = computed(() => origin.value === 'monitor')

  return { origin, pToken, isSuperToken, isIframe, isMonitor }
}
