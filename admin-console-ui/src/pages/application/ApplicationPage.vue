<template>
  <q-spinner v-if="!application" color="primary" size="3em" />
  <div v-else class="q-gutter-md">
    <p class="text-h4 text-primary text-bold">{{ application.name }} #{{ application.id }}</p>
    <p class="text-body1">State: {{ application.state }}</p>

    <q-dialog v-model="showDelete" persistent>
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm">
            You're about to delete Application type: {{ application.name }}.
          </span>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn class="text-black bg-white" v-close-popup>Back</q-btn>
          <q-btn color="negative" @click="deleteApp">Delete</q-btn>
        </q-card-actions>
      </q-card>
    </q-dialog>

    <ApiKeys :application-id="application.id" />

    <div class="row">
      <div class="col-md-4 q-gutter-sm">
        <q-btn to="/applications" class="text-black bg-white" style="width: 20%">Back</q-btn>
        <q-btn color="negative" @click="showDelete = true" style="width: 20%">Delete</q-btn>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Application } from 'src/models/Application'
import applicationClient from 'src/client/applicationClient'
import ApiKeys from 'components/application/ApiKeys.vue'

const router = useRouter()

const props = defineProps<{
  id: string
}>()

const application = ref<Application | null>(null)
const showDelete = ref(false)

async function load() {
  application.value = await applicationClient.getById(props.id)
}

async function deleteApp() {
  if (!application.value) {
    return
  }

  await applicationClient.deleteApp(application.value.id)
  await router.push('/types')
}

load()
</script>

<style scoped></style>
