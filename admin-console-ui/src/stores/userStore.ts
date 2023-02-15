import { defineStore } from 'pinia'
import { ref } from 'vue'
import User from 'src/models/User'

export const userStore = defineStore('user',  () =>{
  const user = ref<User | null>(null)

  return {
    user,
  }
})
