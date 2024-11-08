import useAppStore from '@/store/modules/app'
import { clearQuerys } from 'common/utils/window'

export default function setEntry(route) {
  const { _o_, _ps_ } = route.query

  if (!_o_ && !_ps_) return

  useAppStore().setEntryInfo({ origin: _o_, pToken: _ps_ })
}

export const clearEntryQuery = route => {
  const { _o_, _ps_ } = route.query

  if (_o_) clearQuerys('_o_')
  if (_ps_) clearQuerys('_ps_')
}
