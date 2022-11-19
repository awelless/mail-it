<template>
  <q-btn-group>
    <q-btn>&lt;</q-btn>
    <q-btn disable>{{ slice.page }}</q-btn>
    <q-btn>&gt;</q-btn>
  </q-btn-group>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import Slice from 'src/models/Slice'

defineProps<{
  slice: Slice<any>
}>()
</script>

<style scoped></style>
