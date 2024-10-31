module.exports = {
  printWidth: 80, //单行长度
  tabWidth: 2, //缩进长度
  useTabs: false, //使用空格代替tab缩进
  singleQuote: true, //使用单引号
  jsxSingleQuote: true, //使用单引号
  semi: false, //句末使用分号;默认值true
  arrowParens: 'avoid', //单参数箭头函数参数周围省略圆括号-eg: (x) => x; 默认值always
  bracketSpacing: true, //在对象前后添加空格-eg: { foo: bar }
  eslintIntegration: false, //不让prettier使用eslint的代码格式进行校验
  insertPragma: false, //在已被preitter格式化的文件顶部加上标注,
  bracketSameLine: false, //多属性html标签的‘>’折行放置
  rangeStart: 0,
  requirePragma: false, //无需顶部注释即可格式化
  trailingComma: 'none' //尾随逗号
}
