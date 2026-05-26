import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { ApiService } from '../../services/api';
import { Author } from '../../models/author';
import { Genre } from '../../models/genre';
import { Language } from '../../models/language';
import { Series } from '../../models/series'; 
import { AutoCompleteModule, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { Theme } from '../../models/theme';
import { ThemeService } from '../../services/theme';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-filter-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavBarComponent,
    AutoCompleteModule,
    SelectModule,
    InputNumberModule,
    TooltipModule
  ],
  templateUrl: './filter-page.html',
  styleUrl: './filter-page.css',
})
export class FilterPage implements OnInit {
  genres: Genre[] = [];
  languages: Language[] = [];
  themes: Theme[] = [];

  selectedGenres: number[] = [];
  selectedThemes: number[] = [];
  selectedLanguage: number | null = null;
  selectedFiction: boolean | null = null;
  selectedAuthors: Author[] = [];
  authorSuggestions: Author[] = [];
  selectedSeries: Series[] = [];
  seriesSuggestions: Series[] = [];
  selectedClib: string[] = [];
  pagesMin: number | null = null;
  pagesMax: number | null = null;
  errorPagesMin: string | null = null;
  errorPagesMax: string | null = null;

  constructor(
    private apiService: ApiService,
    private themeService: ThemeService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.apiService.get<Genre[]>('genre').subscribe((g) => (this.genres = g));
    this.apiService.get<Language[]>('language').subscribe((l) => (this.languages = l));
    this.themeService.getAll().subscribe({
      next: (data) => (this.themes = data),
    });

    this.route.queryParams.subscribe((params) => {
      if (params['genres']) {
        this.selectedGenres = params['genres'].split(',').map(Number);
      }
      if (params['themes']) {
        this.selectedThemes = params['themes'].split(',').map(Number);
      }
      if (params['language']) this.selectedLanguage = Number(params['language']);
      if (params['fiction'] !== undefined) this.selectedFiction = params['fiction'] === 'true';
      if (params['authorIds']) {
        this.selectedAuthors = [];
        params['authorIds'].split(',').forEach((id: string) => {
          this.apiService
            .get<Author>(`author/${id}`)
            .subscribe((a) => this.selectedAuthors.push(a));
        });
      }
      if (params['seriesIds']) {
        this.selectedSeries = [];
        params['seriesIds'].split(',').forEach((id: string) => {
          this.apiService.get<Series>(`series/${id}`).subscribe((s) => this.selectedSeries.push(s));
        });
      }
      if (params['clibs']) {
        this.selectedClib = params['clibs'].split(',');
      }
      if (params['pagesMin']) this.pagesMin = Number(params['pagesMin']);
      if (params['pagesMax']) this.pagesMax = Number(params['pagesMax']);
    });
  }

  searchAuthors(event: AutoCompleteCompleteEvent): void {
    const query = event.query.trim();
    if (!query) {
      this.authorSuggestions = [];
      return;
    }
    this.apiService
      .get<Author[]>(`author/search/${encodeURIComponent(query)}`)
      .subscribe((a) => (this.authorSuggestions = a));
  }

  searchSeries(event: AutoCompleteCompleteEvent): void {
    const query = event.query.trim();
    if (!query) {
      this.seriesSuggestions = [];
      return;
    }
    this.apiService
      .get<Series[]>(`series/search/${encodeURIComponent(query)}`)
      .subscribe((s) => (this.seriesSuggestions = s));
  }

  toggleTheme(id: number): void {
    const index = this.selectedThemes.indexOf(id);
    if (index > -1) {
      this.selectedThemes.splice(index, 1);
    } else {
      this.selectedThemes.push(id);
    }
  }

  toggleGenre(id: number): void {
    const index = this.selectedGenres.indexOf(id);
    if (index > -1) {
      this.selectedGenres.splice(index, 1);
    } else {
      this.selectedGenres.push(id);
    }
  }

  isThemeSelected(id: number): boolean {
    return this.selectedThemes.includes(id);
  }

  isGenreSelected(id: number): boolean {
    return this.selectedGenres.includes(id);
  }

  toggleClib(level: string): void {
    const index = this.selectedClib.indexOf(level);
    if (index > -1) {
      this.selectedClib.splice(index, 1);
    } else {
      this.selectedClib.push(level);
    }
  }

  isClibSelected(level: string): boolean {
    return this.selectedClib.includes(level);
  }

  validate(): boolean {
    this.errorPagesMin = null;
    this.errorPagesMax = null;

    if (this.pagesMin !== null && this.pagesMin < 0) {
      this.errorPagesMin = "Pagina's kan niet negatief zijn.";
      return false;
    }
    if (this.pagesMax !== null && this.pagesMax < 0) {
      this.errorPagesMax = "Pagina's kan niet negatief zijn.";
      return false;
    }
    if (this.pagesMin !== null && this.pagesMax !== null && this.pagesMin > this.pagesMax) {
      this.errorPagesMin = 'Min. moet kleiner zijn dan max.';
      return false;
    }
    return true;
  }

  onSearch(): void {
    if (!this.validate()) return;
    const params: any = {};
    if (this.selectedGenres.length > 0) params['genres'] = this.selectedGenres.join(',');
    if (this.selectedThemes.length > 0) params['themes'] = this.selectedThemes.join(',');
    if (this.selectedLanguage) params['language'] = this.selectedLanguage;
    if (this.selectedFiction !== null) params['fiction'] = this.selectedFiction;
    if (this.selectedAuthors.length > 0)
      params['authorIds'] = this.selectedAuthors.map((a) => a.id).join(',');
    if (this.selectedSeries.length > 0)
      params['seriesIds'] = this.selectedSeries.map((s) => s.id).join(',');
    if (this.selectedClib.length > 0) params['clibs'] = this.selectedClib.join(',');
    if (this.pagesMin !== null) params['pagesMin'] = this.pagesMin;
    if (this.pagesMax !== null) params['pagesMax'] = this.pagesMax;

    this.router.navigate(['/catalogus'], { queryParams: params });
  }

  clearFilters(): void {
    this.selectedGenres = [];
    this.selectedThemes = [];
    this.selectedLanguage = null;
    this.selectedFiction = null;
    this.selectedAuthors = [];
    this.selectedSeries = [];
    this.selectedClib = [];
    this.pagesMin = null;
    this.pagesMax = null;
  }
}
