import { Author } from "./author";

export interface Series {
  id: number;
  name: string;
  author: Author;
  description?: string;
  
}