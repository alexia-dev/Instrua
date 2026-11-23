/**
 * Single source for all authenticated Instrua API calls.
 * Production deliberately has no silent localhost or demo fallback.
 */
export const API_URL = (import.meta.env.VITE_INSTRUA_API_URL ||
  (import.meta.env.DEV ? "http://localhost:8080" : "")).replace(/\/$/, "");

export type UserRole = "PATIENT" | "COMPANY_OWNER" | "COMPANY_ADMIN" | "RECEPTION" | "CLINICAL" | "BILLING" | "PLATFORM_ADMIN";

export interface Session {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  userId: string;
  name: string;
  email: string;
  roles: UserRole[];
}
export interface Company {
  id: string;
  name: string;
  slug: string;
  timezone: string;
  active: boolean;
}
export interface Client { id: string; name: string; email: string | null; phone: string | null; active: boolean; }
export interface Professional { id: string; name: string; title: string | null; accessRole: UserRole; active: boolean; }
export interface Service { id: string; name: string; description: string | null; durationMinutes: number; price: number | null; active: boolean; }
export type AppointmentStatus = "SCHEDULED" | "CONFIRMED" | "CANCELLED" | "COMPLETED" | "NO_SHOW";
export interface Appointment {
  id: string;
  clientId: string;
  clientName: string;
  employeeId: string | null;
  employeeName: string | null;
  serviceId: string;
  serviceName: string;
  startsAt: string;
  endsAt: string;
  status: AppointmentStatus;
  confirmationStatus: string;
  notes: string | null;
  cancellationReason: string | null;
}
export interface CatalogCompany {
  id: string; name: string; slug: string; timezone: string;
  services: Pick<Service, "id" | "name" | "description" | "durationMinutes" | "price">[];
  professionals: { id: string; name: string; title: string | null }[];
}
export interface CancellationPolicy {
  id: string;
  title: string;
  policy_text: string;
  effective_from: string;
  cancellation_deadline_minutes: number | null;
  late_cancellation_fee: number | null;
}

export async function api<T>(
  path: string,
  token?: string,
  method: "GET" | "POST" | "PATCH" | "PUT" = "GET",
  body?: unknown
): Promise<T> {
  if (!API_URL) throw new Error("A API de produção ainda não está configurada. Defina VITE_INSTRUA_API_URL na implantação.");
  let response: Response;
  try {
    response = await fetch(`${API_URL}/api/v1${path}`, {
      method,
      headers: {
        Accept: "application/json",
        ...(body === undefined ? {} : { "Content-Type": "application/json" }),
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: body === undefined ? undefined : JSON.stringify(body),
      cache: "no-store"
    });
  } catch {
    throw new Error("Não foi possível conectar ao servidor Instrua. Verifique a conexão ou a configuração da API.");
  }
  if (!response.ok) {
    const data = await response.json().catch(() => null) as {message?: string} | null;
    if (response.status === 401 || response.status === 403) throw new Error("Sessão expirada ou acesso não autorizado. Entre novamente.");
    throw new Error(data?.message || `Não foi possível concluir a operação (HTTP ${response.status}).`);
  }
  return await response.json() as T;
}
