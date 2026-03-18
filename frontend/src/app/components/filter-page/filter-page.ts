import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { ApiService } from '../../services/api';
import { Author } from '../../models/author';
import { Genre } from '../../models/genre';
import { Language } from '../../models/language';

@Component({
  selector: 'app-filter-page',
  standalone: true,
  imports: [CommonModule, FormsModule, NavBarComponent],
  templateUrl: './filter-page.html',
  styleUrl: './filter-page.css',
})
export class FilterPage implements OnInit {
  genres: Genre[] = [];
  languages: Language[] = [];
  authors: Author[] = [];

  selectedGenres: number[] = [];
  selectedLanguage: number | null = null;
  selectedFiction: boolean | null = null;
  selectedAuthor: number | null = null;
  ageMin: number | null = null;
  ageMax: number | null = null;
  pagesMin: number | null = null;
  pagesMax: number | null = null;

  constructor(
    private apiService: ApiService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.apiService.get<Genre[]>('genre').subscribe(g => this.genres = g);
    this.apiService.get<Language[]>('language').subscribe(l => this.languages = l);
    this.apiService.get<Author[]>('author').subscribe(a => this.authors = a);


    this.route.queryParams.subscribe(params => {
      if (params['genres']) {
        this.selectedGenres = Array.isArray(params['genres'])
          ? params['genres'].map(Number)
          : [Number(params['genres'])];
      }
      if (params['language']) this.selectedLanguage = Number(params['language']);
      if (params['fiction'] !== undefined) this.selectedFiction = params['fiction'] === 'true';
      if (params['authorId']) this.selectedAuthor = Number(params['authorId']);
      if (params['ageMin']) this.ageMin = Number(params['ageMin']);
      if (params['ageMax']) this.ageMax = Number(params['ageMax']);
      if (params['pagesMin']) this.pagesMin = Number(params['pagesMin']);
      if (params['pagesMax']) this.pagesMax = Number(params['pagesMax']);
    });
  }

  toggleGenre(id: number): void {
    const index = this.selectedGenres.indexOf(id);
    if (index > -1) {
      this.selectedGenres.splice(index, 1);
    } else {
      this.selectedGenres.push(id);
    }
  }

  isGenreSelected(id: number): boolean {
    return this.selectedGenres.includes(id);
  }

  onSearch(): void {
  const params: any = {};
  if (this.selectedGenres.length > 0) params['genres'] = this.selectedGenres;
  if (this.selectedLanguage) params['language'] = this.selectedLanguage;
  if (this.selectedFiction !== null) params['fiction'] = this.selectedFiction;
  if (this.selectedAuthor) params['authorId'] = this.selectedAuthor;
  if (this.ageMin !== null) params['ageMin'] = this.ageMin;
  if (this.ageMax !== null) params['ageMax'] = this.ageMax;
  if (this.pagesMin !== null) params['pagesMin'] = this.pagesMin;
  if (this.pagesMax !== null) params['pagesMax'] = this.pagesMax;

  this.router.navigate(['/catalogus'], { queryParams: params });
}

  clearFilters(): void {
    this.selectedGenres = [];
    this.selectedLanguage = null;
    this.selectedFiction = null;
    this.selectedAuthor = null;
    this.ageMin = null;
    this.ageMax = null;
    this.pagesMin = null;
    this.pagesMax = null;
  }
}