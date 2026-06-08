async function request(path: string, options: RequestInit = {}): Promise<Response> {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
    ...options,
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(parseErrorMessage(body, `Request failed: ${response.status}`));
  }

  return response;
}

export async function getConfiguration(): Promise<{ maxSummonMonsterLevel: number }> {
  const response = await request('/api/configuration');
  return response.json();
}

export async function saveConfiguration(
  maxSummonMonsterLevel: number,
  dailyUsesMaximum: number,
): Promise<{ maxSummonMonsterLevel: number; dailyUses: { maximum: number; remaining: number } }> {
  const response = await request('/api/configuration', {
    method: 'PUT',
    body: JSON.stringify({ maxSummonMonsterLevel, dailyUsesMaximum }),
  });
  return response.json();
}

export async function getSummonQuantity(creatureSummonLevel: number): Promise<{
  available: boolean;
  formula: string;
  maximumPossibleQuantity: number;
}> {
  const response = await request(`/api/summons/quantity?creatureSummonLevel=${encodeURIComponent(creatureSummonLevel)}`);
  return response.json();
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
    // Ignore JSON parse failures and return the raw body.
  }

  return body;
}
