import { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('pages/message/MailMessagesPage.vue'),
  },
  {
    path: '/types',
    component: () => import('pages/type/MailMessageTypesPage.vue'),
  },
  {
    path: '/types/create',
    component: () => import('pages/type/CreateMailMessageType.vue'),
  },
  {
    path: '/types/:id',
    component: () => import('pages/type/MailMessageType.vue'),
    props: true,
  },
  {
    path: '/types/:id/edit',
    component: () => import('pages/type/EditMailMessageType.vue'),
    props: true,
  },
  {
    path: '/:catchAll(.*)*',
    redirect: '/',
  },
]

export default routes
