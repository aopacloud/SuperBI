/**
 * 创建shitf数组
 * @param {*} step
 * @returns
 */
const createShiftArr = (step = 4) => {
  const space = ' '.repeat(step)
  const shift = ['\n'] // array of shifts
  for (let i = 0; i < 100; i++) {
    shift.push(shift[i] + space)
  }
  return shift
}

// 分割sql
const sqlSplit = (str, tab) => {
  return (
    str
      .replace(/\s{1,}/g, ' ')

      .replace(/ AND /gi, '~::~' + tab + tab + 'AND ')
      .replace(/ BETWEEN /gi, '~::~' + tab + 'BETWEEN ')
      .replace(/ CASE /gi, '~::~' + tab + 'CASE ')
      .replace(/ ELSE /gi, '~::~' + tab + 'ELSE ')
      .replace(/ END /gi, '~::~' + tab + 'END ')
      .replace(/ FROM /gi, '~::~FROM ')
      .replace(/ GROUP\s{1,}BY/gi, '~::~GROUP BY ')
      .replace(/ HAVING /gi, '~::~HAVING ')
      //.replace(/ SET /ig," SET~::~")
      .replace(/ IN /gi, ' IN ')

      .replace(/ JOIN /gi, '~::~JOIN ')
      .replace(/ CROSS~::~{1,}JOIN /gi, '~::~CROSS JOIN ')
      .replace(/ INNER~::~{1,}JOIN /gi, '~::~INNER JOIN ')
      .replace(/ LEFT~::~{1,}JOIN /gi, '~::~LEFT JOIN ')
      .replace(/ RIGHT~::~{1,}JOIN /gi, '~::~RIGHT JOIN ')

      .replace(/ ON /gi, '~::~' + tab + 'ON ')
      .replace(/ OR /gi, '~::~' + tab + tab + 'OR ')
      .replace(/ ORDER\s{1,}BY/gi, '~::~ORDER BY ')
      .replace(/ OVER /gi, '~::~' + tab + 'OVER ')

      .replace(/\(\s{0,}SELECT /gi, '~::~(SELECT ')
      .replace(/\)\s{0,}SELECT /gi, ')~::~SELECT ')

      .replace(/ THEN /gi, ' THEN~::~' + tab + '')
      .replace(/ UNION /gi, '~::~UNION~::~')
      .replace(/ USING /gi, '~::~USING ')
      .replace(/ WHEN /gi, '~::~' + tab + 'WHEN ')
      .replace(/ WHERE /gi, '~::~WHERE ')
      .replace(/ WITH /gi, '~::~WITH ')

      //.replace(/\,\s{0,}\(/ig,",~::~( ")
      //.replace(/\,/ig,",~::~"+tab+tab+"")

      .replace(/ ALL /gi, ' ALL ')
      .replace(/ AS /gi, ' AS ')
      .replace(/ ASC /gi, ' ASC ')
      .replace(/ DESC /gi, ' DESC ')
      .replace(/ DISTINCT /gi, ' DISTINCT ')
      .replace(/ EXISTS /gi, ' EXISTS ')
      .replace(/ NOT /gi, ' NOT ')
      .replace(/ NULL /gi, ' NULL ')
      .replace(/ LIKE /gi, ' LIKE ')
      .replace(/\s{0,}SELECT /gi, 'SELECT ')
      .replace(/\s{0,}UPDATE /gi, 'UPDATE ')
      .replace(/ SET /gi, ' SET ')

      .replace(/~::~{1,}/g, '~::~')
      .split('~::~')
  )
}

const isSubquery = (str, parenthesisLevel) => {
  return (
    parenthesisLevel - (str.replace(/\(/g, '').length - str.replace(/\)/g, '').length)
  )
}

/**
 * sql 格式化
 * @param {string} text 文本
 * @param {number} step 空格
 * @returns
 */
export const sqlFormat = (text = '', step = 4) => {
  let ar_by_quote = text
      .replace(/\s{1,}/g, ' ')
      .replace(/\'/gi, "~::~'")
      .split('~::~'),
    len = ar_by_quote.length,
    ar = [],
    deep = 0,
    tab = ' '.repeat(step),
    inComment = true,
    inQuote = false,
    parenthesisLevel = 0,
    str = '',
    shift = createShiftArr(step)

  for (let i = 0; i < len; i++) {
    if (i % 2) {
      ar = ar.concat(ar_by_quote[i])
    } else {
      ar = ar.concat(sqlSplit(ar_by_quote[i], tab))
    }
  }

  len = ar.length
  for (let i = 0; i < len; i++) {
    parenthesisLevel = isSubquery(ar[i], parenthesisLevel)

    if (/\s{0,}\s{0,}SELECT\s{0,}/.exec(ar[i])) {
      ar[i] = ar[i].replace(/\,/g, ',\n' + tab + tab + '')
    }

    if (/\s{0,}\s{0,}SET\s{0,}/.exec(ar[i])) {
      ar[i] = ar[i].replace(/\,/g, ',\n' + tab + tab + '')
    }

    if (/\s{0,}\(\s{0,}SELECT\s{0,}/.exec(ar[i])) {
      deep++
      str += shift[deep] + ar[i]
    } else if (/\'/.exec(ar[i])) {
      if (parenthesisLevel < 1 && deep) {
        deep--
      }
      str += ar[i]
    } else {
      str += shift[deep] + ar[i]
      if (parenthesisLevel < 1 && deep) {
        deep--
      }
    }
  }

  str = str.replace(/^\n{1,}/, '').replace(/\n{1,}/g, '\n')
  return str
}
