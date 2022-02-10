export interface Tag {
  key: string,
  value: string,
}

export interface MetricDto {
  name: string,
  description?: string,
  baseUnit?: string,
  measurements: MeasurementDto[],
  availableTags: TagDto[],
}

export interface MeasurementDto {
  statistic: string,
  value: number,
}

export interface TagDto {
  tag: string,
  values: string[],
}
