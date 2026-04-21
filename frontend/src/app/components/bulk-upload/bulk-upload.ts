import { Component } from '@angular/core';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { BulkUpload as BulkUpload_1 } from '../../services/BulkUpload';
import { ButtonModule } from 'primeng/button';

interface RowIssue {
  row: number;
  message: string;
}

interface BulkUploadResult {
  added: number;
  skipped: RowIssue[];
  errors: RowIssue[];
  skippedCount: number;
  errorCount: number;
  total_processed: number;
  fullSuccess: boolean;
}

@Component({
  selector: 'app-bulk-upload',
  templateUrl: './bulk-upload.html',
  imports: [ButtonModule],
  styleUrls: ['./bulk-upload.css'],
})
export class BulkUpload {
  selectedFile: File | null = null;
  isDragging = false;
  isUploading = false;
  uploadProgress = 0;
  result: BulkUploadResult | null = null;
  uploadError: string | null = null;

  constructor(
    private http: HttpClient,
    private bulkUpload: BulkUpload_1,
  ) {}

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(): void {
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    const file = event.dataTransfer?.files[0];
    if (file) this.setFile(file);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.setFile(file);
  }

  private setFile(file: File): void {
    if (!file.name.endsWith('.xlsx')) {
      this.uploadError = 'Only .xlsx files are accepted.';
      return;
    }
    this.selectedFile = file;
    this.uploadError = null;
    this.result = null;
  }

  clearFile(): void {
    this.selectedFile = null;
    this.uploadProgress = 0;
    this.result = null;
    this.uploadError = null;
  }

  upload(): void {
    if (!this.selectedFile) return;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.isUploading = true;
    this.uploadProgress = 0;
    this.uploadError = null;

    this.http
      .post<BulkUploadResult>('/api/excel/book/bulk-upload', formData, {
        reportProgress: true,
        observe: 'events',
      })
      .subscribe({
        next: (event) => {
          if (event.type === HttpEventType.UploadProgress && event.total) {
            this.uploadProgress = Math.round((100 * event.loaded) / event.total);
          } else if (event.type === HttpEventType.Response) {
            this.result = event.body;
            this.isUploading = false;
          }
        },
        error: (err) => {
          this.uploadError = err.error?.message ?? 'Upload failed. Please try again.';
          this.isUploading = false;
        },
      });
  }

  reset(): void {
    this.selectedFile = null;
    this.result = null;
    this.uploadProgress = 0;
    this.uploadError = null;
  }
}
