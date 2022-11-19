export default interface Slice<T> {
  content: Array<T>
  page: number
  size: number
}

export const DEFAULT_SLICE_NUMBER = 1
export const DEFAULT_SLICE_SIZE = 10
