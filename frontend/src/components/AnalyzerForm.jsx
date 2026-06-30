// Presentational component: renders the input panel. It owns no state — every
// value and change handler comes in as props from the parent (App). This is the
// "controlled component" + "lift state up" pattern.
export default function AnalyzerForm({
  logs,
  setLogs,
  rag,
  setRag,
  severity,
  setSeverity,
  onAnalyze,
  loading,
}) {
  return (
    <div className="panel">
      <label className="lbl">Application logs</label>
      <textarea
        value={logs}
        onChange={(e) => setLogs(e.target.value)}
        placeholder="Paste a stack trace or error log here..."
      />

      <div className="controls">
        <label className="chk">
          <input
            type="checkbox"
            checked={rag}
            onChange={(e) => setRag(e.target.checked)}
          />
          Use RAG (ground in past incidents)
        </label>

        <label className="chk">
          Severity filter:
          <select value={severity} onChange={(e) => setSeverity(e.target.value)}>
            <option value="">any</option>
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="CRITICAL">CRITICAL</option>
          </select>
        </label>

        <button onClick={onAnalyze} disabled={loading}>
          {loading ? 'Analyzing…' : 'Analyze'}
        </button>
      </div>

      <div className="hint">
        Tip: toggle RAG off and on with the same log to compare the difference.
      </div>
    </div>
  )
}
