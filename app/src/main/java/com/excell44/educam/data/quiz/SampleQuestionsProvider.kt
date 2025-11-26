package com.excell44.educam.data.quiz

/**
 * Enhanced provider of sample questions for guest users with meaningful educational content.
 * Focuses on Terminale level mathematics and sciences for Cameroonian students.
 */
object SampleQuestionsProvider {

    private val mathQuestions = listOf(
        Question(
            text = "Résoudre l'équation: 2x + 5 = 17",
            subject = "Mathématiques",
            level = "Terminale C",
            hint = "Soustraire 5 des deux membres"
        ) to listOf(
            OptionEntity(questionId = 0, index = 0, text = "x = 16", isCorrect = false),
            OptionEntity(questionId = 0, index = 1, text = "x = 6", isCorrect = true),
            OptionEntity(questionId = 0, index = 2, text = "x = 11", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "x = 2", isCorrect = false)
        ),

        Question(
            text = "Dans un triangle rectangle, d'après le théorème de Pythagore, si les cathètes mesurent 3 cm et 4 cm, l'hypoténuse mesure:",
            subject = "Mathématiques",
            level = "Terminale C",
            hint = "a² + b² = c²"
        ) to listOf(
            OptionEntity(questionId = 0, index = 0, text = "5 cm", isCorrect = true),
            OptionEntity(questionId = 0, index = 1, text = "7 cm", isCorrect = false),
            OptionEntity(questionId = 0, index = 2, text = "1 cm", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "12 cm", isCorrect = false)
        ),

        Question(
            text = "La dérivée de f(x) = x² + 3x + 2 est:",
            subject = "Mathématiques",
            level = "Terminale C",
            hint = "Appliquer les règles de dérivation"
        ) to listOf(
            OptionEntity(questionId = 0, index = 0, text = "f'(x) = 2x + 3", isCorrect = true),
            OptionEntity(questionId = 0, index = 1, text = "f'(x) = x² + 3", isCorrect = false),
            OptionEntity(questionId = 0, index = 2, text = "f'(x) = 2x + 2", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "f'(x) = x² + 3x", isCorrect = false)
        )
    )

    private val physicsQuestions = listOf(
        Question(
            text = "Selon la loi de Coulomb, la force électrique entre deux charges est proportionnelle à:",
            subject = "Physique",
            level = "Terminale C",
            hint = "F = k * q1*q2 / r²"
        ) to listOf(
            OptionEntity(questionId = 0, index = 0, text = "Le produit des charges divisé par le carré de la distance", isCorrect = true),
            OptionEntity(questionId = 0, index = 1, text = "La somme des charges divisée par la distance", isCorrect = false),
            OptionEntity(questionId = 0, index = 2, text = "Le carré des charges multiplié par la distance", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "La différence des charges multipliée par la distance", isCorrect = false)
        ),

        Question(
            text = "Dans un circuit électrique, quelle loi lie l'intensité, la tension et la résistance?",
            subject = "Physique",
            level = "Terminale C",
            hint = "Loi d'Ohm"
        ) to listOf(
            OptionEntity(questionId = 0, index = 0, text = "U = R × I", isCorrect = true),
            OptionEntity(questionId = 0, index = 1, text = "U = R / I", isCorrect = false),
            OptionEntity(questionId = 0, index = 2, text = "U = R + I", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "U = R - I", isCorrect = false)
        ),

        Question(
            text = "Quelle est l'unité SI de la puissance électrique?",
            subject = "Physique",
            level = "Terminale C",
            hint = "Puissance = Travail / Temps"
        ) to listOf(
            OptionEntity(questionId = 0, index = 0, text = "Le Watt (W)", isCorrect = true),
            OptionEntity(questionId = 0, index = 1, text = "Le Volt (V)", isCorrect = false),
            OptionEntity(questionId = 0, index = 2, text = "L'Ampère (A)", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "L'Ohm (Ω)", isCorrect = false)
        )
    )

    private val chemistryQuestions = listOf(
        Question(
            text = "Combien y a-t-il d'électrons dans un atome de carbone neutre?",
            subject = "Chimie",
            level = "Terminale C",
            hint = "Le numéro atomique du carbone est 6"
        ) to listOf(
            OptionEntity(questionId = 0, index = 0, text = "4", isCorrect = false),
            OptionEntity(questionId = 0, index = 1, text = "6", isCorrect = true),
            OptionEntity(questionId = 0, index = 2, text = "8", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "12", isCorrect = false)
        ),

        Question(
            text = "Quelle est la configuration électronique de l'oxygène?",
            subject = "Chimie",
            level = "Terminale C",
            hint = "Numéro atomique 8: 1s² 2s² 2p⁴"
        ) to listOf(
            OptionEntity(questionId = 0, index = 0, text = "1s² 2s² 2p⁴", isCorrect = true),
            OptionEntity(questionId = 0, index = 1, text = "1s² 2s² 2p⁶", isCorrect = false),
            OptionEntity(questionId = 0, index = 2, text = "1s² 2s² 2p²", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "1s² 2s⁴ 2p²", isCorrect = false)
        ),

        Question(
            text = "Dans une réaction acide-base, un acide est une substance qui:",
            subject = "Chimie",
            level = "Terminale C",
            hint = "Donneur de protons"
        ) to listOf(
            OptionEntity(questionId = 0, index = 0, text = "Cède des protons (H⁺)", isCorrect = true),
            OptionEntity(questionId = 0, index = 1, text = "Accepte des protons (H⁺)", isCorrect = false),
            OptionEntity(questionId = 0, index = 2, text = "N'échange pas de protons", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "N'existe pas en solution", isCorrect = false)
        )
    )

    fun sample(): List<Pair<Question, List<OptionEntity>>> {
        return (mathQuestions + physicsQuestions + chemistryQuestions).shuffled()
    }

    /**
     * Get guest-friendly questions - limit to 5 questions per quiz for better guest experience
     */
    fun getGuestQuestions(count: Int = 10): List<Pair<Question, List<OptionEntity>>> {
        return sample().take(count.coerceIn(5, 15)) // Guest gets 5-15 questions max
    }

    /**
     * Get questions by subject for guest mode
     */
    fun getGuestQuestionsBySubject(subject: String, count: Int = 8): List<Pair<Question, List<OptionEntity>>> {
        val subjectQuestions = when(subject.lowercase()) {
            "mathématiques", "math", "maths" -> mathQuestions
            "physique", "physics" -> physicsQuestions
            "chimie", "chemistry" -> chemistryQuestions
            else -> sample() // Mix if subject not recognized
        }
        return subjectQuestions.shuffled().take(count.coerceIn(3, 10))
    }

    /**
     * Get introductory questions for new users (easier level)
     */
    fun getIntroQuestions(): List<Pair<Question, List<OptionEntity>>> {
        return sample().filter { questionPair ->
            // Filter for easier Terminale questions that can serve as introduction
            questionPair.first.difficulty != "Difficile"
        }.take(6)
    }
}
