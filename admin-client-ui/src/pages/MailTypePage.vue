<template>
  <q-spinner v-if='!type' color='primary' size='3em' />
  <div v-else>
    <h4 class='text-h4 text-primary text-bold'>{{ type.name }}</h4>
    <p class='text-body1'>{{ type.description }}</p>
    <p class='text-body2'>Max Retries: {{ type.maxRetriesCount ?? 'infinite' }}</p>

    <q-btn-group>
      <q-btn @click='changeUpdateCreateForm' color='info'>Edit</q-btn>
      <q-btn @click='attemptDeleteMailType' color='negative'>Delete</q-btn>
    </q-btn-group>
  </div>
</template>

<script setup lang='ts'>
import { ref } from 'vue'
import MailMessageType, { MailMessageContentType } from 'src/models/MailMessageType'

const type = ref<MailMessageType | null>(null)

type.value = {
  id: 1,
  name: 'Casual mail',
  description: 'some description',
  maxRetriesCount: 10,
  contentType: MailMessageContentType.PLAIN_TEXT,
}
</script>

<style scoped></style>
