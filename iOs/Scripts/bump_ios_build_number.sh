#!/usr/bin/env bash
set -euo pipefail

cd "$(git rev-parse --show-toplevel)/iOs"

current=$(grep -m1 'CURRENT_PROJECT_VERSION' project.yml | grep -oE '[0-9]+')
if [[ -z "$current" ]]; then
    echo "Erreur: CURRENT_PROJECT_VERSION introuvable dans project.yml" >&2
    exit 1
fi
next=$((current + 1))

sed -i '' "s/CURRENT_PROJECT_VERSION: \"$current\"/CURRENT_PROJECT_VERSION: \"$next\"/" project.yml

if ! command -v xcodegen >/dev/null 2>&1; then
    echo "Erreur: xcodegen introuvable (brew install xcodegen) — impossible de régénérer DTKTester.xcodeproj" >&2
    exit 1
fi
xcodegen generate --spec project.yml --project .

git add project.yml DTKTester.xcodeproj
echo "Build number iOS: $current -> $next"
