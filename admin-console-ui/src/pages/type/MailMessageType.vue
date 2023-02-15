<template>
  <q-spinner v-if='!type' color='primary' size='3em' />
  <div v-else class='q-gutter-md'>
    <p class='text-h4 text-primary text-bold'>{{ type.name }} #{{ type.id }}</p>
    <p class='text-body1'>{{ type.description }}</p>
    <p class='text-body1'>Max Retries: {{ type.maxRetriesCount ?? 'infinite' }}</p>
    <p class='text-body1'>Content type: {{ type.contentType }}</p>
    <p v-if='type.contentType === MailMessageContentType.HTML'>Template engine: {{ type.templateEngine }}</p>

    <q-dialog v-model='showDelete' persistent>
      <q-card>
        <q-card-section class='row items-center'>
          <span class='q-ml-sm'>
            You're about to delete mail message type: {{ type.name }}.
            <br>
            Do you want to send already submitted messages or delete this type without awaiting?
          </span>
        </q-card-section>

        <q-card-actions align='right'>
          <q-btn class='text-black bg-white' v-close-popup>Back</q-btn>
          <q-btn color='negative' @click='deleteType(false)'>Delete gracefully</q-btn>
          <q-btn color='negative' @click='deleteType(true)'>Delete forcefully</q-btn>
        </q-card-actions>
      </q-card>
    </q-dialog>

    <div class='row'>
      <div class='col-md-4 q-gutter-sm'>
        <q-btn to='/types' class='text-black bg-white' style='width:20%'>Back</q-btn>
        <q-btn :to='`/types/${type.id}/edit`' color='info' style='width:20%'>Edit</q-btn>
        <q-btn color='negative' @click='showDelete = true' style='width:20%'>Delete</q-btn>
      </div>
    </div>
  </div>
</template>

<script setup lang='ts'>
import { ref } from 'vue'
import MailMessageType, { MailMessageContentType } from 'src/models/MailMessageType'
import mailMessageTypeClient from 'src/client/mailMessageTypeClient'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps<{
  id: string
}>()

const type = ref<MailMessageType | null>(null)
const showDelete = ref(false)

async function load() {
  type.value = await mailMessageTypeClient.getById(props.id)
}

async function deleteType(force: boolean) {
  if (!type.value) {
    return
  }

  await mailMessageTypeClient.deleteType(type.value.id, force)
  await router.push('/types')
}

load()
</script>

<style scoped></style>
