import compression from 'vite-plugin-compression'

export default function createCompression() {
  // https://github.com/vbenjs/vite-plugin-compression
  return compression({
    verbose: false,
    algorithm: 'gzip',
    ext: '.gz',
    threshold: 1024 * 20,
  })
}
