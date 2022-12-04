<template>
  <q-layout view='hHh Lpr lFf' class='bg-grey-2'>
    <q-header elevated>
      <q-toolbar>
        <router-link style='text-decoration: none' to='/'>
          <q-toolbar-title class='text-white'>
            Mail-it
          </q-toolbar-title>
        </router-link>
      </q-toolbar>
    </q-header>

    <q-drawer show-if-above bordered>
      <q-list>
        <q-item clickable to='/'>
          <q-item-section>
            Mails
          </q-item-section>
        </q-item>
        <q-item clickable to='/types'>
          <q-item-section>
            Mail Types
          </q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <q-page padding>
        <router-view />
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang='ts'></script>
