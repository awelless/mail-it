import { api } from 'boot/axios'
import User from 'src/models/User'

async function getCurrentUser(): Promise<GetCurrentUserResponse> {
  const { data, status } = await api.get('/users/me', {
    validateStatus: (statusCode) => statusCode == 200 || statusCode == 401,
  })

  if (status == 401) {
    return {
      success: false,
    }
  }

  if (status != 200) {
    throw Error('Error during log in occurred')
  }

  return {
    success: true,
    user: data,
  }
}

export default {
  getCurrentUser,
}

export interface GetCurrentUserResponse {
  success: boolean
  user?: User
}
