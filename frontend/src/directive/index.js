import hasPermission from './permission'

export default function directive(app) {
  app.directive('permission', hasPermission)
}
