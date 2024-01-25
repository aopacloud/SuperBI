const modules = import.meta.glob('./assets/images/*.{png,jpg}', { eager: true })

const reg = new RegExp(/\.\/assets\/images\/(.*).(jpg|png)/)
const allImgs = Object.keys(modules).reduce((acc, cur) => {
  const matched = cur.match(reg)

  if (!matched) return acc

  const name = matched[1]
  if (!name) return acc

  acc[name] = modules[cur].default

  return acc
}, {})

export const engines = [
  {
    name: 'ClickHouse',
    engine: 'CLICKHOUSE',
    img: allImgs['clickhouse'],
    icon: allImgs['clickhouse.icon'],
  },
  { name: 'Db2', engine: 'db2', disabled: true, img: allImgs['db2'] },
  { name: 'DM', engine: 'dm', disabled: true, img: allImgs['dm'] },
  { name: 'Impala', engine: 'impala', disabled: true, img: allImgs['imPala'] },
  { name: 'KingBase', engine: 'kingbase', disabled: true, img: allImgs['kingBase'] },
  { name: 'MariaDb', engine: 'mariadb', disabled: true, img: allImgs['mariaDB'] },
  { name: 'MongoDB', engine: 'mongo', disabled: true, img: allImgs['mongo'] },
  { name: 'MySql', engine: 'mysql', disabled: true, img: allImgs['mySql'] },
  { name: 'Oracle', engine: 'oracle', disabled: true, img: allImgs['oracle'] },
  { name: 'PG', engine: 'pg', disabled: true, img: allImgs['pg'] },
  {
    name: 'SqlServer',
    engine: 'sqlserver',
    disabled: true,
    img: allImgs['sqlServer'],
  },
  { name: 'TiDB', engine: 'tidb', disabled: true, img: allImgs['tiDB'] },
  { name: '本地文件上传', engine: 'file', disabled: true, img: allImgs['fileExcel'] },
]
