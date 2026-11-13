#!/usr/bin/env bash
# ============================================================
# 母婴亲子 SaaS - MySQL 每日备份
# 用法：/bin/bash /www/wwwroot/mc-saas/deploy/backup.sh
# 计划任务：每天 02:30
# 保留：BACKUP_KEEP 份（默认 7）
# ============================================================
set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${APP_DIR}/.env"
if [[ -f "${ENV_FILE}" ]]; then
  # shellcheck disable=SC1090
  set -a; source "${ENV_FILE}"; set +a
fi

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-maternal_child}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
BACKUP_DIR="${BACKUP_DIR:-/www/backup/mc-saas}"
BACKUP_KEEP="${BACKUP_KEEP:-7}"

mkdir -p "${BACKUP_DIR}"
STAMP="$(date +%Y%m%d_%H%M%S)"
TARGET="${BACKUP_DIR}/${DB_NAME}_${STAMP}.sql.gz"
LOG="${BACKUP_DIR}/backup.log"

log() { echo "[$(date '+%F %T')] $*" | tee -a "${LOG}"; }

log "开始备份 ${DB_NAME} -> ${TARGET}"

MYSQL_PWD="${DB_PASSWORD}" mysqldump \
  --host="${DB_HOST}" --port="${DB_PORT}" --user="${DB_USER}" \
  --single-transaction --quick --routines --triggers \
  --default-character-set=utf8mb4 --no-tablespaces \
  "${DB_NAME}" | gzip -9 > "${TARGET}"

if [[ ! -s "${TARGET}" ]]; then
  log "备份失败：文件为空"
  exit 1
fi
if ! gzip -t "${TARGET}"; then
  log "备份失败：gzip 校验不通过"
  exit 1
fi

SIZE="$(du -h "${TARGET}" | awk '{print $1}')"
log "备份完成，大小 ${SIZE}"

# 仅保留最近 N 份（只删除本目录下匹配命名规则的历史备份）
mapfile -t OLD < <(ls -1t "${BACKUP_DIR}/${DB_NAME}"_*.sql.gz 2>/dev/null | tail -n +$((BACKUP_KEEP + 1)) || true)
if (( ${#OLD[@]} > 0 )); then
  for f in "${OLD[@]}"; do
    log "清理历史备份：${f}"
    rm -f -- "${f}"
  done
fi

log "当前保留 $(ls -1 "${BACKUP_DIR}/${DB_NAME}"_*.sql.gz 2>/dev/null | wc -l) 份备份"
