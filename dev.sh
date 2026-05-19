#!/bin/zsh
case "${1:-}" in
  seed)
    shift
    docker compose -f docker-compose.dev.yml run --rm -e SPRING_PROFILES_ACTIVE=seed user-service "$@"
    ;;
  *)
    docker compose -f docker-compose.dev.yml "$@"
    ;;
esac
