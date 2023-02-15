import { api } from 'boot/axios'
import User from 'src/models/User'

async function login(username: string, password: string): Promise<LoginResponse> {
  const { data, status } = await api.post('/login', null, {
    params: {
      username,
      password,
    },
    validateStatus: statusCode => statusCode == 200 || statusCode == 401,
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
    }
  }
}

export default {
  login,
}

export interface LoginResponse {
  success: boolean,
  user?: User,
  error?: string,
}
