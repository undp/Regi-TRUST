SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "$SCRIPT")
ROOT_PATH=$(dirname $(dirname "$SCRIPT_PATH"))
DEPLOY_PATH="$ROOT_PATH/deploy/local"

# build dns docker image
if [ $1 = "build" ]; then
  echo "Switching context to dns building... ($ROOT_PATH)"
  cd "$ROOT_PATH"
  docker build -t tdzm-nsd:latest .

  # build ui docker image
  echo "Switching context to ui building... ($ROOT_PATH/ui)"
  cd "$ROOT_PATH/ui"
  docker build -t tdzm-ui:latest .
else 
  echo "No build flag. Skipping build. To build images run this script as 'deploy.sh build'"
fi

echo "Docker image building has been finished. Starting dns deployment."

echo "Switching context to deployment ($DEPLOY_PATH)"
cd "$DEPLOY_PATH"
docker compose up -d