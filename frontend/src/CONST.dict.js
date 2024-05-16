/*
 * @Author: huanghe Huang.He@olaola.chat
 * @Date: 2024-04-07 15:37:07
 * @LastEditors: huanghe Huang.He@olaola.chat
 * @LastEditTime: 2024-04-07 17:24:45
 * @FilePath: /dm-BDP-front-feat/src/CONST.dict.js
 */
/* ***************************  */
/* ***************************  */
/*   存放静态隐射字典表, 减少藕合    */
/* ***************************  */
/* ***************************  */

// 同环比配置
export const COMPARE = {
  MODE: {
    ORIGIN: 0, // 原值
    DIFF: 1, // 差值
    DIFF_PERSENT: 2, // 差值百分比
  },
  MERGE: {
    FALSE: false, // 单独显示
    TRUE: true, // 合并显示
  },
}

// 指标、维度
export const CATEGORY = {
  // PROPERTY: 'PROPERTY',
  PROPERTY: 'DIMENSION', //'PROPERTY',
  PROPERTY_COLUMN: 'DIMENSION_COLUMN', //'PROPERTY',
  // INDEX: 'INDEX',
  INDEX: 'MEASURE', //'INDEX',
  FILTER: 'FILTER',
}

// 单、或、且 关系
export const RELATION = {
  EQUAL: 'EQUAL',
  OR: 'OR',
  AND: 'AND',
}
