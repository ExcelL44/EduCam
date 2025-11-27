#!/bin/bash

# 🧪 Script de lancement rapide des tests d'intégration EduCam
# Usage: ./run-integration-tests.sh

set -e

echo "🚀 EduCam Integration Tests - Quick Start"
echo "=========================================="
echo ""

# Vérifie si un device/émulateur est connecté
echo "📱 Vérification des devices connectés..."
DEVICES=$(adb devices | grep -v "List" | grep "device$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ Aucun device/émulateur détecté!"
    echo "   Veuillez démarrer un émulateur ou connecter un device physique."
    echo ""
    echo "   Depuis Android Studio:"
    echo "   Tools → Device Manager → Run Emulator"
    exit 1
fi

echo "✅ Device détecté"
echo ""

# Synchronise Gradle
echo "🔄 Synchronisation Gradle..."
./gradlew --refresh-dependencies > /dev/null 2>&1 || ./gradlew.bat --refresh-dependencies > /dev/null 2>&1

# Compile l'app
echo "🔨 Compilation de l'app de test..."
./gradlew assembleDebugAndroidTest || ./gradlew.bat assembleDebugAndroidTest

echo ""
echo "🧪 Lancement des tests d'intégration..."
echo "   (Cela peut prendre 5-10 minutes)"
echo ""

# Lance les tests
./gradlew connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.excell44.educam.integration.QuizIntegrationTest \
    || ./gradlew.bat connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.excell44.educam.integration.QuizIntegrationTest

# Résultats
echo ""
echo "=========================================="
echo "✅ Tests terminés!"
echo ""
echo "📊 Rapports disponibles dans:"
echo "   app/build/reports/androidTests/connected/index.html"
echo ""
echo "Pour ouvrir le rapport:"
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "   open app/build/reports/androidTests/connected/index.html"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "   xdg-open app/build/reports/androidTests/connected/index.html"
else
    echo "   start app/build/reports/androidTests/connected/index.html"
fi
