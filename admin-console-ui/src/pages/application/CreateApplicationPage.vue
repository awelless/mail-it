<template>
  <p class="text-h4 text-primary text-bold q-pb-md">Create new Application</p>
  <div class="q-gutter-md">
    <q-input outlined v-model="name" label="Name" />

    <div class="row">
      <div class="col-md-2 q-gutter-sm">
        <q-btn @click="create" color="info" style="width: 40%">Create</q-btn>
        <q-btn to="/applications" class="text-black bg-white" style="width: 40%">Back</q-btn>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import applicationClient from 'src/client/applicationClient'
import { ref } from 'vue'

const router = useRouter()

const name = ref('')

async function create() {
  const createdApp = await applicationClient.create(name.value)
  await router.push(`/applications/${createdApp.id}`)
}
</script>

<style scoped></style>
