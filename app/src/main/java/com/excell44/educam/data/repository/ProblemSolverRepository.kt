package com.excell44.educam.data.repository

import com.excell44.educam.data.dao.ProblemSolutionDao
import com.excell44.educam.data.model.ProblemSolution
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProblemSolverRepository @Inject constructor(
    private val problemSolutionDao: ProblemSolutionDao
) {
    suspend fun saveSolution(solution: ProblemSolution) {
        problemSolutionDao.insertSolution(solution)
    }

    suspend fun solveProblem(
        userId: String,
        recognizedText: String,
        imagePath: String? = null,
        pdfPath: String? = null,
        gradeLevel: String
    ): ProblemSolution {
        // TODO: Intégrer avec une API ML ou un modèle local pour résoudre le problème
        // Pour l'instant, on simule une solution
        if (recognizedText.isBlank()) {
            throw IllegalArgumentException("Le texte reconnu ne peut pas être vide")
        }
        
        val solution = generateSolution(recognizedText, gradeLevel)
        val steps = generateSolutionSteps(recognizedText, gradeLevel)
        
        val problemSolution = ProblemSolution(
            id = UUID.randomUUID().toString(),
            userId = userId,
            imagePath = imagePath,
            pdfPath = pdfPath,
            recognizedText = recognizedText,
            solution = solution,
            steps = steps,
            subject = detectSubject(recognizedText),
            topic = detectTopic(recognizedText)
        )
        
        try {
            problemSolutionDao.insertSolution(problemSolution)
        } catch (e: Exception) {
            // Log l'erreur mais continue quand même
            // En production, utiliser un système de logging
        }
        
        return problemSolution
    }

    fun getSolutionsByUser(userId: String): Flow<List<ProblemSolution>> {
        return problemSolutionDao.getSolutionsByUser(userId)
    }

    private fun generateSolution(text: String, gradeLevel: String): String {
        // Simulation - À remplacer par une vraie IA
        return "Solution détaillée basée sur le problème reconnu et le niveau $gradeLevel"
    }

    private fun generateSolutionSteps(text: String, gradeLevel: String): List<String> {
        // Simulation - À remplacer par une vraie IA
        return listOf(
            "Étape 1: Analyser le problème",
            "Étape 2: Identifier les données connues",
            "Étape 3: Appliquer les formules appropriées",
            "Étape 4: Résoudre étape par étape",
            "Étape 5: Vérifier la solution"
        )
    }

    private fun detectSubject(text: String): String? {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("équation") || lowerText.contains("calcul") || lowerText.contains("math") -> "Math"
            lowerText.contains("force") || lowerText.contains("énergie") || lowerText.contains("physique") -> "Physics"
            lowerText.contains("réaction") || lowerText.contains("molécule") || lowerText.contains("chimie") -> "Chemistry"
            else -> null
        }
    }

    private fun detectTopic(text: String): String? {
        // Détection basique du sujet
        return null
    }
}

