package com.example.unscramble.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.unscramble.data.SCORE_INCREASE
import kotlinx.coroutines.flow.update

class GameViewModel: ViewModel(){

    private val _uiState = MutableStateFlow(GameUiState())
    //creating backing property to access value from outside
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow() //asStateFlow mmakes mutable stateFlow to read only

    private lateinit var currentWord: String

    // Set of words used in the game
    //keeping the list to confirm the words should not be repeated in the game
    private var usedWords: MutableSet<String> = mutableSetOf()

    var userGuess by mutableStateOf("")

    private val _questionCount = MutableStateFlow(0)
    val questionCount: StateFlow<Int> = _questionCount.asStateFlow()

    init {
        resetGame()
    }


    private fun pickRandomWordAndShuffle(): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        return if (usedWords.contains(currentWord)) {
            pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(currentWord: String): String{
        //logic for scrambling word
        val tempWord = currentWord.toCharArray()
        //scrambled word should not be equal to the current word
        while (String(tempWord) == currentWord) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun resetGame(){
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord
    }

    fun checkUserGuess() {

        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateUserState(updatedScore)

        } else {
            // User's guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    private fun updateUserState(updatedScore: Int){
        _uiState.update { currentState ->
            currentState.copy( isGuessedWordWrong = false,
                currentWordCount = currentState.currentWordCount.inc(),
                currentScrambledWord = pickRandomWordAndShuffle(),
                score = updatedScore)
        }
    }
    fun submitWord(): Boolean{
        if(userGuess == currentWord){
//            pickNextWord()
            questionCount.value.plus(1)
            return true
        }else{
            return false
        }
    }
}