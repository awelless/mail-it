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
    <template v-slot:body-cell-actions="props">
      <q-td :props="props">
        <div>
          <q-btn color="negative" dense icon="delete" @click="initiateDeletion(props.row)"></q-btn>
        </div>
      </q-td>
    </template>
  </q-table>

  <q-dialog v-model="showDelete" persistent>
    <q-card>
      <q-card-section class="row items-center">
          <span class="q-ml-sm">
            You're about to delete Api Key: {{ deletedApiKey.name }}.
          </span>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn class="text-black bg-white" v-close-popup>Back</q-btn>
        <q-btn color="negative" @click="deleteKey">Delete</q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>

  <CreateApiKey v-model="showCreate" :application-id="applicationId" :on-create="load"/>
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
  { name: 'createdAt', label: 'Created', field: (row: ApiKey) => formatDate(row.createdAt) },
  { name: 'expiresAt', label: 'Expires', field: (row: ApiKey) => formatDate(row.expiresAt) },
  { name: 'actions', label: 'Actions' },
]
const apiKeys = ref<ApiKey[]>([])

const showCreate = ref(false)
const showDelete = ref(false)

const deletedApiKey = ref<ApiKey | null>(null)

load()

async function load() {
  apiKeys.value = await apiKeyClient.getAll(props.applicationId)
}

function initiateDeletion(apiKey: ApiKey) {
  deletedApiKey.value = apiKey
  showDelete.value = true
}

async function deleteKey() {
  const apiKey = deletedApiKey.value

  if (!apiKey) {
    return
  }

  await apiKeyClient.deleteApiKey(props.applicationId, apiKey.id)
  await load()

  showDelete.value = false
  deletedApiKey.value = null
}

function formatDate(date: Date): string {
  return date.toLocaleString()
}
</script>
