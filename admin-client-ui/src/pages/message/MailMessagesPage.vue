<template>
  <q-spinner v-if='!mails' color='primary' size='3em' />
  <div v-else class='q-gutter-md'>
    <div v-for='mail in mails.content' :key='mail.id'>
      <MailMessageCard :mail='mail' />
    </div>
    <SliceFooter :slice='mails' :fetch-function='fetchFunction' />
  </div>
</template>

<script setup lang='ts'>
import MailMessageCard from 'components/message/MailMessageCard.vue'
import Slice from 'src/models/Slice'
import MailMessage, { MailMessageStatus } from 'src/models/MailMessage'
import { ref } from 'vue'
import SliceFooter from 'components/SliceFooter.vue'

const mails = ref<Slice<MailMessage> | null>(null)

mails.value = {
  page: 1,
  size: 10,
  last: false,
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

const fetchFunction = (page: number) => {
  if (page == 1) {
    mails.value = {
      page: 1,
      size: 10,
      last: false,
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
  } else {
    mails.value = {
      page: 2,
      size: 10,
      last: true,
      content: [
        {
          id: 4,
          emailFrom: 'me4@gmail.com',
          emailTo: 'you4@gmail.com',
          type: {
            id: 1,
            name: 'Casual mail',
          },
          createdAt: new Date(),
          status: MailMessageStatus.PENDING,
          failedCount: 0,
        },
        {
          id: 3,
          emailFrom: 'me3@gmail.com',
          emailTo: 'you3@gmail.com',
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
  }
}
</script>

<style scoped></style>
