﻿## 数据集编辑页面

- 编辑时，需要将源表中的字段合并进数据集详情中，此时逻辑顺序与接口请求时机易导致重复的请求，是否有可优化方案？？

> 现在是根据数据集信息中的 config 字段判断是否是初始化，有则是，无则否。初始化则请求时 携带 trigger: INIT | CHANGE, 传递到 table 中处理
