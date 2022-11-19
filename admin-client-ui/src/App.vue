<template>
  <q-layout view="hHh Lpr lFf" class="bg-grey-2">
    <q-header elevated>
      <q-toolbar>
        <q-toolbar-title> Mail-it </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-drawer show-if-above bordered>
      <q-list> </q-list>
    </q-drawer>

    <router-view />
  </q-layout>
</template>

<script setup lang="ts"></script>
