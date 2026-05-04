import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api';

interface ChallengeDTO {
  id: number;
  description: string;
  conditionType: string;
  conditionValue: string;
  completed: boolean;
}

interface GamificationDTO {
  totalBooks: number;
  streakLevel: string;
  challenges: ChallengeDTO[];
}

interface Badge {
  label: string;
  icon: string;
  required: number;
}

@Component({
  selector: 'app-gamification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gamification.html',
  styleUrl: './gamification.css',
})
export class Gamification implements OnInit {
  gamification: GamificationDTO | null = null;

 badges: Badge[] = [
    { label: 'Beginner', icon: '📚', required: 1 },
    { label: 'Op dreef', icon: '🔥', required: 3 },
    { label: '5 op rij', icon: '⭐', required: 5 },
    { label: 'Nachtlezer', icon: '🌙', required: 10 },
    { label: 'Klassiek', icon: '🏛️', required: 15 },
    { label: 'Leesheld', icon: '🦸', required: 25 },
    { label: 'Bibliofiel', icon: '🎓', required: 50 },
    { label: 'Meestelezer', icon: '👑', required: 75 },
    { label: 'Legende', icon: '🌟', required: 100 },
    { label: 'Grootmeester', icon: '🔮', required: 150 },
    { label: 'Onsterfelijke', icon: '⚡', required: 200 },
];

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.apiService.get<any>('gamification').subscribe({
      next: (data) => (this.gamification = {
        totalBooks: data.total_books,
        streakLevel: data.streak_level,
        challenges: data.challenges.map((c: any) => ({
          id: c.id,
          description: c.description,
          conditionType: c.condition_type,
          conditionValue: c.condition_value,
          completed: c.completed
        }))
      }),
    });
  }

  getBadgeProgress(): number {
    if (!this.gamification || this.gamification.totalBooks === 0) return 0;
    const total = this.gamification.totalBooks;
    const nextBadge = this.badges.find(b => b.required > total);
    if (!nextBadge) return 100;
    const prevBadge = [...this.badges].reverse().find(b => b.required <= total);
    const prev = prevBadge ? prevBadge.required : 0;
    return Math.round(((total - prev) / (nextBadge.required - prev)) * 100);
  }

  getNextBadge(): Badge | null {
    if (!this.gamification || this.gamification.totalBooks === 0) {
      return this.badges[0];
    }
    return this.badges.find(b => b.required > this.gamification!.totalBooks) ?? null;
  }

  isUnlocked(badge: Badge): boolean {
    return (this.gamification?.totalBooks ?? 0) >= badge.required;
  }

  getTypeLabel(type: string): string {
  switch(type) {
    case 'genre': return '📖 Genre uitdaging';
    case 'pages': return '📄 Pagina uitdaging';
    case 'year': return '📅 Jaar uitdaging';
    case 'language': return '🌍 Taal uitdaging';
    default: return '📚 Uitdaging';
  }
}

getCompletedCount(): number {
  return this.gamification?.challenges.filter(c => c.completed).length ?? 0;
}
}