import { api } from 'boot/axios'
import { ApiKey } from 'src/models/ApiKey'

async function getAll(): Promise<ApiKey[]> {
  const { data } = await api.get<ApiKey[]>('/api-keys')
  return data
}

async function create(name: string, expirationDays: number): Promise<string> {
  const { data, status } = await api.post('/api-keys', { name, expirationDays })

  if (status != 201) {
    throw Error(`Failed to create Api Key: ${JSON.stringify(data)}`)
  }

  return data.token
}

async function deleteApiKey(apiKeyId: string): Promise<void> {
  const { data, status } = await api.delete(`/api-keys/${apiKeyId}`)

  if (status != 204) {
    throw Error(`Failed to delete ApiKey: ${JSON.stringify(data)}`)
  }
}

export default {
  getAll,
  create,
  deleteApiKey,
}
