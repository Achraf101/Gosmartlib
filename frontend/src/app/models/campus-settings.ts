export interface CampusSettings {
  campusId: number;
  monthlyBook: boolean;
  inSpotlight: boolean;
}

export interface HiddenComponent {
  screen: string;
  type: string;
}

export interface CampusSettings {
  campusId: number;
  hiddenComponents: HiddenComponent[];
}

export function isVisible(settings: CampusSettings | null, screen: string, type: string): boolean {
  if (!settings) return true;
  if (!settings.hiddenComponents) return true;
  return !settings.hiddenComponents.some((c) => c.screen === screen && c.type === type);
}
