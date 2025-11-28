package com.excell44.educam

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ✅ Test instrumenté de base pour vérifier que l'environnement fonctionne
 * 
 * Ce test s'exécute sur un device Android ou émulateur.
 * Il sert de smoke test pour s'assurer que les tests instrumentés
 * peuvent s'exécuter correctement dans le CI/CD.
 */
@RunWith(AndroidJUnit4::class)
class BasicInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context de l'app sous test
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Vérifier que le package est correct
        assertEquals("com.excell44.educam.debug", appContext.packageName)
    }

    @Test
    fun smokeTest_appLaunches() {
        // Test simple pour vérifier que l'environnement de test fonctionne
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Vérifier que le context n'est pas null
        assert(appContext != null)
    }
}
