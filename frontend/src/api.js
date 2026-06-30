// Data layer: all calls to the backend live here so UI components never deal
// with URLs, fetch, or JSON. If the API changes, you only touch this file.

export async function analyzeLogs({ logs, rag, severity }) {
  let url = `/api/logs/analyze?rag=${rag}`
  if (severity) url += `&severity=${encodeURIComponent(severity)}`

  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ logs }),
  })
  if (!res.ok) throw new Error('HTTP ' + res.status)
  return res.json()
}
