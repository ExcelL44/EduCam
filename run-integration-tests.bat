@echo off
REM 🧪 Script Windows pour lancer les tests d'intégration EduCam
REM Usage: run-integration-tests.bat

echo 🚀 EduCam Integration Tests - Quick Start
echo ==========================================
echo.

REM Vérifie si un device/émulateur est connecté
echo 📱 Vérification des devices connectés...
adb devices | find "device" > nul
if errorlevel 1 (
    echo ❌ Aucun device/émulateur détecté!
    echo    Veuillez démarrer un émulateur ou connecter un device physique.
    echo.
    echo    Depuis Android Studio:
    echo    Tools → Device Manager → Run Emulator
    exit /b 1
)

echo ✅ Device détecté
echo.

REM Synchronise Gradle
echo 🔄 Synchronisation Gradle...
call gradlew.bat --refresh-dependencies > nul 2>&1

REM Compile l'app
echo 🔨 Compilation de l'app de test...
call gradlew.bat assembleDebugAndroidTest

echo.
echo 🧪 Lancement des tests d'intégration...
echo    (Cela peut prendre 5-10 minutes)
echo.

REM Lance les tests
call gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.excell44.educam.integration.QuizIntegrationTest

REM Résultats
echo.
echo ==========================================
echo ✅ Tests terminés!
echo.
echo 📊 Rapports disponibles dans:
echo    app\build\reports\androidTests\connected\index.html
echo.
echo Pour ouvrir le rapport:
echo    start app\build\reports\androidTests\connected\index.html

pause
