<template>
  <q-spinner v-if='!type' color='primary' size='3em' />
  <div v-else class='q-gutter-md'>
    <p class='text-h4 text-primary text-bold'>{{ type.name }} #{{ type.id }}</p>
    <p class='text-body1'>{{ type.description }}</p>
    <p class='text-body1'>Max Retries: {{ type.maxRetriesCount ?? 'infinite' }}</p>
    <p class='text-body1'>Content type: {{ type.contentType }}</p>
    <p v-if='type.contentType === MailMessageContentType.HTML'>Template engine: {{ type.templateEngine }}</p>

    <div class='row'>
      <div class='col-md-2 q-gutter-sm'>
        <q-btn :to='`/types/${type.id}/edit`' color='info' style='width:40%'>Edit</q-btn>
        <q-btn to='/types' class='text-black bg-white' style='width:40%'>Back</q-btn>
      </div>
    </div>
  </div>
</template>

<script setup lang='ts'>
import { ref } from 'vue'
import MailMessageType, { MailMessageContentType } from 'src/models/MailMessageType'
import mailMessageTypeClient from 'src/client/mailMessageTypeClient'

const props = defineProps<{
  id: string
}>()

const type = ref<MailMessageType | null>(null)

async function load() {
  type.value = await mailMessageTypeClient.getById(props.id)
}

load()
</script>

<style scoped></style>
