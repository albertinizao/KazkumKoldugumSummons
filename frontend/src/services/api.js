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
    throw new Error(body || `Request failed: ${response.status}`)
  }

  return response
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
