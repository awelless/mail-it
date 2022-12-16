<template>
  <p class='text-h4 text-primary text-bold q-pb-md'>
    Edit Mail Message Type: {{ mailMessageType.name }} #{{ mailMessageType.id }}
  </p>
  <MailMessageTypeEditor
    :mail-message-type='mailMessageType'
    submission-button-message='Edit'
    :submission-action='edit'
    :back-path='`/types/${mailMessageType.id}`'
  />
</template>

<script setup lang='ts'>
import MailMessageTypeEditor from 'components/type/MailMessageTypeEditor.vue'
import MailMessageType, { MailMessageContentType } from 'src/models/MailMessageType'
import { ref } from 'vue'

const props = defineProps<{
  id: number
}>()

const mailMessageType = ref<MailMessageType>({
  id: 1,
  name: 'Casual mail',
  description: 'some description',
  maxRetriesCount: 10,
  contentType: MailMessageContentType.PLAIN_TEXT,
})

function edit(type: MailMessageType) {
  console.log('update type')
}
</script>

<style scoped>

</style>
