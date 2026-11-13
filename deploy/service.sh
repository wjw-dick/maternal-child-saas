#!/bin/bash
# =============================================================================
# service.sh — 应用服务管理脚本（部署 v2 规范）
# 用法: ./service.sh {start|stop|restart|status|enable|disable}
#   start    启动（后台 nohup，PID 写入 app.pid，日志 app.log）
#   stop     停止（先 SIGTERM 优雅退出，超时 30s 后 SIGKILL）
#   restart  重启
#   status   查看进程/端口/最近日志
#   enable   注册 systemd 开机自启
#   disable  取消开机自启
# 部署时请替换下方 APP_NAME / APP_DIR / JAVA_HOME 三个变量。
# =============================================================================
APP_NAME="maternal-child"                 # 例如 bicycle
APP_DIR="/home/app/maternal-child.quantaida.cloud"                   # 应用目录（含 app.jar / .env / logs），例如 /home/app/bicycle.quantaida.cloud
JAR="$APP_DIR/app.jar"
PID_FILE="$APP_DIR/app.pid"
LOG_DIR="$APP_DIR/logs"
LOG="$LOG_DIR/app.log"
JAVA_HOME="/www/server/java/jdk-17.0.8" # 服务器 JDK17 路径（上线时核实）
JAVA="$JAVA_HOME/bin/java"
JVM_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"
PORT=18081                           # 后端监听端口（用于 status 展示）

is_running() {
  [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null
}

start() {
  if is_running; then
    echo "ALREADY_RUNNING pid=$(cat "$PID_FILE")"
    return 0
  fi
  [ -f "$JAR" ] || { echo "ERROR: $JAR not found"; exit 1; }
  [ -f "$APP_DIR/.env" ] || { echo "ERROR: $APP_DIR/.env not found"; exit 1; }
  mkdir -p "$LOG_DIR"
  set -a; . "$APP_DIR/.env"; set +a
  cd "$APP_DIR"
  nohup "$JAVA" $JVM_OPTS -jar "$JAR" > "$LOG" 2>&1 &
  echo $! > "$PID_FILE"
  echo "STARTED pid=$(cat "$PID_FILE")"
}

stop() {
  if ! is_running; then
    echo "NOT_RUNNING"
    rm -f "$PID_FILE"
    return 0
  fi
  PID=$(cat "$PID_FILE")
  kill "$PID" 2>/dev/null
  for i in $(seq 1 30); do
    kill -0 "$PID" 2>/dev/null || break
    sleep 1
  done
  if kill -0 "$PID" 2>/dev/null; then
    echo "graceful timeout, force kill"
    kill -9 "$PID" 2>/dev/null
  fi
  rm -f "$PID_FILE"
  echo "STOPPED"
}

restart() { stop; sleep 2; start; }

status() {
  echo "app:      $APP_NAME"
  echo "pid:      $(cat "$PID_FILE" 2>/dev/null || echo none)"
  if is_running; then
    echo "running:  YES"
    ps -p "$(cat "$PID_FILE")" -o pid,etime,%cpu,%mem,cmd --no-headers 2>/dev/null
  else
    echo "running:  NO"
  fi
  echo "port:     $(ss -lntp 2>/dev/null | grep -q ":$PORT" && echo "LISTENING :$PORT" || echo "not listening")"
  echo "lastlog:"
  tail -n 5 "$LOG" 2>/dev/null
}

enable() {
  [ -n "$APP_NAME" ] && [ "$APP_NAME" != "__PLACEHOLDER_APP_NAME__" ] || { echo "ERROR: set APP_NAME first"; exit 1; }
  cat > "/etc/systemd/system/$APP_NAME.service" <<EOF
[Unit]
Description=$APP_NAME java service
After=network.target

[Service]
Type=forking
WorkingDirectory=$APP_DIR
PIDFile=$PID_FILE
ExecStart=$APP_DIR/service.sh start
ExecStop=$APP_DIR/service.sh stop
ExecReload=$APP_DIR/service.sh restart
Restart=on-failure
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
EOF
  systemctl daemon-reload
  systemctl enable "$APP_NAME"
  echo "ENABLED systemd service $APP_NAME"
}

disable() {
  systemctl disable "$APP_NAME" 2>/dev/null
  rm -f "/etc/systemd/system/$APP_NAME.service"
  systemctl daemon-reload
  echo "DISABLED"
}

case "$1" in
  start) start ;;
  stop) stop ;;
  restart) restart ;;
  status) status ;;
  enable) enable ;;
  disable) disable ;;
  *) echo "usage: $0 {start|stop|restart|status|enable|disable}"; exit 1 ;;
esac
