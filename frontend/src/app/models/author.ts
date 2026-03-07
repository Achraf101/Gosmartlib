export interface Author {
  id: number;
  name: string;
  description?: string;
  type?: 'COAUTEUR' | 'ILLUSTRATOR' | 'VERTALER' | 'REDACTEUR' | 'FOTOGRAAF' | 'OMSLAGONTWERPER';
}