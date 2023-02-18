<template>
  <q-spinner v-if="!type" color="primary" size="3em" />
  <div v-else>
    <p class="text-h4 text-primary text-bold q-pb-md">Edit Mail Message Type: {{ type.name }} #{{ type.id }}</p>
    <MailMessageTypeEditor :mail-message-type="type" submission-button-message="Edit" :submission-action="update" :back-path="`/types/${type.id}`" />
  </div>
</template>

<script setup lang="ts">
import MailMessageTypeEditor from 'components/type/MailMessageTypeEditor.vue'
import MailMessageType from 'src/models/MailMessageType'
import { ref } from 'vue'
import mailMessageTypeClient from 'src/client/mailMessageTypeClient'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps<{
  id: string
}>()

const type = ref<MailMessageType | null>(null)

async function load() {
  type.value = await mailMessageTypeClient.getById(props.id)
}

load()

async function update(type: MailMessageType) {
  await mailMessageTypeClient.update(type)
  await router.push(`/types/${props.id}`)
}
</script>

<style scoped></style>
