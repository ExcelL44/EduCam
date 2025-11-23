@echo off
REM Script de build local pour le projet EduCam
REM Ce script nettoie, compile et génère les APK

echo ========================================
echo Build Local - EduCam
echo ========================================
echo.

REM Vérifier que nous sommes dans le bon répertoire
if not exist "gradlew.bat" (
    echo Erreur: gradlew.bat introuvable. Assurez-vous d'executer ce script depuis la racine du projet.
    exit /b 1
)

REM Arrêter les daemons Gradle existants
echo [1/5] Arrêt des daemons Gradle...
call gradlew.bat --stop
if errorlevel 1 (
    echo Avertissement: Erreur lors de l'arrêt des daemons
)

REM Nettoyer le projet
echo.
echo [2/5] Nettoyage du projet...
call gradlew.bat clean
if errorlevel 1 (
    echo Erreur: Le nettoyage a échoué
    exit /b 1
)

REM Compiler le projet
echo.
echo [3/5] Compilation du projet...
call gradlew.bat build --no-daemon
if errorlevel 1 (
    echo Erreur: La compilation a échoué
    exit /b 1
)

REM Exécuter les tests
echo.
echo [4/5] Exécution des tests...
call gradlew.bat test --no-daemon
if errorlevel 1 (
    echo Avertissement: Certains tests ont échoué
)

REM Générer les APK
echo.
echo [5/5] Génération des APK...
call gradlew.bat assembleDebug assembleRelease --no-daemon
if errorlevel 1 (
    echo Erreur: La génération des APK a échoué
    exit /b 1
)

echo.
echo ========================================
echo Build terminé avec succès!
echo ========================================
echo.
echo APK Debug:   app\build\outputs\apk\debug\
echo APK Release: app\build\outputs\apk\release\
echo.

pause

