export interface SchoolSettings {
  schoolId: number;
  monthlyBook: boolean;
  inSpotlight: boolean;
  hiddenComponents: HiddenComponent[];
}

export interface HiddenComponent {
  screen: string;
  type: string;
}

export function isVisible(settings: SchoolSettings | null, screen: string, type: string): boolean {
  if (!settings) return true;
  if (!settings.hiddenComponents) return true;
  return !settings.hiddenComponents.some((c) => c.screen === screen && c.type === type);
}
