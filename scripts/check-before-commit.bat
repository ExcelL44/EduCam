@echo off
REM Script de vérification avant commit
REM Ce script vérifie le code avant de faire un commit

echo ========================================
echo Vérification avant commit - EduCam
echo ========================================
echo.

REM Vérifier que nous sommes dans le bon répertoire
if not exist "gradlew.bat" (
    echo Erreur: gradlew.bat introuvable. Assurez-vous d'executer ce script depuis la racine du projet.
    exit /b 1
)

set ERROR_COUNT=0

REM Vérifier le formatage Kotlin (optionnel - nécessite ktlint)
echo [1/4] Vérification du formatage Kotlin (optionnel)...
call gradlew.bat ktlintCheck --no-daemon 2>nul
if errorlevel 1 (
    echo INFO: ktlint non configuré ou problèmes de formatage détectés
    echo Pour configurer ktlint, ajoutez le plugin dans build.gradle.kts
) else (
    echo OK: Formatage Kotlin correct
)

REM Vérifier les erreurs de compilation
echo.
echo [2/4] Vérification de la compilation...
call gradlew.bat compileDebugKotlin compileReleaseKotlin --no-daemon
if errorlevel 1 (
    echo ERREUR: Erreurs de compilation détectées!
    set /a ERROR_COUNT+=1
) else (
    echo OK: Compilation réussie
)

REM Exécuter les tests
echo.
echo [3/4] Exécution des tests...
call gradlew.bat test --no-daemon
if errorlevel 1 (
    echo ERREUR: Des tests ont échoué!
    set /a ERROR_COUNT+=1
) else (
    echo OK: Tous les tests passent
)

REM Vérifier le lint Android (si disponible)
echo.
echo [4/4] Vérification du lint Android...
call gradlew.bat lintDebug --no-daemon
if errorlevel 1 (
    echo AVERTISSEMENT: Des problèmes de lint ont été détectés
    echo Consultez: app\build\outputs\lint-results-debug.html
) else (
    echo OK: Lint Android réussi
)

echo.
echo ========================================
if %ERROR_COUNT% GTR 0 (
    echo Vérification terminée avec %ERROR_COUNT% erreur(s)
    echo.
    echo Veuillez corriger les erreurs avant de commiter.
    echo ========================================
    exit /b 1
) else (
    echo Vérification terminée avec succès!
    echo Vous pouvez maintenant commiter vos changements.
    echo ========================================
    exit /b 0
)

