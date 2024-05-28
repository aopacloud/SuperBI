import exportExcel from '@/components/Chart/Table/export'

self.addEventListener('message', e => {
  const { data } = e
  const { type, payload } = data

  if (type === 'export') {
    exportExcel(payload).then(e => {
      postMessage({ type: 'exportResult', payload: e })
    })
  }
})

self.addEventListener('error', e => {
  console.error('worker error: ', e)
})
