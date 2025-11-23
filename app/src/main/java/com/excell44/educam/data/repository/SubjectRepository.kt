package com.excell44.educam.data.repository

import com.excell44.educam.data.dao.SubjectDao
import com.excell44.educam.data.model.Subject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepository @Inject constructor(
    private val subjectDao: SubjectDao
) {
    fun getSubjectsBySubjectAndGrade(subject: String, gradeLevel: String): Flow<List<Subject>> {
        return subjectDao.getSubjectsBySubjectAndGrade(subject, gradeLevel)
    }

    suspend fun getSubjectById(id: String): Subject? {
        return try {
            subjectDao.getSubjectById(id)
        } catch (e: Exception) {
            null // Retourner null en cas d'erreur
        }
    }

    fun getAllSubjects(): Flow<List<String>> {
        return subjectDao.getAllSubjects()
    }
}

