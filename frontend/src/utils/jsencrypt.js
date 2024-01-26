import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'

// 密钥对生成 http://web.chacuo.net/netrsakeypair

const publicKey = `
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoVfX8Vp81KukQMMzFOGE
QMgGFlKi3W8hUxXut1KjKDqoERallaocyUFQMzkcev/zNgvF0zoYLGxopg7/nZDZ
RtPCjsFekQTOxCBN/tHrwJQtlDvS7GzyX++SDQql9NuZEPvZqw5iSxkZyLM0ZG2F
1C9kb9wRpINywAhMsZgslbfNPFYQRyXHWdjlbdm+n/GhRsXGwV384Lm1Kw4hEVfn
YR4+PkkWKNp5zj1MyE+MbcsM63CDlOO+lsLs57HIRJdo8vNaawvsPkm94qiLpw1N
S+qf0PX7Wk9p3z79YzGF0/CPSaT/vrrzf6ARbuzPa000iiaWSXtkl9L4aiOGPQD+
KQIDAQAB
`

const privateKey = `
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQChV9fxWnzUq6RA
wzMU4YRAyAYWUqLdbyFTFe63UqMoOqgRFqWVqhzJQVAzORx6//M2C8XTOhgsbGim
Dv+dkNlG08KOwV6RBM7EIE3+0evAlC2UO9LsbPJf75INCqX025kQ+9mrDmJLGRnI
szRkbYXUL2Rv3BGkg3LACEyxmCyVt808VhBHJcdZ2OVt2b6f8aFGxcbBXfzgubUr
DiERV+dhHj4+SRYo2nnOPUzIT4xtywzrcIOU476WwuznschEl2jy81prC+w+Sb3i
qIunDU1L6p/Q9ftaT2nfPv1jMYXT8I9JpP++uvN/oBFu7M9rTTSKJpZJe2SX0vhq
I4Y9AP4pAgMBAAECggEAA8G933rR6UnN20wwRk4x/CgsLBzkee96gSrDkjU0Y11q
SGuUk3WQkgcZTPgJd7u5+AoyIwb5RR+JGETFIvAafxGzOAu8nffUsLBn4IUWzjcl
EIyMDsFCq4Uyi2cyEg+C8uhKWzdMhp8hAJFbqPL76MLpvckMCB6fb2VCE+iNMTc6
8ZhfQADLiSMKMy2EhFXU8l0Orbg/D6jL+E1jbweppQjAADhxbQkOsBKBTijx8cDg
EpWH+GXwzBFcjHJWEZjJzvYPU8PSSVoZeWk6soGF0lcK575N3t8aw/eUkbzhS7jG
RReQ5a2IDxLrcrUPBjD/hUkXWJvQLAvK9b/ThRBa4QKBgQDS3i+kdCyk910CRdod
tDoeNQoE7voTan/S5uxeMUpJMyNOaH+M2SfEE4vib9kYVaMW+ZfCrXO0e9+n+N0P
EKBqHuEZFGL+cLwk/3rCNCBufvgbKafBFepnB4o7ga7IEiWQ0NBS128URazAPyto
KlOHOW1urM/wZKuQs3oAB8iedQKBgQDD4Bw2v9dTC9LlzcqrAs8eYZYBGrT56wKV
61PMMxPhKo8P4cqaLLkhk5Km9nMUsK3ciTtNf3EuVus34UMFtlZh2SBMcKM3a4Rm
Tm0ue/KE56TOAAPOtEs8tRb5Ww98OuctzWij+Kvfy7yKSYFdUGfGbntQQo536gRX
r3wENpVSZQKBgEWV8sznVZA+3R+R44+n2QH86MiGAFqF65ayg8/1nhkX2g/W5jiZ
tMi81o2r8U1ZfU9ooE8rJCKsOjtZIcIF75n1hLzdAGoM09m1wVHzF7y/A6sbDPPo
AolrcI+AlSgK7QCkGAsucsaS05ZBglaGmTfnmJhMSPaAkilXQOxfpTG9AoGBAJhN
U+LGPBkYon2JDEL4Ri+rVUW6jGme8D2940RtmtGDlAWXXT+P8stGwcsM6eJ2llla
Y0AZDdooP3ENuF1ir7wZZeHJ2z8mXKZn6MEyIxpwO1bp8AvzaGTlsd1ljp7b9hFJ
HEoS+yFie1FTYkE6WdaS5VM8zSY0UfgUPOvRvT15AoGANjgaKpv/6WHKfLLszuOd
nPkUerN6kK3RqUUk3IXQaTsFr0s48NZESrPR92S2rCr0MDta5410FdQKYS0CeP/w
3qx9/krb5wLGywZ977eQCTEbu16rMpfYKZoGUmL92eCpR/epj0VaV7d91WS6TKos
KnhfX9Y0oQUmN6lV5Eq9kWE=
`

// 加密
export function encrypt(txt) {
  let encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey) // 设置公钥

  const res = encryptor.encrypt(txt) // 对数据进行加密
  encryptor = null
  return res
}

// 解密
export function decrypt(txt) {
  let encryptor = new JSEncrypt()
  encryptor.setPrivateKey(privateKey) // 设置私钥

  const res = encryptor.decrypt(txt) // 对数据进行解密
  encryptor = null
  return res
}
