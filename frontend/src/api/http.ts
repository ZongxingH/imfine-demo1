import type { ApiErrorResponse } from "../types";

type QueryValue = string | number | boolean | null | undefined;

export class ApiClientError extends Error {
  code: string;
  details?: ApiErrorResponse["details"];
  status: number;

  constructor(status: number, error: ApiErrorResponse) {
    super(error.message || "请求失败");
    this.name = "ApiClientError";
    this.status = status;
    this.code = error.code || "HTTP_ERROR";
    this.details = error.details;
  }
}

const API_BASE = "/api";

function buildUrl(path: string, query?: object) {
  const url = new URL(`${API_BASE}${path}`, window.location.origin);
  (Object.entries(query ?? {}) as Array<[string, QueryValue]>).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      url.searchParams.set(key, String(value));
    }
  });
  return `${url.pathname}${url.search}`;
}

export async function requestJson<T>(
  path: string,
  options: RequestInit = {},
  query?: object
): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(buildUrl(path, query), {
    ...options,
    headers
  });
  const text = await response.text();
  const parsed = text ? JSON.parse(text) : undefined;

  if (!response.ok) {
    throw new ApiClientError(response.status, parsed ?? { code: "HTTP_ERROR", message: response.statusText });
  }

  return parsed as T;
}

export function errorMessage(error: unknown) {
  if (error instanceof ApiClientError) {
    return `${error.message}${error.code ? `（${error.code}）` : ""}`;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "请求失败，请稍后重试";
}
