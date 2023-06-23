<template>
  <q-table
    :rows="apiKeys"
    :columns="columns"
    no-data-label="No Api Keys are created for this App"
    :hide-header="apiKeys.length === 0"
    :hide-bottom="apiKeys.length > 0"
  >
    <template v-slot:top>
      <div class="col-2 q-table__title">Api Keys</div>
      <q-space/>
      <q-btn color="info" @click="showCreate = true">
        Create
      </q-btn>
    </template>
  </q-table>

  <CreateApiKey v-model="showCreate" :application-id="applicationId" :load-all-function="load"/>
</template>

<script setup lang="ts">
import { defineProps, ref } from 'vue'
import { ApiKey } from 'src/models/ApiKey'
import apiKeyClient from 'src/client/apiKeyClient'
import CreateApiKey from 'components/application/CreateApiKey.vue'

const props = defineProps<{
  applicationId: string,
}>()

const columns = [
  { name: 'name', label: 'Name', field: 'name' },
  { name: 'createdAt', label: 'Created', field: (row: ApiKey) => row.createdAt.toString() },
  { name: 'expiresAt', label: 'Expires', field: (row: ApiKey) => row.expiresAt.toString() },
]
const apiKeys = ref<ApiKey[]>([])

const showCreate = ref(false)

async function load() {
  apiKeys.value = await apiKeyClient.getAll(props.applicationId)
}

load()
</script>
