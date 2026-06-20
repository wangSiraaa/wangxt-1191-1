export type Role = 'BLASTER' | 'STOREKEEPER' | 'SAFETY_OFFICER';

export type ExplosiveType = 'DETONATOR' | 'EXPLOSIVE';

export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'NEED_REVIEW' | 'OUTBOUND_COMPLETED' | 'INBOUND_COMPLETED' | 'CLOSED';

export type ShiftStatus = 'OPEN' | 'IN_PROGRESS' | 'WAITING_RETURN' | 'WAITING_VERIFY' | 'CLOSED';

export type AnomalyType = 'EXPIRED_LICENSE' | 'QUANTITY_MISMATCH' | 'NOT_RETURNED' | 'DAMAGE' | 'MISFIRE' | 'OTHER';

export interface User {
  id: number;
  username: string;
  name: string;
  role: Role;
  phone: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  name: string;
  role: Role;
  userId: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface Shift {
  id: number;
  shiftNo: string;
  workFace: string;
  blaster: User;
  status: ShiftStatus;
  startTime: string;
  endTime?: string;
  remarks?: string;
  workPlan?: WorkPlan;
  actualHoles: number;
  remainingCleared: boolean;
  misfireHandled: boolean;
}

export interface WorkPlan {
  id: number;
  planNo: string;
  workFace: string;
  designedHoles: number;
  estimatedDetonators: number;
  estimatedExplosives: number;
  workDate: string;
  description?: string;
}

export interface PickupApplication {
  id: number;
  applicationNo: string;
  shift: Shift;
  blaster: User;
  detonatorQuantity: number;
  explosiveQuantity: number;
  status: ApplicationStatus;
  rejectionReason?: string;
  reviewRemark?: string;
  reviewer?: User;
  reviewedAt?: string;
  createdAt: string;
}

export interface Explosive {
  id: number;
  serialNo: string;
  type: ExplosiveType;
  name: string;
  specification: string;
  quantity: number;
  availableQuantity: number;
  batchNo: string;
}

export interface OutboundRecord {
  id: number;
  outboundNo: string;
  explosiveSerialNo: string;
  type: ExplosiveType;
  quantity: number;
  storekeeper: User;
  blaster: User;
  outboundTime: string;
  workFace: string;
  remarks?: string;
}

export interface InboundRecord {
  id: number;
  inboundNo: string;
  explosiveSerialNo: string;
  type: ExplosiveType;
  usedQuantity: number;
  returnedQuantity: number;
  storekeeper: User;
  blaster: User;
  inboundTime: string;
  remarks?: string;
}

export interface VerificationRecord {
  id: number;
  verificationNo: string;
  safetyOfficer: User;
  expectedDetonators: number;
  usedDetonators: number;
  returnedDetonators: number;
  expectedExplosives: number;
  usedExplosives: number;
  returnedExplosives: number;
  allReturned: boolean;
  verificationRemark?: string;
  verificationTime: string;
}

export interface AnomalyRecord {
  id: number;
  recordNo: string;
  type: AnomalyType;
  description: string;
  explosiveSerialNo?: string;
  anomalyQuantity?: number;
  reportedBy: User;
  handledBy?: User;
  reportedAt: string;
  handledAt?: string;
  handlingResult?: string;
  resolved: boolean;
}

export interface HoleChangeRecord {
  id: number;
  changeNo: string;
  shift: Shift;
  application?: PickupApplication;
  originalHoles: number;
  newHoles: number;
  holeDifference: number;
  originalDetonators: number;
  newDetonators: number;
  originalExplosives: number;
  newExplosives: number;
  changeReason: string;
  requestedBy: User;
  reviewedBy?: User;
  status: ApplicationStatus;
  reviewRemark?: string;
  requestedAt: string;
  reviewedAt?: string;
}
