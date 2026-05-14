export interface LocationSettings {
  locationId: number;
  monthlyBook: boolean;
  inSpotlight: boolean;
}

export interface HiddenComponent {
  screen: string;
  type: string;
}

export interface LocationSettings {
  locationId: number;
  hiddenComponents: HiddenComponent[];
}

export function isVisible(
  settings: LocationSettings | null,
  screen: string,
  type: string,
): boolean {
  if (!settings) return true;
  if (!settings.hiddenComponents) return true;
  return !settings.hiddenComponents.some((c) => c.screen === screen && c.type === type);
}
