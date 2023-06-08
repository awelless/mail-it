<template>
  <q-card bordered>
    <q-card-section>
      <router-link class="text-h6 text-primary text-bold" style="text-decoration: none" :to="`/applications/${application.id}`">
        {{ application.name }}
      </router-link>
      <div class="text-subtitle2">id: {{ application.id }}</div>
    </q-card-section>
    <q-card-section>
      <p class="text-body1">State: {{ application.state }}</p>
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import { Application } from 'src/models/Application'

defineProps<{
  application: Application
}>()
</script>
