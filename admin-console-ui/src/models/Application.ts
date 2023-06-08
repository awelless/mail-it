export interface Application {
  id: string
  name: string
  state: ApplicationState
}

export enum ApplicationState {
  ENABLED = 'ENABLED',
  DELETED = 'DELETED',
}
