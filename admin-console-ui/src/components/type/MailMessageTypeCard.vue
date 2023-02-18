<template>
  <q-card bordered>
    <q-card-section>
      <router-link class="text-h6 text-primary text-bold" style="text-decoration: none" :to="`/types/${type.id}`">
        {{ type.name }}
      </router-link>
      <div class="text-subtitle2">id: {{ type.id }}</div>
    </q-card-section>
    <q-card-section>
      <p class="text-body1">Type: {{ type.contentType }}</p>
      <p v-if="type.contentType === MailMessageContentType.HTML" class="text-body1">Template engine: {{ type.templateEngine }}</p>
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import MailMessageType, { MailMessageContentType } from 'src/models/MailMessageType'

defineProps<{
  type: MailMessageType
}>()
</script>

<style scoped></style>
