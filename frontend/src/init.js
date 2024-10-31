import { shareUsers } from '@/store/modules/shared'

export default function useInit() {
  shareUsers().get()
}
