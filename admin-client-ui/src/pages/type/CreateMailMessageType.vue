<template>
  <p class='text-h4 text-primary text-bold q-pb-md'>Create new Mail Message Type</p>
  <MailMessageTypeEditor
    submission-button-message='Create'
    :submission-action='create'
    back-path='/types'
  />
</template>

<script setup lang='ts'>
import MailMessageTypeEditor from 'components/type/MailMessageTypeEditor.vue'
import MailMessageType from 'src/models/MailMessageType'

function create(type: MailMessageType) {
  // todo
  console.log('creation')
}
</script>

<style scoped>

</style>
