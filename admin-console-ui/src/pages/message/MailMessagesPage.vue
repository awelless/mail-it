<template>
  <div class="q-pt-md">
    <q-spinner v-if="!mails" color="primary" size="3em" />
    <div v-else class="q-gutter-md">
      <div v-for="mail in mails.content" :key="mail.id">
        <MailMessageCard :mail="mail" />
      </div>
      <SliceFooter :slice="mails" :fetch-function="loadPage" />
    </div>
  </div>
</template>

<script setup lang="ts">
import MailMessageCard from 'components/message/MailMessageCard.vue'
import Slice, { DEFAULT_SLICE_NUMBER, DEFAULT_SLICE_SIZE } from 'src/models/Slice'
import MailMessage from 'src/models/MailMessage'
import { ref } from 'vue'
import SliceFooter from 'components/SliceFooter.vue'
import mailMessageClient from 'src/client/mailMessageClient'

const mails = ref<Slice<MailMessage> | null>(null)

async function loadPage(page: number) {
  mails.value = await mailMessageClient.getAllSliced(page, DEFAULT_SLICE_SIZE)
}

loadPage(DEFAULT_SLICE_NUMBER)
</script>

<style scoped></style>
