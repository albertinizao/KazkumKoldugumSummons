const JSON_HEADERS = {
  'Content-Type': 'application/json',
} as const;

export async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path, {
    headers: {
      Accept: 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`GET ${path} failed with status ${response.status}`);
  }

  return (await response.json()) as T;
}

export async function postJson<TBody extends Record<string, unknown> | undefined, TResult>(
  path: string,
  body?: TBody,
): Promise<TResult> {
  const response = await fetch(path, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!response.ok) {
    throw new Error(`POST ${path} failed with status ${response.status}`);
  }

  return (await response.json()) as TResult;
}
