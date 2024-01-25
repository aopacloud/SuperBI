class Emittor {
  static instance = null

  constructor() {
    this.pending = {}

    if (!Emittor.instance) {
      Emittor.instance = this
    }
    return Emittor.instance
  }

  emit(eventName, ...args) {
    const pend = this.pending[eventName]

    if (!pend) return

    for (const key of pend.keys()) {
      const cbs = pend.get(key)

      cbs.forEach(cb => cb(...args))
    }
  }

  on(eventName, cb) {
    let pend = this.pending[eventName]

    if (!pend) {
      pend = new Map()

      pend.set(cb, [cb])
    } else {
      if (!pend.has(cb)) {
        pend.set(cb, [cb])
      } else {
        const cbs = pend.get(cb)

        cbs.push(cb)
        pend.set(cb, cbs)
      }
    }

    this.pending[eventName] = pend
  }

  off(eventName, cb) {
    const pend = this.pending[eventName]

    if (!pend) return

    if (!cb) {
      delete this.pending[eventName]

      return
    }

    pend.delete(cb)
  }
}

export default new Emittor()
