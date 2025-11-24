package com.excell44.educam.data.quiz

/**
 * Small provider of sample questions used for development and testing.
 */
object SampleQuestionsProvider {
    fun sample(): List<Pair<Question, List<OptionEntity>>> {
        val list = mutableListOf<Pair<Question, List<OptionEntity>>>()

        val q1 = Question(text = "Quel est le résultat de 2+2?", subject = "Math", level = "Tle C", hint = "Addition basique")
        val o1 = listOf(
            OptionEntity(questionId = 0, index = 0, text = "3", isCorrect = false),
            OptionEntity(questionId = 0, index = 1, text = "4", isCorrect = true),
            OptionEntity(questionId = 0, index = 2, text = "22", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "0", isCorrect = false)
        )
        list.add(q1 to o1)

        val q2 = Question(text = "Quelle est la formule de la vitesse moyenne?", subject = "Physique", level = "1ère C", hint = "v = d / t")
        val o2 = listOf(
            OptionEntity(questionId = 0, index = 0, text = "d / t", isCorrect = true),
            OptionEntity(questionId = 0, index = 1, text = "t / d", isCorrect = false),
            OptionEntity(questionId = 0, index = 2, text = "d * t", isCorrect = false),
            OptionEntity(questionId = 0, index = 3, text = "aucune de ces réponses", isCorrect = false)
        )
        list.add(q2 to o2)

        // add a few placeholder questions
        for (i in 3..10) {
            val q = Question(text = "Question sample $i : exemple", subject = "Math", level = "Tle C", hint = "Indice sample")
            val opts = (0..3).map { idx -> OptionEntity(questionId = 0, index = idx, text = "Option ${idx + 1}", isCorrect = idx == 0) }
            list.add(q to opts)
        }

        return list
    }
}
