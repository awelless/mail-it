import { api } from 'boot/axios'
import User from 'src/models/User'

async function login(username: string, password: string): Promise<LoginResponse> {
  const credentials = {
    username,
    password,
  }

  const { status } = await api.post('/login', credentials, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    validateStatus: (statusCode) => statusCode == 200 || statusCode == 401,
  })

  if (status == 401) {
    return {
      success: false,
      error: 'Bad credentials',
    }
  }

  if (status != 200) {
    throw Error('Error during log in occurred')
  }

  return {
    success: true,
    user: {
      username,
    },
  }
}

export default {
  login,
}

export interface LoginResponse {
  success: boolean
  user?: User
  error?: string
}
