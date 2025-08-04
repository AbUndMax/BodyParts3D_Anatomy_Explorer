#!/bin/bash

set -e
set -o pipefail

# Der Commit, der als neuer Root dienen soll
ROOT_COMMIT="edebc3f09d939f87e6bf975c33d47904e9683e4c"
NEW_BRANCH="rebase-root"

echo "1. Checking out root commit as orphan branch ..."
git checkout $ROOT_COMMIT

# Orphan branch anlegen (kein Commit, keine Eltern)
git checkout --orphan $NEW_BRANCH

# Entferne alle Files im Arbeitsverzeichnis und Index
git rm -rf . > /dev/null 2>&1 || true

# Checkout exakt den Stand vom gewünschten Commit ins Arbeitsverzeichnis
git checkout $ROOT_COMMIT -- .

# Alles zum Commit hinzufügen und commiten
git add .
git commit -m "Initialize Project (state as of $ROOT_COMMIT)"

echo "2. Switching to main branch and creating patches for all following commits ..."
git checkout main

# Erzeuge Patches aller nachfolgenden Commits (relative zu ROOT_COMMIT)
PATCH_DIR=".patches_temp"
mkdir $PATCH_DIR
git format-patch $ROOT_COMMIT..main -o $PATCH_DIR

echo "3. Switching to new root branch and applying patches ..."
git checkout $NEW_BRANCH

# Patches einzeln anwenden (stoppt bei Konflikt, du musst dann git am --continue machen)
git am --committer-date-is-author-date $PATCH_DIR/*.patch

echo "4. Cleaning up ..."
rm -rf $PATCH_DIR

echo "DONE!"
echo ""
echo "Dein neuer Branch '$NEW_BRANCH' startet mit $ROOT_COMMIT als Root und hat alle nachfolgenden Commits!"
echo "Du kannst jetzt diesen Branch pushen oder als neuen main setzen."
