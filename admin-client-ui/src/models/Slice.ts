export default interface Slice<T> {
  content: Array<T>
  page: number
  size: number
  last: boolean
}

export const DEFAULT_SLICE_NUMBER = 1
export const DEFAULT_SLICE_SIZE = 10
