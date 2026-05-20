export interface ChallengeDTO {
  id: number;
  description: string;
  conditionType: string;
  conditionValue: string;
  completed: boolean;
}

export interface GamificationDTO {
  totalBooks: number;
  streakLevel: string;
  challenges: ChallengeDTO[];
}

export interface Badge {
  label: string;
  icon: string;
  required: number;
}