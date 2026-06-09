export type FieldErrors = Record<string, string>;

export function required(value: string | number | null | undefined, label: string) {
  return value === undefined || value === null || String(value).trim() === "" ? `${label}不能为空` : "";
}

export function maxLength(value: string | undefined, max: number, label: string) {
  return value && value.length > max ? `${label}不能超过 ${max} 个字符` : "";
}

export function phone(value: string | undefined) {
  if (!value) return "";
  return /^[0-9+\-\s()]{6,40}$/.test(value) ? "" : "联系电话格式不正确";
}

export function positive(value: string | number | undefined, label: string) {
  const num = Number(value);
  return Number.isFinite(num) && num > 0 ? "" : `${label}必须大于 0`;
}

export function nonNegative(value: string | number | undefined, label: string) {
  if (value === undefined || value === "") return "";
  const num = Number(value);
  return Number.isFinite(num) && num >= 0 ? "" : `${label}不能为负数`;
}

export function positiveInteger(value: string | number | undefined, label: string) {
  if (value === undefined || value === "") return "";
  const num = Number(value);
  return Number.isInteger(num) && num > 0 ? "" : `${label}必须为正整数`;
}

export function pastOrToday(value: string | undefined, label: string) {
  if (!value) return "";
  const today = new Date();
  const limit = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
  return new Date(value).getTime() <= limit ? "" : `${label}不能晚于今天`;
}

export function collectErrors(entries: Array<[string, string]>) {
  return entries.reduce<FieldErrors>((acc, [key, message]) => {
    if (message) acc[key] = message;
    return acc;
  }, {});
}
