<template>
  <div v-if="mails == null">Mails are loading...</div>
  <div v-else>
    <div v-for="mail in mails.content" :key="mail.id">
      <MailMessageCard :mail="mail" />
    </div>
    <br />
    <SliceFooter :slice="mails" />
  </div>
</template>

<script setup lang="ts">
import MailMessageCard from 'src/components/MailMessageCard.vue'
import Slice from 'src/models/Slice'
import MailMessage, { MailMessageStatus } from 'src/models/MailMessage'
import { ref } from 'vue'
import SliceFooter from 'components/SliceFooter.vue'

const mails = ref<Slice<MailMessage> | null>(null)

mails.value = {
  page: 1,
  size: 10,
  content: [
    {
      id: 2,
      emailFrom: 'me2@gmail.com',
      emailTo: 'you2@gmail.com',
      type: {
        id: 1,
        name: 'Casual mail',
      },
      createdAt: new Date(),
      status: MailMessageStatus.PENDING,
      failedCount: 0,
    },
    {
      id: 1,
      emailFrom: 'me@gmail.com',
      emailTo: 'you@gmail.com',
      type: {
        id: 1,
        name: 'Casual mail',
      },
      createdAt: new Date(),
      status: MailMessageStatus.PENDING,
      failedCount: 0,
    },
  ],
}
</script>

<style scoped></style>
