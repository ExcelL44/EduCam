# Script PowerShell pour setup local du projet EduCam
# À exécuter après git clone

Write-Host "🔧 Setup Local EduCam - Configuration Firebase" -ForegroundColor Cyan
Write-Host ""

$googleServicesPath = "google-services.json"

if (Test-Path $googleServicesPath) {
    Write-Host "✅ google-services.json déjà présent" -ForegroundColor Green
    
    # Vérifier taille du fichier (doit être > 100 bytes)
    $fileSize = (Get-Item $googleServicesPath).Length
    if ($fileSize -lt 100) {
        Write-Host "⚠️  Fichier semble invalide (taille: $fileSize bytes)" -ForegroundColor Yellow
        Write-Host "Action recommandée: Télécharger depuis Firebase Console" -ForegroundColor Yellow
    } else {
        Write-Host "   Taille: $fileSize bytes - OK" -ForegroundColor Green
    }
} else {
    Write-Host "❌ google-services.json non trouvé" -ForegroundColor Red
    Write-Host ""
    Write-Host "📋 Options pour obtenir le fichier:" -ForegroundColor Yellow
    Write-Host "1. Télécharger depuis Firebase Console:"
    Write-Host "   https://console.firebase.google.com/project/educam-prod/settings/general"
    Write-Host ""
    Write-Host "2. Demander à un membre de l'équipe"
    Write-Host ""
    Write-Host "3. Créer version MOCK (tests locaux uniquement)"
    Write-Host ""
    
    $choice = Read-Host "Créer version MOCK pour tests ? (O/N)"
    
    if ($choice -eq "O" -or $choice -eq "o") {
        $mockJson = @"
{
  "project_info": {
    "project_number": "123456789012",
    "firebase_url": "https://educam-mock.firebaseio.com",
    "project_id": "educam-mock",
    "storage_bucket": "educam-mock.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789012:android:abcdef1234567890",
        "android_client_info": {
          "package_name": "com.excell44.educam"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "AIzaSyMOCK_KEY_FOR_LOCAL_TESTS_ONLY"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
"@
        
        $mockJson | Out-File -FilePath $googleServicesPath -Encoding UTF8 -NoNewline
        Write-Host ""
        Write-Host "✅ Fichier MOCK créé avec succès" -ForegroundColor Green
        Write-Host "⚠️  ATTENTION: Firebase non fonctionnel (offline-first uniquement)" -ForegroundColor Yellow
    } else {
        Write-Host ""
        Write-Host "❌ Setup annulé - Téléchargez google-services.json manuellement" -ForegroundColor Red
        Write-Host "   Placez-le dans: app\google-services.json" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host ""
Write-Host "🎉 Setup terminé !" -ForegroundColor Green
