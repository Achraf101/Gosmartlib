export interface BulkPreviewItem {
  row: number;
  isbn: string;
  found: boolean;
  title: string | null;
  author: string | null;
  coverUrl: string | null;
}

export interface BulkPreviewResult {
  items: BulkPreviewItem[];
  total: number;
  foundCount: number;
  notFoundCount: number;
}
