SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "$SCRIPT")
ROOT_PATH=$(dirname $(dirname "$SCRIPT_PATH"))
DEPLOY_PATH="$ROOT_PATH/deploy/local"

echo "Stopping docker compose and removing deployment."
cd $DEPLOY_PATH
docker compose down
docker volume rm zonedata