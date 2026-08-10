#!/usr/bin/env bash
set -euo pipefail

cd "$(git rev-parse --show-toplevel)/Android"

current=$(grep -m1 'versionCode = ' app/build.gradle.kts | grep -oE '[0-9]+')
if [[ -z "$current" ]]; then
    echo "Erreur: versionCode introuvable dans app/build.gradle.kts" >&2
    exit 1
fi
next=$((current + 1))

sed -i '' "s/versionCode = $current/versionCode = $next/" app/build.gradle.kts

git add app/build.gradle.kts
echo "Build number Android: $current -> $next"
