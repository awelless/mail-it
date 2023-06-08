<template>
  <q-layout view="hHh Lpr lFf" class="bg-grey-2">
    <q-header elevated>
      <q-toolbar>
        <router-link style="text-decoration: none" to="/">
          <q-toolbar-title class="text-white"> Mail-it </q-toolbar-title>
        </router-link>

        <q-space />

        <template v-if="user.user">
          <div class="q-mr-sm text-subtitle1">{{ user.user.username }}</div>
          <!-- todo should be icon-->
          <q-btn flat @click="logout">logout</q-btn>
        </template>
      </q-toolbar>
    </q-header>

    <q-drawer v-if="user.user" show-if-above bordered>
      <q-list>
        <q-item clickable to="/">
          <q-item-section> Mails </q-item-section>
        </q-item>
        <q-item clickable to="/types">
          <q-item-section> Mail Types </q-item-section>
        </q-item>
        <q-item clickable to="/applications">
          <q-item-section> Applications </q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <q-page padding>
        <div v-if="loaded">
          <router-view v-if="user.user" />
          <LoginPage v-else />
        </div>
        <q-spinner v-else color="primary" size="3em" />
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { userStore } from 'stores/userStore'
import LoginPage from 'pages/LoginPage.vue'
import userClient from 'src/client/userClient'
import { ref } from 'vue'
import cookies from 'src/cookies/cookies'

const loaded = ref(false)

const user = userStore()

async function tryLoadUser() {
  const response = await userClient.getCurrentUser()
  if (response.success) {
    user.$patch({ user: response.user })
  }

  loaded.value = true
}

tryLoadUser()

function logout() {
  cookies.deleteByName('SESSION')
  user.$patch({ user: null })
}
</script>
