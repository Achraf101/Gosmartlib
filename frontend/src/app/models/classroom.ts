export interface ClassroomDTO {
  id: number;
  name: string;
  studentCount: number;
}

export interface StudentPreviewDTO {
  id: number;
  firstName: string | null;
  lastName: string | null;
  lastActivity: string | null;
}
