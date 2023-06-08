<template>
  <div class="row">
    <div class="col-md-1">
      <q-btn to="/applications/create" color="info" style="width: 80%">Create</q-btn>
    </div>
  </div>
  <div class="q-pt-md">
    <q-spinner v-if="!applications" color="primary" size="3em"/>
    <div v-else class="q-gutter-md q-pt-md">
      <div v-for="application in applications.content" :key="application.id">
        <ApplicationCard :application="application"/>
      </div>
      <SliceFooter :slice="applications" :fetch-function="loadPage"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Slice, { DEFAULT_SLICE_NUMBER, DEFAULT_SLICE_SIZE } from 'src/models/Slice'
import SliceFooter from 'components/SliceFooter.vue'
import { Application } from 'src/models/Application'
import applicationClient from 'src/client/applicationClient'
import ApplicationCard from 'components/application/ApplicationCard.vue'

const applications = ref<Slice<Application> | null>(null)

async function loadPage(page: number) {
  applications.value = await applicationClient.getAllSliced(page, DEFAULT_SLICE_SIZE)
}

loadPage(DEFAULT_SLICE_NUMBER)
</script>
