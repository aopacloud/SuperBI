<template>
  <a-modal
    width="100%"
    wrap-class-name="fullscreen-modal"
    :keyboard="false"
    :title="title"
    :open="open"
    @cancel="onCancel"
  >
    <template #footer>
      <div class="footer" style="text-align: center">
        <a-button
          v-if="stepValue > 0"
          :disabled="stepValue === 0 || submitLoading"
          @click="prevStep"
        >
          上一步
        </a-button>
        <a-button
          type="primary"
          :loading="uploadLoading || submitLoading"
          :disabled="!previewSuccess"
          @click="nextStep"
        >
          {{ nextStepText }}
        </a-button>
      </div>
    </template>

    <div class="wrapper">
      <a-steps
        class="steps"
        style="width: 800px; margin: 80px auto"
        label-placement="vertical"
        :items="steps"
        :current="stepValue"
      />

      <keep-alive>
        <section v-if="stepValue === 0" class="content flex-column">
          <a-spin
            :spinning="uploadLoading || previewLoading"
            tip="文件正在上传中..."
          >
            <div class="upload-box">
              <input
                ref="inputRef"
                type="file"
                class="upload-input"
                accept=".xls, .xlsx"
                @change="onFileChange"
              />
              <div class="upload-area">
                <img class="upload-img" :src="uploadImg" alt="" />

                <template v-if="isSuccess">
                  <p>
                    <span style="color: #70b603">方案上传成功</span>
                    点击可重新上传
                  </p>
                  <p class="font-help2" style="margin-top: 6px">
                    以下是预览数据
                  </p>
                </template>

                <template v-else-if="isError">
                  <p>
                    <span style="color: red">文件上传失败</span> 点击重新上传
                  </p>
                </template>

                <template v-else>
                  <p>点击上传本地文件</p>
                  <p class="font-help2" style="margin-top: 6px">
                    仅支持 Excel 格式
                  </p>
                </template>
              </div>
            </div>
          </a-spin>

          <p v-if="isReady" class="text-center">
            您可以参考
            <b>
              <a
                :href="uploadTemplateUrl"
                download="模板-多维本地文件上传.xlsx"
              >
                文件上传模板
              </a>
            </b>
            或者前往
            <b><a target="_blank" :href="uploadNormUrl">上传规范</a></b>
            进行查看
          </p>

          <p v-if="isError" class="text-center" style="color: red">
            {{ uploadInfo.message }}
          </p>

          <a-spin :spinning="previewLoading" style="margin-top: 150px">
            <PreviewTable
              v-if="previewLoaded"
              class="flex-1 scroll"
              style="width: 1200px; margin: 15px auto 0"
              :columns="previewColumns"
              :data="previewData"
            />
          </a-spin>
        </section>

        <EditTable
          v-else-if="stepValue === 1"
          ref="editTableRef"
          class="content"
          style="width: 1200px; margin: 0 auto"
          :loading="loading"
          :previewFields="previewColumns"
        />

        <SaveInfo
          v-else-if="stepValue === 2"
          class="content"
          ref="saveInfoRef"
          :isFromOutApp="isFromOutApp"
          :detail="detailInfo"
        />
      </keep-alive>
    </div>
  </a-modal>
</template>

<script setup lang="jsx">
import { ref, computed, useTemplateRef } from 'vue'
import { Modal, message } from 'ant-design-vue'
import PreviewTable from './components/PreviewTable.vue'
import EditTable from './components/EditTable.vue'
import SaveInfo from './components/SaveInfo.vue'
import { useRoute, useRouter } from 'vue-router'
import {
  upload,
  uploadChunk,
  uploadFromInquiry,
  getPreview,
  createCK
} from '@/apis/dataset/upload'
import {
  getLastVersionById,
  postDataset,
  putDataset,
  postPublishById
} from '@/apis/dataset'
import { getFieldsByTableItem } from '@/apis/metaData'
import { moveDirectory } from '@/apis/directory'
import {
  steps,
  uploadStatusMap,
  uploadReadyImg as imgReady,
  uploadSuccessImg as imgSuccess,
  uploadErrorImg as imgError,
  uploadNormUrl,
  uploadTemplateUrl
} from './config'
import { getWordWidth } from '@/common/utils/string'
import { fileToChunks, file2Buffer } from '@/common/utils/file'
import SparkMD5 from 'spark-md5'

const _getWordWidth = str => getWordWidth(str, 10, 20) + 16

const open = defineModel('open', { type: Boolean, default: true })

const { mode, id: datasetId } = defineProps({
  mode: {
    type: String,
    default: 'CREATE',
    validator: s => ['CREATE', 'EDIT'].includes(s)
  },
  id: {
    type: [String, Number]
  }
})

const emits = defineEmits(['ok'])

const route = useRoute()
const router = useRouter()

const title = computed(() => (mode === 'EDIT' ? '编辑数据集' : '本地文件上传'))

const reset = () => {
  detailInfo.value = {}
  uploadStatus.value = undefined
  previewLoaded.value = false
  previewColumns.value = []
  previewData.value = []
}

const onCancel = () => {
  Modal.confirm({
    title: '提示',
    content: '关闭弹窗操作将不会保存',
    onOk() {
      if (route.name === 'AnalysisUpload') {
        router.replace('/')
      } else {
        open.value = false
      }
    }
  })
}

// 外部应用分析
const isFromOutApp = computed(() => !!route.query.fromApp)

// 数据集详情
const loading = ref(false)
const detailInfo = ref({})
const fetchDetail = async id => {
  try {
    loading.value = true
    const res = await getLastVersionById(id)
    previewSuccess.value = true
    detailInfo.value = res
    previewColumns.value = res.fields
  } catch (error) {
    console.error('获取数据集详情失败', error)
  } finally {
    loading.value = false
  }
}

// 步骤条
const stepValue = ref(+(mode === 'EDIT'))
const nextStepText = computed(() => {
  if (stepValue.value === steps.length - 1) {
    return mode === 'EDIT' ? '保存' : '开始分析'
  } else {
    return '下一步'
  }
})

const saveInfoRef = useTemplateRef('saveInfoRef')
const editTableRef = useTemplateRef('editTableRef')

const prevStep = () => {
  if (stepValue.value === 0) return

  stepValue.value--
}
const nextStep = async () => {
  const step = stepValue.value

  // 最后一步
  if (step === steps.length - 1) {
    const info = await saveInfoRef.value.validate()
    if (!info) return

    detailInfo.value = {
      ...detailInfo.value,
      ...info
    }

    submitSave()
  } else {
    // 第一步：上传文件
    if (step === 0) {
      if (!isUploadDone.value && !previewSuccess.value) {
        message.info('请先上传方案文件')
        return
      }

      stepValue.value++
    } else if (step === 1) {
      // 第二部：字段设置
      const fields = await editTableRef.value.validate()
      if (!fields) return

      detailInfo.value.fields = fields

      // 上传成功或预览成功
      // 上传成功（编辑的上传）
      // 预览成功（上传后的预览或从其他app分析过来的预览成功）
      if (isSuccess.value || previewLoaded.value) {
        createAndValidCkTable().then(() => {
          stepValue.value++
        })
      } else {
        stepValue.value++
      }
    }
  }
}

const submitLoading = ref(false)

const ckConfig = shallowRef({})

/**
 * 创建并验证CK表格
 * @returns {Promise<any>}
 */
const createAndValidCkTable = async () => {
  let rowIndex = undefined
  try {
    submitLoading.value = true
    editTableRef.value.resetValidate()

    const { uploadPath, fields = [] } = detailInfo.value
    const res = await createCK({ path: uploadPath }, fields)

    ckConfig.value = res
    return Promise.resolve(res)
  } catch (e) {
    rowIndex = e.message.match(/\s(\d+)\s/)?.[1]
    return Promise.reject(e)
  } finally {
    editTableRef.value.validateRow(rowIndex)
    submitLoading.value = false
  }
}

// 获取底表源字段
const getOriginFields = config => {
  const { dbName, tableName, engine = 'CLICKHOUSE' } = config
  const payload = {
    engine,
    datasourceName: dbName,
    dbName,
    tableName,
    workspaceId: detailInfo.value.workspaceId
  }

  return getFieldsByTableItem(payload)
}
// 保存
const toSave = async config => {
  const payload = { ...detailInfo.value, config }
  delete payload.uploadPath

  const res =
    mode === 'EDIT'
      ? await putDataset(detailInfo.value.id, payload)
      : await postDataset({ ...payload, upload: true })

  // 如果改动了位置
  if (detailInfo.value.folderId !== res.folderId) {
    moveDirectory({
      position: 'DATASET',
      type: 'ALL',
      workspaceId: detailInfo.value.workspaceId,
      targetId: res.id,
      folderId: detailInfo.value.folderId
    })
  }

  return res
}

const submitSave = async () => {
  try {
    submitLoading.value = true
    let config = detailInfo.value.config

    // 创建 或 上传成功（编辑的上传）
    if (mode === 'CREATE' || isSuccess.value) {
      // 获取底表源信息
      const { fields: originFields, ...res } = await getOriginFields(
        ckConfig.value
      )

      // 构建创建数据集参数的 config
      config = {
        engine: res.engine,
        content: {
          tables: [res]
        }
      }

      detailInfo.value.fields = detailInfo.value.fields.map(item => {
        const oField = originFields.find(t => t.name === item.name)
        return {
          ...item,
          databaseDataType: oField.databaseDataType
        }
      })
    }
    // 保存
    const { id } = await toSave(config)
    // 发布
    await postPublishById(id)

    message.success('保存成功')

    if (route.name === 'AnalysisUpload') {
      router.replace({ name: 'DatasetAnalysis', params: { id } })
    } else {
      open.value = false
      emits('ok', id)
    }
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    submitLoading.value = false
  }
}

// 上传状态
const uploadStatus = ref()
const uploadInfo = reactive({ status: uploadStatus, message: '' })
const isReady = computed(() => !uploadStatus.value)
const isSuccess = computed(() => uploadStatus.value === uploadStatusMap.SUCCESS) // 上传成功
const isError = computed(() => uploadStatus.value === uploadStatusMap.ERROR) // 上传失败
const isUploadDone = computed(
  () =>
    uploadStatus.value === uploadStatusMap.SUCCESS ||
    uploadStatus.value === uploadStatusMap.DONE
) //  上传完成

const uploadImg = computed(() =>
  isReady.value ? imgReady : isSuccess.value ? imgSuccess : imgError
)

// 从查询平台分析过来
const fetchPreviewFromOutApp = async () => {
  const { queryId, runId } = route.query
  if (!queryId || !runId) {
    message.error('缺少必要参数')
    return
  }

  try {
    previewLoading.value = true

    const path = await uploadFromInquiry({ queryId, runId })
    fetchPreview(path)
  } catch (error) {
    console.error('获取预览数据失败', error)
  } finally {
    previewLoading.value = false
  }
}

const init = () => {
  stepValue.value = +(mode === 'EDIT')
}

onMounted(() => {
  if (isFromOutApp.value) {
    fetchPreviewFromOutApp()
  }
})
watch(open, v => {
  if (v) {
    init()
    if (mode === 'EDIT' && datasetId) {
      fetchDetail(datasetId)
    }
  } else {
    reset()
  }
})

/**
 * 验证文件
 *
 * @param {File} file 文件对象
 * @returns 返回布尔值，表示文件大小是否满足限制条件
 */
const validateFile = file => {
  const { size } = file

  const MAX_SIZE_MB = 20
  // 最大文件大小不能超过 10MB
  const maxSize = 1024 * 1024 * MAX_SIZE_MB

  if (size > maxSize) {
    message.warn(`文件大小不能超过 ${MAX_SIZE_MB}MB`)
    return false
  }

  return true
}

const inputRef = useTemplateRef('inputRef')
const onFileChange = e => {
  const file = e.target.files[0]

  if (!file) return

  if (!validateFile(file)) {
    e.target.value = null
    return
  }

  const chunks = fileToChunks(file, 1024 * 1024 * 1)
  uploadChunks(chunks, file)
}

const uploadChunks = async (chunks, file) => {
  // 使用md5对每个分片进行处理
  const createChunksByMd5 = async () => {
    // // 文件的 md5
    const fileBuffer = await file2Buffer(file)
    const spark = new SparkMD5.ArrayBuffer()
    spark.append(fileBuffer)
    const fileMd5 = spark.end()

    const chunkBuffers = []

    for (let i = 0; i < chunks.length; i++) {
      const chunkFile = chunks[i]
      const chunkBuffer = await file2Buffer(chunkFile)
      // 分片的 md5
      const chunkSpark = new SparkMD5.ArrayBuffer()
      chunkSpark.append(chunkBuffer)
      const chunkMd5 = chunkSpark.end()

      const chunk = {
        status: 'READY',
        chunk: i,
        chunkFile,
        chunkMd5,
        file: {
          fileMd5: fileMd5,
          fileSize: file.size,
          filename: file.name
        }
      }

      chunkBuffers.push(chunk)
    }

    return chunkBuffers
  }

  const chunksByMd5 = await createChunksByMd5()

  submitUpload(chunksByMd5)
}

// 上传
const submitUpload = chunks => {
  const toUploadChunkItem = (chunkItem, index) => {
    const {
      chunk,
      chunkFile,
      chunkMd5,
      file: { fileMd5, fileSize, filename }
    } = chunkItem

    const formD = new FormData()
    formD.append('chunkFile', chunkFile)

    // 上传分片
    return uploadChunk(
      {
        chunk,
        chunkMd5,
        'file.fileMd5': fileMd5,
        'file.fileSize': fileSize,
        'file.filename': filename
      },
      formD
    )
      .then(() => Promise.resolve(true))
      .catch(() => Promise.reject(false))
  }

  try {
    uploadLoading.value = true
    previewLoaded.value = false
    Promise.all(chunks.map(toUploadChunkItem))
      .then(async r => {
        const { fileMd5, fileSize, filename } = chunks[0].file

        // 上传
        return upload({ fileMd5, fileSize, filename })
      })
      .then(r => {
        message.success('上传成功')
        uploadStatus.value = uploadStatusMap.SUCCESS
        // 预览
        fetchPreview(r)
      })
      .catch(error => {
        console.error('分片上传失败', error)
        uploadInfo.message = error.message
        uploadStatus.value = uploadStatusMap.ERROR
        previewLoaded.value = false
        previewSuccess.value = false
      })
      .finally(e => {
        uploadLoading.value = false
        inputRef.value.value = null
      })
  } catch (error) {
    console.error('分片上传失败', error)
  }
}

const uploadLoading = ref(false)
// 数据预览
const previewLoading = ref(false)
const previewColumns = shallowRef([])
const previewData = shallowRef([])
// 预览数据是否加载完
const previewLoaded = ref(false)
// 预览数据是否加载成功 - 行列校验成功
const previewSuccess = ref(false)

const transformColumns = ({ data = [], columns = [] }) => {
  return columns.map((col, colIndex) => {
    const dataCols = data.map(row => row[colIndex])
    // 'c1.c1' => name.displayName
    const [name, title] = col.name.split('.')
    const width = Math.max(_getWordWidth(title), ...dataCols.map(_getWordWidth))

    return {
      ...col,
      name,
      title,
      dataIndex: name,
      width
    }
  })
}
const transformList = ({ data = [], columns = [] }) => {
  let errorMsg
  const result = data.map(row => {
    // 行的列数与列个数不一致
    if (row.length !== columns.length) {
      errorMsg = '预览数据异常，请联系管理员'
      return {}
    }

    return row.reduce((a, b, i) => {
      const col = columns[i]
      a[col.dataIndex] = b
      return a
    }, {})
  })

  if (errorMsg) {
    previewSuccess.value = false
    message.error(errorMsg)
  } else {
    previewSuccess.value = true
    return result
  }
}
const fetchPreview = async path => {
  try {
    previewLoading.value = true

    const { metaData = [], rowData = [] } = await getPreview({ path })
    previewColumns.value = transformColumns({
      data: rowData,
      columns: metaData
    })

    previewLoaded.value = true
    previewData.value = transformList({
      data: rowData,
      columns: toRaw(previewColumns.value)
    })
    detailInfo.value.uploadPath = path
  } catch (error) {
    console.error('获取预览失败', error)
  } finally {
    previewLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.steps {
  :deep(.ant-steps-item) {
    .ant-steps-item-container {
      cursor: initial !important;
    }
  }
}
.content {
  flex: 1;
  overflow: auto;
}

.upload {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.upload-box {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 300px;
  height: 250px;
  margin: 0 auto;
  background-color: #e6f0ff;
  border-radius: 4px;
  cursor: pointer;
  &:hover {
    background-color: #dbe8fa;
  }
}

.upload-input {
  position: absolute;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}
.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  p {
    margin-bottom: 0;
  }
}
.upload-img {
  // width: 110px;
  width: 60%;
}
</style>
