import { api } from 'boot/axios'
import Slice from 'src/models/Slice'
import { Application } from 'src/models/Application'

async function getById(id: string): Promise<Application> {
  const { data, status } = await api.get<Application>(`/applications/${id}`)

  if (status != 200) {
    throw Error(`No Application with id: ${id} is found`)
  }

  return data
}

async function getAllSliced(page: number, size: number): Promise<Slice<Application>> {
  const { data, status } = await api.get<Slice<Application>>(`/applications?page=${page}&size=${size}`)

  if (status != 200) {
    throw Error('No Applications are found')
  }

  return data
}

async function create(name: string): Promise<Application> {
  const { data, status } = await api.post('/applications', { name })

  if (status != 201) {
    throw Error(`Failed to create Application: ${JSON.stringify(data)}`)
  }

  return data
}

async function deleteApp(id: string): Promise<void> {
  const { data, status } = await api.delete(`/applications/${id}`)

  if (status != 202) {
    throw Error(`Failed to delete Application: ${JSON.stringify(data)}`)
  }
}

export default {
  getById,
  getAllSliced,
  create,
  deleteApp,
}
