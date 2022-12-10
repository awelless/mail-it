<template>
  <div class='q-gutter-md'>
    <q-input outlined v-model='name' label='Name' />
    <q-input outlined v-model='description' label='Description' />

    <q-input outlined v-model='maxRetriesCount' label='Max Retries' type='number' :disable='infiniteRetries'>
      <template v-slot:after>
        <q-toggle v-model='infiniteRetries' label='infinite' left-label />
      </template>
    </q-input>

    <q-select outlined v-model='contentType' label='Content type' :options='Object.values(MailMessageContentType)' />

    <template v-if='contentType === MailMessageContentType.HTML'>
      <q-select
        outlined
        v-model='templateEngine'
        label='Template Engine'
        :options='Object.values(HtmlTemplateEngine)'
      />

      <p class='text-body1'>Upload template file</p>
      <q-file
        outlined
        v-model='templateFile'
        label='Template File'
        @update:model-value='handleUpload'
        :accept='acceptFileTypes'
      >
        <template v-slot:prepend>
          <q-icon name='attach_file' />
        </template>
      </q-file>

      <p class='text-body1'>or paste it here</p>
      <q-input outlined v-model='template' label='Template' type='textarea' />
    </template>

    <div class='row'>
      <div class='col-md-2 q-gutter-sm'>
        <q-btn @click='create' color='info' style='width:40%'>Create</q-btn>
        <q-btn to='/types' class='text-black bg-white' style='width:40%'>Back</q-btn>
      </div>
    </div>
  </div>
</template>

<script setup lang='ts'>
import { computed, ref } from 'vue'
import { HtmlTemplateEngine, MailMessageContentType } from 'src/models/MailMessageType'

const name = ref('')
const description = ref('')
const infiniteRetries = ref(false)
const maxRetriesCount = ref<number | null>(null)
const contentType = ref<string | null>(null)
const templateEngine = ref<HtmlTemplateEngine | null>(null)
const templateFile = ref<File | null>(null)
const template = ref<string | null>(null)

const acceptFileTypes = computed(() => {
  switch (templateEngine.value) {
    case null: return '*/*'
    case HtmlTemplateEngine.NONE: return '.html'
    case HtmlTemplateEngine.FREEMARKER: return '.ftl, .ftlh'
    default: throw Error(`Unsupported template engine: ${templateEngine.value}`)
  }
})

async function handleUpload(file: File) {
  template.value = await file.text()
}

function create() {
  // todo
  console.log('creation')
}
</script>

<style scoped>

</style>
