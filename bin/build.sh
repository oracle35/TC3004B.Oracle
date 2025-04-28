#!/usr/bin/env bash
set -e

# Directory setup
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}/MtdrSpring"

FRONTEND_DIR="front"
BACKEND_DIR="backend"
FRONTEND_DIST_DIR="${BACKEND_DIR}/target/frontend"

echo "Building Todo List Application..."

# Build frontend
echo "Building frontend..."
cd "${FRONTEND_DIR}"

if [ ! -f .last_build ] || [ "$(find src -type f -newer .last_build | wc -l)" -gt 0 ]; then
  npm install
  npm run build
  touch .last_build
else
  echo "Frontend is up-to-date. Skipping build."
fi

# Create frontend distribution directory
cd ..
mkdir -p "${FRONTEND_DIST_DIR}"

# Copy build files to backend
echo "Copying frontend build to backend..."
cp -r "${FRONTEND_DIR}/dist/"* "${FRONTEND_DIST_DIR}/"

# Load environment variables
echo "Loading environment variables..."
export $(grep -v '^#' "${ROOT_DIR}/.env" | xargs)

# Build and run backend
echo "Building and running backend..."
cd "${BACKEND_DIR}"

# Pass environment variables to Maven
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="\
-Dlogging.level.root=debug \
-Dtelegram.bot.token=${telegram_token} \
-Dtelegram.bot.name=${telegram_name}
-Dspring.datasource.url=jdbc:oracle:thin:@${db_tns_name}?TNS_ADMIN=${ROOT_DIR}/wallet \
-Dspring.datasource.username=${db_user} \
-Dspring.datasource.password=${dbpassword} \
-Dspring.datasource.driver-class-name=${driver_class_name}"

echo "Build complete!"

