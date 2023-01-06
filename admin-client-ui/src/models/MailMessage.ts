import IdName from 'src/models/IdName'

export default interface MailMessage {
  id: string
  emailFrom?: string
  emailTo: string
  /**
   * {@link IdName} of {@link MailMessageType}
   */
  type: IdName
  createdAt: Date
  sendingStartedAt?: Date
  sentAt?: Date
  status: MailMessageStatus
  failedCount: number
}

export enum MailMessageStatus {
  PENDING = 'PENDING',
  RETRY = 'RETRY',
  SENDING = 'SENDING',
  SENT = 'SENT',
  FAILED = 'FAILED',
  CANCELED = 'CANCELED',
}
