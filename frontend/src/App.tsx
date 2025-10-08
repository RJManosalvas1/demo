import { useEffect, useMemo, useState } from 'react'

// === Tipos ===
type Product = { id: number; name: string; price: number }

// === Helpers ===
function useLatency() {
    const [ms, setMs] = useState<number | null>(null)
    const measure = async <T,>(fn: () => Promise<T>) => {
        const t0 = performance.now()
        const data = await fn()
        setMs(performance.now() - t0)
        return data
    }
    return { ms, measure }
}

const money = (v: number) =>
    v.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })

export default function App() {
    // estado
    const [items, setItems] = useState<Product[]>([])
    const [loading, setLoading] = useState(false)
    const [newName, setNewName] = useState('')
    const [newPrice, setNewPrice] = useState('9.99')
    const [quote, setQuote] = useState<string>('—')
    const [msg, setMsg] = useState<string>('')        // mensajes para productos
    const [qErr, setQErr] = useState<string>('')      // error para la cita

    // métricas
    const { ms: listMs, measure: measureList } = useLatency()
    const { ms: quoteMs, measure: measureQuote } = useLatency()

    // Base API: usa variable de entorno si existe; si no, usa el proxy (/api)
    const API = useMemo(() => {
        const base = import.meta.env.VITE_API_BASE
        return base ? `${base}/api` : '/api'
    }, [])

    // ====== Productos ======
    const load = async () => {
        setMsg('')
        setLoading(true)
        try {
            const data = await measureList(async () => {
                const r = await fetch(`${API}/products`)
                if (!r.ok) throw new Error(`GET /products -> ${r.status} ${r.statusText}`)
                return (await r.json()) as Product[]
            })
            setItems(data)
        } catch (e: any) {
            setMsg(e?.message ?? String(e))
        } finally {
            setLoading(false)
        }
    }

    const add = async () => {
        setMsg('')
        const id = Date.now()
        const name = newName.trim() || `Producto ${id}`
        const price = Number.isFinite(+newPrice) ? +newPrice : 9.99

        try {
            const r = await fetch(`${API}/products`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id, name, price }),
            })
            if (!r.ok) {
                const t = await r.text()
                throw new Error(`POST /products -> ${r.status} ${r.statusText}. ${t}`)
            }
            setNewName('')
            setNewPrice('9.99')
            await load()
        } catch (e: any) {
            setMsg(e?.message ?? String(e))
        }
    }

    const remove = async (id: number) => {
        setMsg('')
        try {
            const r = await fetch(`${API}/products/${id}`, { method: 'DELETE' })
            if (!r.ok) throw new Error(`DELETE /products/${id} -> ${r.status} ${r.statusText}`)
            await load()
        } catch (e: any) {
            setMsg(e?.message ?? String(e))
        }
    }

    // ====== Cita (Proxy) ======
    const getQuote = async () => {
        setQErr('')
        setQuote('cargando...')
        try {
            const data = await measureQuote(async () => {
                const r = await fetch(`${API}/quotes`, { headers: { Accept: 'text/plain' } })
                if (!r.ok) throw new Error(`GET /quotes -> ${r.status} ${r.statusText}`)
                return await r.text()
            })
            setQuote(data || '(respuesta vacia)')
        } catch (e: any) {
            setQuote('—')
            setQErr(e?.message ?? String(e))
        }
    }

    useEffect(() => { load() }, [])

    return (
        <main style={{ fontFamily: 'system-ui', padding: 24, maxWidth: 900, margin: '0 auto' }}>
            <h1>Aplicación Demo — Spring Cache + Proxy</h1>
            <p style={{ opacity: 0.7 }}>
                Backend en <code>localhost:8080</code> — Frontend Vite React (proxy evita CORS)
            </p>

            {/* Productos */}
            <section style={{ border: '1px solid #ddd', borderRadius: 12, padding: 16, marginTop: 24 }}>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <h2>Productos (con caché)</h2>
                    {listMs != null && <small style={{ opacity: 0.6 }}>⏱ {Math.round(listMs)} ms</small>}
                </div>

                <div style={{ marginTop: 10, display: 'flex', gap: 8 }}>
                    <input placeholder="Nombre" value={newName} onChange={e => setNewName(e.target.value)} />
                    <input placeholder="Precio" value={newPrice} onChange={e => setNewPrice(e.target.value)} />
                    <button onClick={add}>Añadir</button>
                    <button onClick={load} disabled={loading}>{loading ? 'Cargando...' : 'Refrescar'}</button>
                </div>

                <ul style={{ marginTop: 12 }}>
                    {items.map(p => (
                        <li key={p.id}>
                            <b>{p.name}</b> — ${money(p.price)}
                            <button onClick={() => remove(p.id)} style={{ marginLeft: 8 }}>Eliminar</button>
                        </li>
                    ))}
                    {items.length === 0 && <i>No hay productos aún.</i>}
                </ul>

                {msg && <p style={{ color: '#e11d48', marginTop: 8 }}>Error: {msg}</p>}

                <p style={{ opacity: 0.7 }}>
                    La primera consulta tarda más (sin caché). Las siguientes usan el <b>caché</b> del backend.
                </p>
            </section>

            {/* Cita (Proxy) */}
            <section style={{ border: '1px solid #ddd', borderRadius: 12, padding: 16, marginTop: 24 }}>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <h2>Cita del día (Proxy)</h2>
                    {quoteMs != null && <small style={{ opacity: 0.6 }}>⏱ {Math.round(quoteMs)} ms</small>}
                </div>

                <button onClick={getQuote} style={{ marginTop: 10 }}>Obtener cita</button>
                <pre style={{ marginTop: 12, background: '#f9f9f9', padding: 12, borderRadius: 8 }}>{quote}</pre>
                {qErr && <p style={{ color: '#e11d48', marginTop: 8 }}>Error: {qErr}</p>}

                <p style={{ opacity: 0.7 }}>
                    El <b>Proxy</b> obtiene la cita desde una API externa y guarda la respuesta en caché.
                </p>
            </section>
        </main>
    )
}
