import { api } from 'boot/axios'
import { ApiKey } from 'src/models/ApiKey'

async function getAll(applicationId: string): Promise<ApiKey[]> {
  const { data } = await api.get<ApiKey[]>(`/applications/${applicationId}/api-keys`)
  return data
}

async function create(applicationId: string, name: string, expirationDays: number): Promise<string> {
  const { data, status } = await api.post(`/applications/${applicationId}/api-keys`, { name, expirationDays })

  if (status != 201) {
    throw Error(`Failed to create Api Key: ${JSON.stringify(data)}`)
  }

  return data.token
}

async function deleteApiKey(applicationId: string, apiKeyId: string): Promise<void> {
  const { data, status } = await api.delete(`/applications/${applicationId}/api-keys/${apiKeyId}`)

  if (status != 204) {
    throw Error(`Failed to delete Application: ${JSON.stringify(data)}`)
  }
}

export default {
  getAll,
  create,
  deleteApiKey,
}
