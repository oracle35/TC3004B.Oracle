#!/usr/bin/env bash
set -e

SESSION_NAME="dev-session"

# Root directory
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="${ROOT_DIR}/MtdrSpring/front"
BACKEND_DIR="${ROOT_DIR}/MtdrSpring/backend"

# Create tmux session
tmux new-session -d -s "${SESSION_NAME}" -n dev

# --- Pane 0: Frontend ---
# Changed window name to 'dev' and pane 0 will be frontend
tmux rename-window -t "${SESSION_NAME}:0" frontend

# Run frontend dev server
tmux send-keys -t "${SESSION_NAME}:frontend.0" "cd ${FRONTEND_DIR} && npm install && npm run dev" C-m

# --- Pane 1: Backend ---
# Split horizontally and run backend
tmux split-window -h -t "${SESSION_NAME}:frontend" # Split the 'frontend' window
tmux send-keys -t "${SESSION_NAME}:frontend.1" "cd ${BACKEND_DIR}" C-m

# Load env vars and run Spring Boot
tmux send-keys -t "${SESSION_NAME}:frontend.1" "export \$(grep -v '^#' ${ROOT_DIR}/.env | xargs)" C-m
tmux send-keys -t "${SESSION_NAME}:frontend.1" "./mvnw spring-boot:run \\
  -Dspring-boot.run.jvmArguments=\"\\
  -Dtelegram.bot.token=\${telegram_token} \\
  -Dtelegram.bot.name=\${telegram_name} \\
  -Dspring.datasource.url=jdbc:oracle:thin:@\${db_tns_name}?TNS_ADMIN=${ROOT_DIR}/wallet \\
  -Dspring.datasource.username=\${db_user} \\
  -Dspring.datasource.password=\${dbpassword} \\
  -Dspring.datasource.driver-class-name=\${driver_class_name}\"" C-m

# Attach to session
tmux attach-session -t "${SESSION_NAME}"