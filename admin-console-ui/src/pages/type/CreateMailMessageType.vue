<template>
  <p class='text-h4 text-primary text-bold q-pb-md'>Create new Mail Message Type</p>
  <MailMessageTypeEditor
    submission-button-message='Create'
    :submission-action='create'
    back-path='/types'
  />
</template>

<script setup lang='ts'>
import MailMessageTypeEditor from 'components/type/MailMessageTypeEditor.vue'
import MailMessageType from 'src/models/MailMessageType'
import mailMessageTypeClient from 'src/client/mailMessageTypeClient'
import { useRouter } from 'vue-router'

const router = useRouter()

async function create(type: MailMessageType) {
  const createdType = await mailMessageTypeClient.create(type)
  await router.push(`/types/${createdType.id}`)
}
</script>

<style scoped>

</style>
