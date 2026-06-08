async function request(path, options = {}) {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {})
    },
    ...options
  })

  if (!response.ok) {
    const body = await response.text()
    throw new Error(parseErrorMessage(body, `Request failed: ${response.status}`))
  }

  return response
}

function parseErrorMessage(body, fallback) {
  if (!body) {
    return fallback
  }

  try {
    const parsed = JSON.parse(body)
    if (parsed.code && parsed.message) {
      return `${parsed.code}: ${parsed.message}`
    }
    if (parsed.message) {
      return parsed.message
    }
  } catch (error) {
    // Ignore parse failures and return the raw body.
  }

  return body
}

export async function getConfiguration() {
  const response = await request('/api/configuration')
  return response.json()
}

export async function saveConfiguration(maxSummonMonsterLevel) {
  const response = await request('/api/configuration', {
    method: 'PUT',
    body: JSON.stringify({ maxSummonMonsterLevel })
  })
  return response.json()
}

export async function getSummonQuantity(creatureSummonLevel) {
  const response = await request(`/api/summons/quantity?creatureSummonLevel=${encodeURIComponent(creatureSummonLevel)}`)
  return response.json()
}
