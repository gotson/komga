<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col v-if="tasksCount">
        <v-card>
          <v-card-title>{{ $t('metrics.tasks_executed') }}</v-card-title>
          <v-card-text>
            <bar-chart :data="tasksCount"/>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col v-if="tasksTotalTime">
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
              <tr v-if="booksFileSize">
                <td>{{ $t('common.disk_space') }}</td>
                <td> {{ getFileSize(booksFileSize.measurements[0].value) }}</td>
              </tr>
              <tr v-if="series">
                <td>{{ $tc('common.series', 2) }}</td>
                <td> {{ series.measurements[0].value }}</td>
              </tr>
              <tr v-if="books">
                <td>{{ $t('common.books') }}</td>
                <td> {{ books.measurements[0].value }}</td>
              </tr>
              <tr v-if="collections">
                <td>{{ $t('common.collections') }}</td>
                <td> {{ collections.measurements[0].value }}</td>
              </tr>
              <tr v-if="readlists">
                <td>{{ $t('common.readlists') }}</td>
                <td> {{ readlists.measurements[0].value }}</td>
              </tr>
              <tr v-if="sidecars">
                <td>{{ $t('common.sidecars') }}</td>
                <td> {{ sidecars.measurements[0].value }}</td>
              </tr>
              </tbody>
            </v-simple-table>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col v-if="fileSizeAllTags">
        <v-card>
          <v-card-title>{{ $t('metrics.library_disk_space') }}</v-card-title>
          <v-card-text>
            <pie-chart :data="fileSizeAllTags" :legend="false" :bytes="true"/>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col v-if="booksAllTags">
        <v-card>
          <v-card-title>{{ $t('metrics.library_books') }}</v-card-title>
          <v-card-text>
            <pie-chart :data="booksAllTags" :legend="false"/>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col v-if="seriesAllTags">
        <v-card>
          <v-card-title>{{ $t('metrics.library_series') }}</v-card-title>
          <v-card-text>
            <pie-chart :data="seriesAllTags" :legend="false"/>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col v-if="sidecarsAllTags">
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
  name: 'MetricsView',
  data: () => ({
    getFileSize,
    tasks: undefined as unknown as MetricDto,
    tasksCount: undefined as unknown as { [key: string]: number | undefined } | undefined,
    tasksTotalTime: undefined as unknown as { [key: string]: number | undefined } | undefined,
    series: undefined as unknown as MetricDto,
    seriesAllTags: undefined as unknown as { [key: string]: number | undefined } | undefined,
    books: undefined as unknown as MetricDto,
    booksAllTags: undefined as unknown as { [key: string]: number | undefined } | undefined,
    sidecars: undefined as unknown as MetricDto,
    sidecarsAllTags: undefined as unknown as { [key: string]: number | undefined } | undefined,
    booksFileSize: undefined as unknown as MetricDto,
    fileSizeAllTags: undefined as unknown as { [key: string]: number | undefined } | undefined,
    collections: undefined as unknown as MetricDto,
    readlists: undefined as unknown as MetricDto,
  }),
  computed: {},
  mounted() {
    this.loadData()
  },
  methods: {
    getLibraryNameById(id: string): string {
      return this.$store.getters.getLibraryById(id).name
    },
    async loadData() {
      this.$komgaMetrics.getMetric('komga.tasks.execution')
        .then(m => {
          this.tasks = m
          this.getStatisticForEachTagValue(m, 'type', 'COUNT')
            .then(m => this.tasksCount = m)
            .catch(() => {
            })
          this.getStatisticForEachTagValue(m, 'type', 'TOTAL_TIME')
            .then(m => this.tasksTotalTime = m)
            .catch(() => {
            })
        })
        .catch(() => {
        })

      this.$komgaMetrics.getMetric('komga.series')
        .then(m => {
            this.series = m
            this.getStatisticForEachTagValue(m, 'library', 'VALUE', this.getLibraryNameById)
              .then(v => this.seriesAllTags = v)
          },
        )
        .catch(() => {
        })

      this.$komgaMetrics.getMetric('komga.books')
        .then(m => {
            this.books = m
            this.getStatisticForEachTagValue(m, 'library', 'VALUE', this.getLibraryNameById)
              .then(v => this.booksAllTags = v)
          },
        )
        .catch(() => {
        })

      this.$komgaMetrics.getMetric('komga.books.filesize')
        .then(m => {
            this.booksFileSize = m
            this.getStatisticForEachTagValue(m, 'library', 'VALUE', this.getLibraryNameById)
              .then(v => this.fileSizeAllTags = v)
          },
        )
        .catch(() => {
        })

      this.$komgaMetrics.getMetric('komga.sidecars')
        .then(m => {
            this.sidecars = m
            this.getStatisticForEachTagValue(m, 'library', 'VALUE', this.getLibraryNameById)
              .then(v => this.sidecarsAllTags = v)
          },
        )
        .catch(() => {
        })

      this.$komgaMetrics.getMetric('komga.collections')
        .then(m => this.collections = m)
        .catch(() => {
        })

      this.$komgaMetrics.getMetric('komga.readlists')
        .then(m => this.readlists = m)
        .catch(() => {
        })
    },
    async getStatisticForEachTagValue(metric: MetricDto, tag: string, statistic: string = 'VALUE', tagTransform: (t: string) => string = x => x): Promise<{
      [key: string]: number | undefined
    } | undefined> {
      const tagDto = metric.availableTags.find(x => x.tag === tag)
      if (tagDto) {
        const tagToStatistic = tagDto.values.reduce((a, b) => {
          a[b] = 0
          return a
        }, {} as { [key: string]: number | undefined })

        for (let tagKey in tagToStatistic) {
          tagToStatistic[tagTransform(tagKey)] = (await this.$komgaMetrics.getMetric(metric.name, [{
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
