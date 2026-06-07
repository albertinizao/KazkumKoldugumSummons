export type CreatureInstanceState = 'HEALTHY' | 'DAMAGED' | 'DOWN';

export interface DailyUsesState {
  maximum: number;
  remaining: number;
}

export interface CombatActionButton {
  label: string;
  description: string;
}
