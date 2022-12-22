<template>
  <q-pagination
    :model-value='slice.page + 1'
    :max='maxPage'
    direction-links
    @update:model-value='updateFunction'
  />
</template>

<script setup lang='ts'>
import { computed, defineProps } from 'vue'
import Slice from 'src/models/Slice'

const props = defineProps<{
  slice: Slice<unknown>,
  fetchFunction: (page: number) => Promise<void>,
}>()

const maxPage = computed(() => props.slice.page + 1 + (props.slice.last ? 0 : 1))

async function updateFunction(page: number) {
  await props.fetchFunction(page - 1)
}
</script>

<style scoped></style>
