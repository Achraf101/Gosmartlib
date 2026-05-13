import { Component, OnInit } from '@angular/core';
import { BookDetail } from '../../models/book';
import { ActivatedRoute } from '@angular/router';
import { BookService } from '../../services/book';
import { BookformComponent } from "../bookform/bookform";

@Component({
  selector: 'app-book-edit',
  imports: [BookformComponent],
  templateUrl: './book-edit.html',
  styleUrl: './book-edit.css',
})
export class BookEdit implements OnInit{
  book?: BookDetail;

  constructor(private route: ActivatedRoute, private bookService: BookService) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.bookService.getById(id).subscribe(b => this.book = b);
  }
}
