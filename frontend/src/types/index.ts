export interface Bank {
  id: number;
  name: string;
  code: string;
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  bankId: number | null;
  bankName: string | null;
  permissions: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserProfile;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}

export interface SecurityAlert {
  id: number;
  stationId?: number;
  bankId: number;
  bankName?: string;
  alertType: string;
  severity: 'CRITICAL' | 'WARNING' | 'INFO';
  title: string;
  message: string;
  locationName?: string;
  acknowledged: boolean;
  receivedAt: string;
}
