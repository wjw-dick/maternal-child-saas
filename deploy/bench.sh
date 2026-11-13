#!/usr/bin/env bash
# ============================================================
# 母婴亲子 SaaS - 关键接口压测
# 依赖：curl、jq、ab（apache2-utils）或 wrk
# 用法：BASE=https://api.quantaida.cloud TENANT=demo /bin/bash deploy/bench.sh
# ============================================================
set -euo pipefail

BASE="${BASE:-http://127.0.0.1:8080}"
TENANT="${TENANT:-demo}"
USERNAME="${USERNAME:-admin}"
PASSWORD="${PASSWORD:-admin123}"
DURATION="${DURATION:-30s}"
THREADS="${THREADS:-4}"
CONNECTIONS="${CONNECTIONS:-50}"
REQUESTS="${REQUESTS:-1000}"

command -v curl >/dev/null || { echo "缺少 curl"; exit 1; }
command -v jq >/dev/null || { echo "缺少 jq"; exit 1; }

echo "== 1. 登录获取 token =="
LOGIN="$(curl -fsS -X POST "${BASE}/api/tenant/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\",\"tenantCode\":\"${TENANT}\"}")"
TOKEN="$(echo "${LOGIN}" | jq -r '.data.token')"
if [[ -z "${TOKEN}" || "${TOKEN}" == "null" ]]; then
  echo "登录失败：${LOGIN}"
  exit 1
fi
echo "token 获取成功"

bench() {
  local name="$1" url="$2"
  echo
  echo "== ${name} =="
  echo "GET ${url}"
  if command -v wrk >/dev/null; then
    wrk -t"${THREADS}" -c"${CONNECTIONS}" -d"${DURATION}" \
      -H "Authorization: Bearer ${TOKEN}" -H "X-Tenant-Code: ${TENANT}" "${url}"
  elif command -v ab >/dev/null; then
    ab -n "${REQUESTS}" -c "${CONNECTIONS}" \
      -H "Authorization: Bearer ${TOKEN}" -H "X-Tenant-Code: ${TENANT}" "${url}"
  else
    echo "请安装 wrk 或 ab（apache2-utils）"
    return 1
  fi
}

bench "公开服务列表" "${BASE}/api/public/services"
bench "租户看板"   "${BASE}/api/tenant/dashboard"
bench "会员列表"   "${BASE}/api/tenant/members?page=1&size=10"
bench "订单列表"   "${BASE}/api/tenant/orders?page=1&size=10"
bench "预约列表"   "${BASE}/api/tenant/appointments?page=1&size=10"

echo
echo "== 完成 =="
echo "关注指标：QPS、P95/P99、错误率、Hikari 连接池、Redis 命中率、RabbitMQ 队列积压"
