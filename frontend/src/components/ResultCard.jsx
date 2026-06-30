import SeverityBadge from './SeverityBadge.jsx'

// The fields to render, in display order. Keeping this as data (not repeated
// JSX) means adding/reordering a field is a one-line change.
const FIELDS = [
  ['severity', 'Severity'],
  ['summary', 'Summary'],
  ['errorType', 'Error type'],
  ['rootCause', 'Root cause'],
  ['affectedComponent', 'Affected component'],
  ['suggestedFix', 'Suggested fix'],
]

// Presentational component: given a result object, render the rows.
export default function ResultCard({ result }) {
  return (
    <div className="panel result">
      {FIELDS.map(([key, label]) => (
        <div className="row" key={key}>
          <div className="rkey">{label}</div>
          <div className="rval">
            {key === 'severity' ? (
              <SeverityBadge severity={result.severity} />
            ) : (
              result[key] || ''
            )}
          </div>
        </div>
      ))}
    </div>
  )
}
