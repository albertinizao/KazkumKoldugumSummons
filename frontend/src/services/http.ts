const JSON_HEADERS = {
  'Content-Type': 'application/json',
} as const;

async function parseError(response: Response, path: string, method: string): Promise<never> {
  const body = await response.text();
  throw new Error(body || `${method} ${path} failed with status ${response.status}`);
}

export async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path, {
    headers: {
      Accept: 'application/json',
    },
  });

  if (!response.ok) {
    await parseError(response, path, 'GET');
  }

  return (await response.json()) as T;
}

export async function postJson<TBody extends object | undefined, TResult>(
  path: string,
  body?: TBody,
): Promise<TResult> {
  const response = await fetch(path, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!response.ok) {
    await parseError(response, path, 'POST');
  }

  return (await response.json()) as TResult;
}

export async function deleteJson<TResult>(path: string): Promise<TResult> {
  const response = await fetch(path, {
    method: 'DELETE',
    headers: {
      Accept: 'application/json',
    },
  });

  if (!response.ok) {
    await parseError(response, path, 'DELETE');
  }

  return (await response.json()) as TResult;
}
