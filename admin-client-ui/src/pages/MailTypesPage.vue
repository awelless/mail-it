<template>
  <q-spinner v-if='!types' color='primary' size='3em' />
  <div v-else>
    <div v-for='type in types.content' :key='type.id'>
      <MailMessageTypeCard :type='type' />
    </div>
    <br />
    <SliceFooter :slice='types' :fetch-function='fetchFunction' />
  </div>
</template>

<script setup lang='ts'>
import { ref } from 'vue'
import MailMessageType, { HtmlTemplateEngine, MailMessageContentType } from 'src/models/MailMessageType'
import Slice from 'src/models/Slice'
import SliceFooter from 'components/SliceFooter.vue'
import MailMessageTypeCard from 'components/MailMessageTypeCard.vue'

const types = ref<Slice<MailMessageType> | null>(null)

types.value = {
  page: 1,
  size: 10,
  last: false,
  content: [
    {
      id: 2,
      name: 'Another Casual mail',
      description: 'some another description',
      contentType: MailMessageContentType.PLAIN_TEXT,
    },
    {
      id: 1,
      name: 'Casual mail',
      description: 'some description',
      maxRetriesCount: 10,
      contentType: MailMessageContentType.PLAIN_TEXT,
    },
  ],
}

const fetchFunction = (page: number) => {
  if (page == 1) {
    types.value = {
      page: 1,
      size: 10,
      last: false,
      content: [
        {
          id: 2,
          name: 'Another Casual mail',
          description: 'some another description',
          contentType: MailMessageContentType.PLAIN_TEXT,
        },
        {
          id: 1,
          name: 'Casual mail',
          description: 'some description',
          maxRetriesCount: 10,
          contentType: MailMessageContentType.PLAIN_TEXT,
        },
      ],
    }
  } else {
    types.value = {
      page: 2,
      size: 10,
      last: true,
      content: [
        {
          id: 4,
          name: 'Ad mail',
          description: 'some useless mail',
          maxRetriesCount: 0,
          contentType: MailMessageContentType.HTML,
          templateEngine: HtmlTemplateEngine.NONE,
          template: '<html lang="en"><body>Ads</body></html>',
        },
        {
          id: 3,
          name: 'Registration mail',
          description: 'mail sent after registration',
          maxRetriesCount: 100,
          contentType: MailMessageContentType.HTML,
          templateEngine: HtmlTemplateEngine.FREEMARKER,
          template: '<html lang="en"><body>${name} hello</body></html>',
        },
      ],
    }
  }
}
</script>

<style scoped></style>
