export interface ClassroomDTO {
  id: number;
  name: string;
  studentCount: number;
}

export interface StudentPreviewDTO {
  id: number;
  username: string;
  name: string;
  lastActivity: string | null;
}
