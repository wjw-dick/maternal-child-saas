#!/usr/bin/env node
/**
 * 母婴亲子 SaaS - 轻量并发压测（无第三方依赖）
 * 用法：
 *   BASE=http://127.0.0.1:18080 TENANT=demo REQUESTS=200 CONCURRENCY=20 node deploy/bench-node.mjs
 */
const BASE = process.env.BASE || 'http://127.0.0.1:8080'
const TENANT = process.env.TENANT || 'demo'
const USERNAME = process.env.USERNAME || 'admin'
const PASSWORD = process.env.PASSWORD || 'admin123'
const REQUESTS = Number(process.env.REQUESTS || 200)
const CONCURRENCY = Number(process.env.CONCURRENCY || 20)

async function login() {
  const res = await fetch(`${BASE}/api/tenant/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: USERNAME, password: PASSWORD, tenantCode: TENANT })
  })
  const body = await res.json()
  if (body.code !== 0) throw new Error(`登录失败: ${body.msg}`)
  return body.data.token
}

async function runOne(url, token) {
  const start = performance.now()
  let ok = false
  try {
    const res = await fetch(url, { headers: { Authorization: `Bearer ${token}`, 'X-Tenant-Code': TENANT } })
    const body = await res.json()
    ok = res.ok && body.code === 0
  } catch {
    ok = false
  }
  return { ms: performance.now() - start, ok }
}

function percentile(sorted, p) {
  if (!sorted.length) return 0
  const index = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1)
  return sorted[index]
}

async function bench(name, path, token) {
  const url = `${BASE}${path}`
  const latencies = []
  let errors = 0
  let next = 0
  const started = performance.now()

  async function worker() {
    while (true) {
      const i = next++
      if (i >= REQUESTS) return
      const result = await runOne(url, token)
      latencies.push(result.ms)
      if (!result.ok) errors++
    }
  }

  await Promise.all(Array.from({ length: CONCURRENCY }, worker))
  const elapsed = performance.now() - started
  latencies.sort((a, b) => a - b)
  const avg = latencies.reduce((sum, v) => sum + v, 0) / (latencies.length || 1)
  console.log(
    [
      name.padEnd(12),
      `req=${REQUESTS}`,
      `c=${CONCURRENCY}`,
      `qps=${((REQUESTS / elapsed) * 1000).toFixed(1)}`,
      `avg=${avg.toFixed(1)}ms`,
      `p95=${percentile(latencies, 95).toFixed(1)}ms`,
      `p99=${percentile(latencies, 99).toFixed(1)}ms`,
      `err=${errors}`
    ].join('  ')
  )
  return { errors, avg, p95: percentile(latencies, 95) }
}

const token = await login()
console.log(`BASE=${BASE} TENANT=${TENANT}`)
const cases = [
  ['公开服务', '/api/public/services'],
  ['租户看板', '/api/tenant/dashboard'],
  ['会员列表', '/api/tenant/members?page=1&size=10'],
  ['订单列表', '/api/tenant/orders?page=1&size=10'],
  ['预约列表', '/api/tenant/appointments?page=1&size=10']
]
const results = []
for (const [name, path] of cases) {
  results.push(await bench(name, path, token))
}
const failed = results.reduce((sum, r) => sum + r.errors, 0)
console.log(failed === 0 ? 'RESULT=PASS' : `RESULT=FAIL errors=${failed}`)
