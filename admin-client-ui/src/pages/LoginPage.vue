<template>
  <div class='row'>
    <div class='col-md-4 fixed-center'>
      <q-form class='q-gutter-sm' @submit='login()'>
        <q-input outlined v-model='username' label='Username' />
        <q-input outlined v-model='password' label='Password' type='password' />
        <q-btn type='submit' color='info' style='width: 20%'>Log in</q-btn>
      </q-form>
    </div>
  </div>
</template>

<script setup lang='ts'>
import { ref } from 'vue'
import authClient from 'src/client/authClient'
import { userStore } from 'stores/userStore'
import { useRouter } from 'vue-router'
import { api } from 'boot/axios'
import { useQuasar } from 'quasar'

const router = useRouter()
const quasar = useQuasar()

const user = userStore()

const username = ref<string>('')
const password = ref<string>('')

async function login() {
  const loginResult = await authClient.login(username.value, password.value)

  if (loginResult.success) {
    user.$patch({ user: loginResult.user })

    api.defaults.auth = {
      username: username.value,
      password: password.value,
    }

    await router.push('/')
  } else {
    quasar.notify({
      type: 'negative',
      message: loginResult.error,
    })
  }
}
</script>

<style scoped>

</style>
