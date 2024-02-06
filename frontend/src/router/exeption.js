export default [
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/exeption/index.vue'),
    hidden: true,
  },
]
