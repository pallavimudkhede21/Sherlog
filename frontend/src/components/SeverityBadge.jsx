// A tiny presentational component: takes a severity string, renders a colored
// pill. Reusable anywhere we show a severity.
export default function SeverityBadge({ severity }) {
  const sev = String(severity || '').toUpperCase()
  return <span className={'badge ' + sev}>{sev || '—'}</span>
}
