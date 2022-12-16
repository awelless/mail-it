import MailMessageType from 'src/models/MailMessageType'
import { api } from 'boot/axios'
import Slice from 'src/models/Slice'

async function getById(id: number): Promise<MailMessageType> {
  const { data, status } = await api.get<MailMessageType>(`/mails/types/${id}`)

  if (status != 200) {
    throw Error(`No MailMessageType with id: ${id} is found`)
  }

  return data
}

async function getAllSliced(page: number, size: number): Promise<Slice<MailMessageType>> {
  const { data, status } = await api.get<Slice<MailMessageType>>(`/mails/types?page=${page}&size=${size}`)

  if (status != 200) {
    throw Error('No MailMessageTypes are found')
  }

  return data
}

async function create(type: MailMessageType): Promise<MailMessageType> {
  const { data, status } = await api.post('/mails/types', type)

  if (status != 201) {
    throw Error(`Failed to create MailMessageType: ${JSON.stringify(data)}`)
  }

  return data
}

async function update(type: MailMessageType): Promise<void> {
  const { data, status } = await api.put(`/mails/types/${type.id}`, type)

  if (status != 200) {
    throw Error(`Failed to update MailMessageType: ${JSON.stringify(data)}`)
  }
}

export default {
  getById,
  getAllSliced,
  create,
  update,
}
