package com.example.myworldapp2.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Класс, представляющий викторину вместе со всеми связанными данными
 */
data class QuizWithDetails(
    @Embedded
    val quiz: Quiz,
    
    // Связанная статья
    @Relation(
        parentColumn = "entryId",
        entityColumn = "id"
    )
    val entry: Entry?,
    
    // Вопросы викторины с вариантами ответов
    @Relation(
        entity = QuizQuestion::class,
        parentColumn = "id",
        entityColumn = "quizId"
    )
    val questions: List<QuestionWithAnswers>,
    
    // Результат пользователя (может быть null, если викторина не проходилась)
    var userResult: UserQuizResult? = null
)

/**
 * Класс, представляющий вопрос с вариантами ответов
 */
data class QuestionWithAnswers(
    @Embedded
    val question: QuizQuestion,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "questionId"
    )
    val answers: List<QuizAnswer>
) 