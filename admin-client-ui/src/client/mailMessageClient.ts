import MailMessage from 'src/models/MailMessage'
import { api } from 'boot/axios'
import Slice from 'src/models/Slice'

async function getAllSliced(page: number, size: number): Promise<Slice<MailMessage>> {
  const { data, status } = await api.get<Slice<MailMessage>>(`/mails?page=${page}&size=${size}`)

  if (status != 200) {
    throw Error('No MailMessageTypes are found')
  }

  return data
}

export default {
  getAllSliced,
}
