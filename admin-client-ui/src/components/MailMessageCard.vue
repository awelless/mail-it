<template>
  <q-card bordered>
    <q-card-section>
      <router-link
        class="text-h6 text-primary text-bold"
        style="text-decoration: none"
        :to="`/types/${mail.type.id}`"
        >{{ mail.type.name }}</router-link
      >
      <div class="text-subtitle2">id: {{ mail.id }}</div>
    </q-card-section>
    <q-card-section>
      <b>Status: {{ mail.status }}</b>
      <div>Created At: {{ mail.createdAt.toLocaleString() }}</div>
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import MailMessage from 'src/models/MailMessage'

defineProps<{
  mail: MailMessage
}>()
</script>

<style scoped></style>
