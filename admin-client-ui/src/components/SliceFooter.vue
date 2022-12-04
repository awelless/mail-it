<template>
  <q-pagination
    :model-value='slice.page'
    :max='slice.page + (slice.last ? 0 : 1)'
    direction-links
    @update:model-value='fetchFunction'
  />
</template>

<script setup lang='ts'>
import { defineProps } from 'vue'
import Slice from 'src/models/Slice'

defineProps<{
  slice: Slice<unknown>,
  fetchFunction: (page: number) => void,
}>()
</script>

<style scoped></style>
