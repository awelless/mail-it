<template>
  <div class='row'>
    <div class='col-md-1'>
      <q-btn to='/types/create' color='info' style='width: 80%'>Create</q-btn>
    </div>
  </div>
  <div class='q-pt-md'>
    <q-spinner v-if='!types' color='primary' size='3em' />
    <div v-else class='q-gutter-md q-pt-md'>
      <div v-for='type in types.content' :key='type.id'>
        <MailMessageTypeCard :type='type' />
      </div>
      <SliceFooter :slice='types' :fetch-function='loadPage' />
    </div>
  </div>
</template>

<script setup lang='ts'>
import { ref } from 'vue'
import Slice, { DEFAULT_SLICE_NUMBER, DEFAULT_SLICE_SIZE } from 'src/models/Slice'
import SliceFooter from 'components/SliceFooter.vue'
import MailMessageTypeCard from 'components/type/MailMessageTypeCard.vue'
import mailMessageTypeClient from 'src/client/mailMessageTypeClient'
import MailMessageType from 'src/models/MailMessageType'

const types = ref<Slice<MailMessageType> | null>(null)

async function loadPage(page: number) {
  types.value = await mailMessageTypeClient.getAllSliced(page, DEFAULT_SLICE_SIZE)
}

loadPage(DEFAULT_SLICE_NUMBER)
</script>

<style scoped></style>
