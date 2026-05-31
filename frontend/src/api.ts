export type User = {
  userId: number;
  departmentId: number;
  departmentName: string;
  loginId: string;
  employeeName: string;
  roleCode: "ADMIN" | "USER";
};

export type Dashboard = {
  todayWorkReportCount: number;
  currentMonthTotalHours: number;
  notOutputMonthlyReportCount: number;
  recentActivities: {
    activityAt: string;
    activityTypeName: string;
    badgeClass: string;
    content: string;
    employeeName: string;
  }[];
};

export type WorkReportResult = {
  workDate: string;
  employeeName: string;
  departmentName: string;
  projectName: string;
  workCategory: string;
  workCategoryName: string;
  workHours: number;
  workContent: string;
};

export type Department = {
  departmentId: number;
  departmentCode: string;
  departmentName: string;
  displayOrder: number;
};

export type MasterUser = {
  userId: number;
  departmentId: number;
  departmentName: string;
  loginId: string;
  employeeName: string;
  roleCode: "ADMIN" | "USER";
};

export type ReportHistory = {
  reportOutputHistoryId: number;
  targetYearMonth: string;
  targetEmployeeName: string;
  createdByEmployeeName: string;
  reportType: string;
  fileName: string;
  status: string;
  errorMessage?: string;
  createdAt: string;
};

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:5000/api";

let csrfToken = "";

async function ensureCsrf(): Promise<string> {
  if (csrfToken) {
    return csrfToken;
  }
  const response = await fetch(`${API_BASE}/auth/csrf`, { credentials: "include" });
  if (!response.ok) {
    throw new Error("CSRFトークンを取得できませんでした。");
  }
  const data = (await response.json()) as { token: string };
  csrfToken = data.token;
  return csrfToken;
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (init.method && init.method !== "GET") {
    headers.set("X-CSRF-TOKEN", await ensureCsrf());
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers,
    credentials: "include"
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `APIエラー: ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export const api = {
  login: async (loginId: string, password: string) => {
    const user = await request<User>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ loginId, password })
    });
    csrfToken = "";
    return user;
  },
  logout: async () => {
    await request<void>("/auth/logout", { method: "POST" });
    csrfToken = "";
  },
  me: () => request<User>("/auth/me"),
  dashboard: () => request<Dashboard>("/dashboard"),
  targetUsers: () => request<User[]>("/monthly-reports/target-users"),
  departments: () => request<Department[]>("/master/departments"),
  createDepartment: (payload: unknown) =>
    request<Department>("/master/departments", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  updateDepartment: (id: number, payload: unknown) =>
    request<Department>(`/master/departments/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    }),
  masterUsers: () => request<MasterUser[]>("/master/users"),
  createMasterUser: (payload: unknown) =>
    request<MasterUser>("/master/users", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  updateMasterUser: (id: number, payload: unknown) =>
    request<MasterUser>(`/master/users/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    }),
  registerWorkReport: (payload: unknown) =>
    request<{ workReportId: number }>("/work-reports", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  searchWorkReports: (query: Record<string, string>) => {
    const params = new URLSearchParams(query);
    return request<WorkReportResult[]>(`/work-reports?${params.toString()}`);
  },
  histories: (query: Record<string, string>) => {
    const params = new URLSearchParams(query);
    return request<ReportHistory[]>(`/report-histories?${params.toString()}`);
  },
  exportMonthlyReport: async (payload: { targetYearMonth: string; targetUserId: number }) => {
    const response = await fetch(`${API_BASE}/monthly-reports/export`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-CSRF-TOKEN": await ensureCsrf()
      },
      credentials: "include",
      body: JSON.stringify(payload)
    });
    if (!response.ok) {
      throw new Error(await response.text());
    }
    return downloadBlob(response);
  },
  downloadHistory: async (id: number) => {
    const response = await fetch(`${API_BASE}/report-histories/${id}/download`, {
      credentials: "include"
    });
    if (!response.ok) {
      throw new Error(await response.text());
    }
    return downloadBlob(response);
  }
};

async function downloadBlob(response: Response) {
  const blob = await response.blob();
  const disposition = response.headers.get("content-disposition") ?? "";
  const matched = /filename\*=UTF-8''([^;]+)|filename="?([^"]+)"?/i.exec(disposition);
  const fileName = decodeURIComponent(matched?.[1] ?? matched?.[2] ?? "monthly-report.xlsx");
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = fileName;
  anchor.click();
  URL.revokeObjectURL(url);
}
