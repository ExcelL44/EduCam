package com.excell44.educam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "problem_solutions")
data class ProblemSolution(
    @PrimaryKey
    val id: String,
    val userId: String,
    val imagePath: String? = null,
    val pdfPath: String? = null,
    val recognizedText: String,
    val solution: String,
    val steps: List<String>, // Étapes de résolution
    val subject: String? = null,
    val topic: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

