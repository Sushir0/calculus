package com.example.calculus.domain.game

import com.example.calculus.domain.game.model.GameMode
import com.example.calculus.domain.game.model.GameState
import com.example.calculus.domain.game.model.GameSummary
import com.example.calculus.domain.model.MathProblem
import com.example.calculus.domain.model.ProblemAttempt
import com.example.calculus.domain.model.answer.Answer
import com.example.calculus.domain.problemGenerator.ProblemGenerator
import com.example.calculus.domain.repository.GameSummaryRepository
import com.example.calculus.domain.repository.ProblemAttemptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class GameEngine(
    private val gameMode: GameMode,
    private val problemGenerator: ProblemGenerator,
    private val problemAttemptRepository: ProblemAttemptRepository,
    private val gameSummaryRepository: GameSummaryRepository,
) {
    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val sessionHistory = mutableListOf<ProblemAttempt>()

    private var sessionStartTime: Instant? = null
    private var currentProblemStartTime: Instant? = null

    fun startGame() {
        val now = Clock.System.now()
        sessionStartTime = now
        currentProblemStartTime = now
        sessionHistory.clear()

        _gameState.update {
            GameState.Playing(
                currentProblem = problemGenerator.generateProblem(),
                currentRound = 1,
                streak = 0,
                initialTime = Clock.System.now(),
                gameMode = gameMode
            )
        }
    }

    suspend fun submiteAnswer(answer: Answer){
        val currentState = _gameState.value as? GameState.Playing ?: return
        val now = Clock.System.now()

        updateHistory(currentState.currentProblem, answer, now)

        if(shouldEnd(currentState.currentRound, Clock.System.now())){
            endGame(now)
        }else{
            updatePlayingGameState(answer)
        }

    }

    private suspend fun updateHistory(
        problem: MathProblem,
        answer: Answer,
        startedAt: Instant,
    ){
        val problemAttempt = ProblemAttempt(
            problem = problem,
            selectedAnswer = answer,
            startedAt = startedAt,
            finishedAt = Clock.System.now()
        )

        sessionHistory.add(problemAttempt)
        problemAttemptRepository.save(problemAttempt)
    }

    private fun updatePlayingGameState(
        answer: Answer,
    ){
        val startTime = sessionStartTime ?: return
        val currentState = _gameState.value as? GameState.Playing ?: return

        currentProblemStartTime = Clock.System.now()


        val isCorrect = answer is Answer.Correct
        val newStreak = if (isCorrect) currentState.streak + 1 else 0
        val newProblem = problemGenerator.generateProblem()
        val newRound = currentState.currentRound + 1

        _gameState.update {
            GameState.Playing(
                currentProblem = newProblem,
                currentRound = newRound,
                streak = newStreak,
                initialTime = startTime,
                gameMode = gameMode
            )
        }
    }

    private suspend fun endGame(
        finishedAt: Instant,
    ){
        val initialTime = sessionStartTime ?: return
        val gameSummary = GameSummary(
            history = sessionHistory,
            config = problemGenerator.config,
            startedAt = initialTime,
            finishedAt = finishedAt
        )

        _gameState.update { GameState.Finished(gameSummary) }
        gameSummaryRepository.save(gameSummary = gameSummary)
    }



    private fun shouldEnd(
        nextRound: Int,
        currentTimestamp: Instant,
    ): Boolean {
        val startedTime = sessionStartTime ?: return false

        when (gameMode) {
            is GameMode.FixedRounds -> {
                return nextRound >= gameMode.rounds
            }
            is GameMode.Timed -> {
                val totalElapsed = currentTimestamp - startedTime
                return totalElapsed.inWholeSeconds >= gameMode.durationSeconds
            }
            is GameMode.Endless -> {
                return false
            }
        }
    }

}