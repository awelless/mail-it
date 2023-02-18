<template>
  <div class="q-gutter-md">
    <q-input outlined v-model="name" label="Name" />
    <q-input outlined v-model="description" label="Description" />

    <q-input outlined v-model="maxRetriesCount" label="Max Retries" type="number" :disable="infiniteRetries">
      <template v-slot:after>
        <q-toggle v-model="infiniteRetries" label="infinite" left-label />
      </template>
    </q-input>

    <q-select outlined v-model="contentType" label="Content type" :options="Object.values(MailMessageContentType)" />

    <template v-if="contentType === MailMessageContentType.HTML">
      <q-select outlined v-model="templateEngine" label="Template Engine" :options="Object.values(HtmlTemplateEngine)" />

      <p class="text-body1">Upload template file</p>
      <q-file outlined v-model="templateFile" label="Template File" @update:model-value="handleUpload" :accept="acceptFileTypes">
        <template v-slot:prepend>
          <q-icon name="attach_file" />
        </template>
      </q-file>

      <p class="text-body1">or paste it here</p>
      <q-input outlined v-model="template" label="Template" type="textarea" />
    </template>

    <div class="row">
      <div class="col-md-2 q-gutter-sm">
        <q-btn @click="submit()" color="info" style="width: 40%">{{ submissionButtonMessage }}</q-btn>
        <q-btn :to="backPath" class="text-black bg-white" style="width: 40%">Back</q-btn>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import MailMessageType, { HtmlTemplateEngine, MailMessageContentType } from 'src/models/MailMessageType'

const props = defineProps<{
  mailMessageType?: MailMessageType
  submissionButtonMessage: string
  submissionAction: (type: MailMessageType) => Promise<void>
  backPath: string
}>()

const name = ref(props.mailMessageType?.name ?? '')
const description = ref(props.mailMessageType?.description ?? '')
const infiniteRetries = ref(props.mailMessageType && !props.mailMessageType.maxRetriesCount)
const maxRetriesCount = ref<number | undefined>(props.mailMessageType?.maxRetriesCount)
const contentType = ref<MailMessageContentType | undefined>(props.mailMessageType?.contentType)
const templateEngine = ref<HtmlTemplateEngine | undefined>(props.mailMessageType?.templateEngine)
const templateFile = ref<File | undefined>(undefined)
const template = ref<string | undefined>(props.mailMessageType?.template)

const acceptFileTypes = computed(() => {
  switch (templateEngine.value) {
    case null:
      return '*/*'
    case undefined:
      return '*/*'
    case HtmlTemplateEngine.NONE:
      return '.html'
    case HtmlTemplateEngine.FREEMARKER:
      return '.ftl, .ftlh'
    default:
      throw Error(`Unsupported template engine: ${templateEngine.value}`)
  }
})

async function handleUpload(file: File) {
  template.value = await file.text()
}

async function submit() {
  const actualContentType = contentType.value

  // todo validation
  if (!actualContentType) {
    return
  }

  await props.submissionAction({
    id: props.mailMessageType?.id ?? 0,
    name: name.value,
    description: description.value,
    maxRetriesCount: infiniteRetries.value ? undefined : maxRetriesCount.value,
    contentType: actualContentType,
    templateEngine: templateEngine.value,
    template: template.value,
  })
}
</script>

<style scoped></style>
