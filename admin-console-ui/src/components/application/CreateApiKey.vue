<template>
  <q-dialog persistent>
    <q-card style="width: 100%; max-width: 800px">
      <q-card-section class="row items-center">
        <div class="text-h5">Create Api Key</div>
      </q-card-section>

      <div class="q-mx-md q-mb-sm q-gutter-y-md">
        <q-input outlined v-model="name" label="Name"/>
        <q-select outlined v-model="expirationDays" :options="expirationDaysOptions" label="Expiration Days"/>

        <div v-if="token">
          Created ApiKey. Make sure to copy it. You won't be able to see this key again!
          <div>
            <b>{{ token }}</b>
            <q-btn dense class="q-ml-md" :icon="copyTokenIcon" @click="copyToken"/>
          </div>
        </div>
      </div>

      <q-card-actions align="right">
        <q-btn v-if="!token" color="info" @click="create">Create</q-btn>
        <q-btn class="text-black bg-white" v-close-popup>Back</q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { defineProps, ref } from 'vue'
import apiKeyClient from 'src/client/apiKeyClient'
import { name } from 'axios'

const props = defineProps<{
  applicationId: string,
  onCreate: () => void,
}>()

const expirationDaysOptions = [7, 30, 90, 365]

const name = ref('')
const expirationDays = ref(expirationDaysOptions[0])

const token = ref('')
const copyTokenIcon = ref('content_copy')

async function create() {
  const keyName = name.value

  if (!keyName) {
    return
  }

  token.value = await apiKeyClient.create(props.applicationId, keyName, expirationDays.value)

  props.onCreate()

  name.value = ''
  expirationDays.value = expirationDaysOptions[0]
  token.value = ''
  copyTokenIcon.value = 'content_copy'
}

function copyToken() {
  navigator.clipboard.writeText(token.value)
  copyTokenIcon.value = 'library_add_check'
}
</script>
