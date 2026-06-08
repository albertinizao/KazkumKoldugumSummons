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
    const body = await response.text();
    throw new Error(parseErrorMessage(body, `GET ${path} failed with status ${response.status}`));
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
    const body = await response.text();
    throw new Error(parseErrorMessage(body, `POST ${path} failed with status ${response.status}`));
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
    const body = await response.text();
    throw new Error(parseErrorMessage(body, `DELETE ${path} failed with status ${response.status}`));
  }

  return (await response.json()) as TResult;
}

function parseErrorMessage(body: string, fallback: string): string {
  if (!body) {
    return fallback;
  }

  try {
    const parsed = JSON.parse(body) as { code?: string; message?: string };
    if (parsed.code && parsed.message) {
      return `${parsed.code}: ${parsed.message}`;
    }
    if (parsed.message) {
      return parsed.message;
    }
  } catch {
    // Ignore JSON parse failures and fall back to raw text.
  }

  return body;
}
