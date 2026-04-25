import { Component, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 's-bar',
  imports: [FormsModule],
  templateUrl: './search-bar.html',
  styleUrl: './search-bar.css',
})
export class SearchBar {
  query = '';

  @Output() search = new EventEmitter<string>();

  onSearch(): void {
    this.search.emit(this.query.trim());
  }

  onDelete(): void {
    this.query = '';
    this.search.emit(this.query.trim());
  }
}
