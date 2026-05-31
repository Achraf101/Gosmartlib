import { Component, Input } from '@angular/core';
import { Button, ButtonDirective } from 'primeng/button';
import { Material } from '../../models/material';
import { Card } from 'primeng/card';

@Component({
  selector: 'app-material',
  imports: [Button],
  templateUrl: './material.html',
  styleUrl: './material.css',
})
export class MaterialComponent {
  @Input() material!: Material;
  icon?: string;

  ngOnInit() {
    switch (this.getExtension(this.material.file_name.toLocaleLowerCase())) {
      case 'pdf':
        this.icon = 'file-pdf';
        break;

      case 'doc':
      case 'docx':
      case 'odt':
        this.icon = 'file-word';
        break;

      case 'xls':
      case 'xlsx':
        this.icon = 'file-excel';
        break;

      case 'png':
      case 'jpg':
      case 'jpeg':
      case 'webp':
      case 'gif':
      case 'svg':
      case 'bmp':
      case 'ico':
        this.icon = 'image';
        break;

      default:
        this.icon = 'file';
        break;
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('nl-BE');
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  getExtension(filename: string): string {
    return filename.split('.').pop() ?? '';
  }
}
