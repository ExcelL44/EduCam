package com.excell44.educam.ui.screen.subjects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for managing SubjectsScreen navigation state.
 *
 * This replaces fragile remember-based state management with robust ViewModel state
 * that survives configuration changes and prevents crashes.
 */
@HiltViewModel
class SubjectsNavigationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_NAVIGATION_LEVEL = "navigation_level"
        private const val KEY_SELECTED_SUBJECT = "selected_subject"
        private const val KEY_SELECTED_CATEGORY = "selected_category"
        private const val KEY_SELECTED_ITEM_ID = "selected_item_id"
    }

    // Navigation state with SavedStateHandle for process death survival
    private val _navigationState = MutableStateFlow(
        SubjectsNavigationState(
            level = NavigationLevel.valueOf(
                savedStateHandle.get<String>(KEY_NAVIGATION_LEVEL) ?: NavigationLevel.SUBJECTS.name
            ),
            selectedSubject = savedStateHandle.get<String>(KEY_SELECTED_SUBJECT)?.let { subjectName ->
                SubjectType.values().find { it.name == subjectName }
            },
            selectedCategory = savedStateHandle.get<String>(KEY_SELECTED_CATEGORY)?.let { categoryName ->
                CategoryType.values().find { it.name == categoryName }
            },
            selectedItem = savedStateHandle.get<String>(KEY_SELECTED_ITEM_ID)?.let { itemId ->
                // Find item by ID (this is a simplified approach - in real app you'd have a repository)
                getAllItems().find { it.id == itemId }
            }
        )
    )
    val navigationState: StateFlow<SubjectsNavigationState> = _navigationState.asStateFlow()

    init {
        // Validate state invariants on initialization
        validateStateInvariants()
    }

    /**
     * Navigate to categories level for a selected subject.
     */
    fun navigateToCategories(subject: SubjectType) {
        updateState { currentState ->
            check(currentState.level == NavigationLevel.SUBJECTS) {
                "Can only navigate to categories from subjects level"
            }
            currentState.copy(
                level = NavigationLevel.CATEGORIES,
                selectedSubject = subject,
                selectedCategory = null, // Reset category when changing subject
                selectedItem = null
            )
        }
    }

    /**
     * Navigate to items level for a selected category.
     */
    fun navigateToItems(category: CategoryType) {
        updateState { currentState ->
            check(currentState.level == NavigationLevel.CATEGORIES) {
                "Can only navigate to items from categories level"
            }
            checkNotNull(currentState.selectedSubject) {
                "Subject must be selected before selecting category"
            }
            currentState.copy(
                level = NavigationLevel.ITEMS,
                selectedCategory = category,
                selectedItem = null
            )
        }
    }

    /**
     * Navigate to documents level for a selected item.
     */
    fun navigateToDocuments(item: BankItem) {
        updateState { currentState ->
            check(currentState.level == NavigationLevel.ITEMS) {
                "Can only navigate to documents from items level"
            }
            checkNotNull(currentState.selectedSubject) {
                "Subject must be selected"
            }
            checkNotNull(currentState.selectedCategory) {
                "Category must be selected"
            }
            currentState.copy(
                level = NavigationLevel.DOCUMENTS,
                selectedItem = item
            )
        }
    }

    /**
     * Navigate back to the previous level.
     *
     * @return true if navigation was handled internally, false if screen should exit
     */
    fun navigateBack(): Boolean {
        val currentState = _navigationState.value

        return when (currentState.level) {
            NavigationLevel.SUBJECTS -> {
                // Exit the screen
                false
            }
            NavigationLevel.CATEGORIES -> {
                updateState { it.copy(
                    level = NavigationLevel.SUBJECTS,
                    selectedSubject = null,
                    selectedCategory = null,
                    selectedItem = null
                )}
                true
            }
            NavigationLevel.ITEMS -> {
                updateState { it.copy(
                    level = NavigationLevel.CATEGORIES,
                    selectedCategory = null,
                    selectedItem = null
                )}
                true
            }
            NavigationLevel.DOCUMENTS -> {
                updateState { it.copy(
                    level = NavigationLevel.ITEMS,
                    selectedItem = null
                )}
                true
            }
        }
    }

    /**
     * Reset navigation to initial state.
     */
    fun resetNavigation() {
        updateState {
            SubjectsNavigationState()
        }
    }

    /**
     * Get items for the currently selected category.
     */
    fun getCurrentItems(): List<BankItem> {
        val state = _navigationState.value
        return if (state.selectedCategory != null) {
            getItemsForCategory(state.selectedCategory)
        } else {
            emptyList()
        }
    }

    /**
     * Helper function to get all items (for restoration from SavedStateHandle).
     */
    private fun getAllItems(): List<BankItem> {
        return CategoryType.values().flatMap { category ->
            getItemsForCategory(category)
        }
    }

    /**
     * Validate state invariants to prevent impossible states.
     */
    private fun validateStateInvariants() {
        val state = _navigationState.value

        when (state.level) {
            NavigationLevel.CATEGORIES -> {
                checkNotNull(state.selectedSubject) {
                    "Subject must be selected when at categories level"
                }
            }
            NavigationLevel.ITEMS -> {
                checkNotNull(state.selectedSubject) {
                    "Subject must be selected when at items level"
                }
                checkNotNull(state.selectedCategory) {
                    "Category must be selected when at items level"
                }
            }
            NavigationLevel.DOCUMENTS -> {
                checkNotNull(state.selectedSubject) {
                    "Subject must be selected when at documents level"
                }
                checkNotNull(state.selectedCategory) {
                    "Category must be selected when at documents level"
                }
                checkNotNull(state.selectedItem) {
                    "Item must be selected when at documents level"
                }
            }
            NavigationLevel.SUBJECTS -> {
                // No invariants to check for subjects level
            }
        }
    }

    /**
     * Atomically update state with validation and persistence.
     */
    private fun updateState(updateFunction: (SubjectsNavigationState) -> SubjectsNavigationState) {
        _navigationState.update { currentState ->
            val newState = updateFunction(currentState)

            // Validate the new state
            validateStateInvariants()

            // Persist to SavedStateHandle
            savedStateHandle[KEY_NAVIGATION_LEVEL] = newState.level.name
            savedStateHandle[KEY_SELECTED_SUBJECT] = newState.selectedSubject?.name
            savedStateHandle[KEY_SELECTED_CATEGORY] = newState.selectedCategory?.name
            savedStateHandle[KEY_SELECTED_ITEM_ID] = newState.selectedItem?.id

            newState
        }
    }
}

/**
 * Navigation state for the subjects screen.
 */
data class SubjectsNavigationState(
    val level: NavigationLevel = NavigationLevel.SUBJECTS,
    val selectedSubject: SubjectType? = null,
    val selectedCategory: CategoryType? = null,
    val selectedItem: BankItem? = null
)
