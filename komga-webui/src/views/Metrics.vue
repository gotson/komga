<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col>
        <v-card>
          <v-card-title>{{ $t('metrics.tasks_executed') }}</v-card-title>
          <v-card-text>
            <bar-chart :data="tasksCount"/>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col>
        <v-card>
          <v-card-title>{{ $t('metrics.tasks_total_time') }}</v-card-title>
          <v-card-text>
            <bar-chart :data="tasksTotalTime" suffix="s" :round="0"/>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col>
        <v-card>
          <v-card-title>
            {{ $t('common.all_libraries') }}
          </v-card-title>
          <v-card-text>
            <v-simple-table>
              <tbody>
              <tr>
                <td>{{ $t('common.disk_space') }}</td>
                <td> {{ getFileSize(booksFileSize.measurements[0].value) }}</td>
              </tr>
              <tr>
                <td>{{ $t('common.series') }}</td>
                <td> {{ series.measurements[0].value }}</td>
              </tr>
              <tr>
                <td>{{ $t('common.books') }}</td>
                <td> {{ books.measurements[0].value }}</td>
              </tr>
              <tr>
                <td>{{ $t('common.collections') }}</td>
                <td> {{ collections.measurements[0].value }}</td>
              </tr>
              <tr>
                <td>{{ $t('common.readlists') }}</td>
                <td> {{ readlists.measurements[0].value }}</td>
              </tr>
              <tr>
                <td>{{ $t('common.sidecars') }}</td>
                <td> {{ sidecars.measurements[0].value }}</td>
              </tr>
              </tbody>
            </v-simple-table>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col>
        <v-card>
          <v-card-title>{{ $t('metrics.library_disk_space') }}</v-card-title>
          <v-card-text>
            <pie-chart :data="fileSizeAllTags" :legend="false" :bytes="true"/>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col>
        <v-card>
          <v-card-title>{{ $t('metrics.library_books') }}</v-card-title>
          <v-card-text>
            <pie-chart :data="booksAllTags" :legend="false"/>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col>
        <v-card>
          <v-card-title>{{ $t('metrics.library_series') }}</v-card-title>
          <v-card-text>
            <pie-chart :data="seriesAllTags" :legend="false"/>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col>
        <v-card>
          <v-card-title>{{ $t('metrics.library_sidecars') }}</v-card-title>
          <v-card-text>
            <pie-chart :data="sidecarsAllTags" :legend="false"/>
          </v-card-text>
        </v-card>
      </v-col>

    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {MetricDto} from '@/types/komga-metrics'
import {getFileSize} from '@/functions/file'

export default Vue.extend({
  name: 'Metrics',
  data: () => ({
    getFileSize,
    tasks: {} as MetricDto,
    tasksCount: {} as { [key: string]: number | undefined } | undefined,
    tasksTotalTime: {} as { [key: string]: number | undefined } | undefined,
    series: {} as MetricDto,
    seriesAllTags: {} as { [key: string]: number | undefined } | undefined,
    books: {} as MetricDto,
    booksAllTags: {} as { [key: string]: number | undefined } | undefined,
    sidecars: {} as MetricDto,
    sidecarsAllTags: {} as { [key: string]: number | undefined } | undefined,
    booksFileSize: {} as MetricDto,
    fileSizeAllTags: {} as { [key: string]: number | undefined } | undefined,
    collections: {} as MetricDto,
    readlists: {} as MetricDto,
  }),
  computed: {},
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.tasks = await this.$komgaMetrics.getMetric('komga.tasks.execution')
      this.tasksCount = await this.getStatisticForEachTagValue(this.tasks, 'type', 'COUNT')
      this.tasksTotalTime = await this.getStatisticForEachTagValue(this.tasks, 'type', 'TOTAL_TIME')

      this.series = await this.$komgaMetrics.getMetric('komga.series')
      this.seriesAllTags = await this.getStatisticForEachTagValue(this.series, 'library')

      this.books = await this.$komgaMetrics.getMetric('komga.books')
      this.booksAllTags = await this.getStatisticForEachTagValue(this.books, 'library')

      this.booksFileSize = await this.$komgaMetrics.getMetric('komga.books.filesize')
      this.fileSizeAllTags = await this.getStatisticForEachTagValue(this.booksFileSize, 'library')

      this.sidecars = await this.$komgaMetrics.getMetric('komga.sidecars')
      this.sidecarsAllTags = await this.getStatisticForEachTagValue(this.sidecars, 'library')

      this.collections = await this.$komgaMetrics.getMetric('komga.collections')

      this.readlists = await this.$komgaMetrics.getMetric('komga.readlists')

    },
    async getStatisticForEachTagValue(metric: MetricDto, tag: string, statistic: string = 'VALUE'): Promise<{ [key: string]: number | undefined } | undefined> {
      const tagDto = metric.availableTags.find(x => x.tag === tag)
      if (tagDto) {
        const tagToStatistic = tagDto.values.reduce((a, b) => {
          a[b] = 0
          return a
        }, {} as { [key: string]: number | undefined })

        for (let tagKey in tagToStatistic) {
          tagToStatistic[tagKey] = (await this.$komgaMetrics.getMetric(metric.name, [{
            key: tag,
            value: tagKey,
          }])).measurements.find(x => x.statistic === statistic)?.value
        }
        return this.$_.cloneDeep(tagToStatistic)
      }
      return undefined
    },
  },
})
</script>

<style scoped>

</style>
