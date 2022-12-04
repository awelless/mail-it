import { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('pages/MailMessagesPage.vue'),
  },
  {
    path: '/types',
    component: () => import('pages/MailTypesPage.vue'),
  },
  {
    path: '/types/:id',
    component: () => import('pages/MailTypePage.vue'),
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/MailMessagesPage.vue'),
  },
]

export default routes
