import { Pipe, PipeTransform } from '@angular/core';
import { Reading, KindOfMeter } from '../../core/models/reading.model';

@Pipe({ name: 'kindCount', standalone: true })
export class KindCountPipe implements PipeTransform {
  transform(readings: Reading[], kind: KindOfMeter): number {
    return readings.filter(r => r.kindOfMeter === kind).length;
  }
}
