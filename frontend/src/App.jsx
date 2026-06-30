import { useState } from 'react'
import { analyzeLogs } from './api.js'
import AnalyzerForm from './components/AnalyzerForm.jsx'
import ResultCard from './components/ResultCard.jsx'

// Container component: owns all the state and the "what happens on Analyze"
// logic, then composes the presentational components. The children stay dumb
// and reusable; this is the only place that knows how the pieces fit together.
export default function App() {
  const [logs, setLogs] = useState('')
  const [rag, setRag] = useState(true)
  const [severity, setSeverity] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleAnalyze() {
    if (!logs.trim()) {
      setError('Please paste some logs first.')
      return
    }
    setError('')
    setResult(null)
    setLoading(true)
    try {
      setResult(await analyzeLogs({ logs, rag, severity }))
    } catch (e) {
      setError('Request failed: ' + e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="wrap">
      <h1>🔍 Log Analyzer AI</h1>
      <p className="sub">
        Paste application logs — Groq diagnoses the error and suggests a fix,
        grounded in past incidents.
      </p>

      <AnalyzerForm
        logs={logs}
        setLogs={setLogs}
        rag={rag}
        setRag={setRag}
        severity={severity}
        setSeverity={setSeverity}
        onAnalyze={handleAnalyze}
        loading={loading}
      />

      {error && <div className="err">{error}</div>}
      {result && <ResultCard result={result} />}
    </div>
  )
}
