export default interface MailMessageType {
  id: number
  name: string
  description?: string
  maxRetriesCount?: number
  contentType: MailMessageContentType
  templateEngine?: HtmlTemplateEngine
  template: string
}

export enum MailMessageContentType {
  PLAIN_TEXT = 'PLAIN_TEXT',
  HTML = 'HTML',
}

export enum HtmlTemplateEngine {
  NONE = 'NONE',
  FREEMARKER = 'FREEMARKER',
}
