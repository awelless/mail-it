<template>
  <q-card bordered>
    <q-card-section>
      <p class="text-h6">Mail: #{{ mail.id }}</p>
      <p class="text-subtitle1 text-bold">
        Of type:
        <router-link class="text-subtitle1 text-primary text-bold" style="text-decoration: none" :to="`/types/${mail.type.id}`">
          {{ mail.type.name }}
        </router-link>
      </p>
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
