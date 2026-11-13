#!/usr/bin/env bash
# ============================================================
# 母婴亲子 SaaS - MySQL 恢复脚本（需二次确认）
# 用法：/bin/bash deploy/restore.sh /www/backup/mc-saas/maternal_child_YYYYMMDD_HHMMSS.sql.gz
# 说明：恢复会覆盖当前库数据，执行前自动生成一份安全备份。
# ============================================================
set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${APP_DIR}/.env"
if [[ -f "${ENV_FILE}" ]]; then
  # shellcheck disable=SC1090
  set -a; source "${ENV_FILE}"; set +a
fi

BACKUP_FILE="${1:-}"
if [[ -z "${BACKUP_FILE}" || ! -f "${BACKUP_FILE}" ]]; then
  echo "用法：$0 /path/to/maternal_child_YYYYMMDD_HHMMSS.sql.gz"
  exit 1
fi
if ! gzip -t "${BACKUP_FILE}"; then
  echo "错误：备份文件 gzip 校验失败"
  exit 1
fi

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-maternal_child}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
BACKUP_DIR="${BACKUP_DIR:-/www/backup/mc-saas}"

echo "即将恢复数据库：${DB_NAME}@${DB_HOST}:${DB_PORT}"
echo "恢复文件：${BACKUP_FILE}"
echo "警告：该操作会覆盖现有数据！"
read -r -p "请输入 RESTORE 确认：" CONFIRM
if [[ "${CONFIRM}" != "RESTORE" ]]; then
  echo "已取消"
  exit 0
fi

mkdir -p "${BACKUP_DIR}"
SAFETY="${BACKUP_DIR}/${DB_NAME}_before_restore_$(date +%Y%m%d_%H%M%S).sql.gz"
echo "生成恢复前安全备份：${SAFETY}"
MYSQL_PWD="${DB_PASSWORD}" mysqldump \
  --host="${DB_HOST}" --port="${DB_PORT}" --user="${DB_USER}" \
  --single-transaction --quick --routines --triggers \
  --default-character-set=utf8mb4 --no-tablespaces \
  "${DB_NAME}" | gzip -9 > "${SAFETY}"

echo "开始恢复..."
gunzip -c "${BACKUP_FILE}" | MYSQL_PWD="${DB_PASSWORD}" mysql \
  --host="${DB_HOST}" --port="${DB_PORT}" --user="${DB_USER}" \
  --default-character-set=utf8mb4 "${DB_NAME}"

echo "恢复完成，请重启后端并执行验收："
echo "  systemctl restart mc-saas"
echo "  curl -fsS https://api.quantaida.cloud/api/public/config"
