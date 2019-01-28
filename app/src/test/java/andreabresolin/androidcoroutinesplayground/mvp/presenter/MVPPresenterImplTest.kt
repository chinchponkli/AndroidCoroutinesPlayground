package andreabresolin.androidcoroutinesplayground.mvp.presenter

import andreabresolin.androidcoroutinesplayground.app.domain.task.*
import andreabresolin.androidcoroutinesplayground.app.exception.CustomTaskException
import andreabresolin.androidcoroutinesplayground.app.model.TaskExecutionError
import andreabresolin.androidcoroutinesplayground.app.model.TaskExecutionResult
import andreabresolin.androidcoroutinesplayground.app.model.TaskExecutionState
import andreabresolin.androidcoroutinesplayground.app.model.TaskExecutionState.*
import andreabresolin.androidcoroutinesplayground.app.model.TaskExecutionSuccess
import andreabresolin.androidcoroutinesplayground.base.BaseMockitoTest
import andreabresolin.androidcoroutinesplayground.mvp.view.MVPView
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito.inOrder

class MVPPresenterImplTest : BaseMockitoTest() {

    @Mock
    private lateinit var mockView: MVPView
    @Mock
    private lateinit var mockSequentialTask1: SequentialTaskUseCase
    @Mock
    private lateinit var mockSequentialTask2: SequentialTaskUseCase
    @Mock
    private lateinit var mockSequentialTask3: SequentialTaskUseCase
    @Mock
    private lateinit var mockParallelTask1: ParallelTaskUseCase
    @Mock
    private lateinit var mockParallelTask2: ParallelTaskUseCase
    @Mock
    private lateinit var mockParallelTask3: ParallelTaskUseCase
    @Mock
    private lateinit var mockSequentialErrorTask: SequentialErrorTaskUseCase
    @Mock
    private lateinit var mockParallelErrorTask: ParallelErrorTaskUseCase
    @Mock
    private lateinit var mockMultipleTasks1: MultipleTasksUseCase
    @Mock
    private lateinit var mockMultipleTasks2: MultipleTasksUseCase
    @Mock
    private lateinit var mockMultipleTasks3: MultipleTasksUseCase
    @Mock
    private lateinit var mockCallbackTask1: CallbackTaskUseCase
    @Mock
    private lateinit var mockCallbackTask2: CallbackTaskUseCase
    @Mock
    private lateinit var mockCallbackTask3: CallbackTaskUseCase

    private lateinit var subject: MVPPresenterImpl

    @Before
    fun before() {
        subject = MVPPresenterImpl(
            appCoroutineScope,
            mockView,
            mockSequentialTask1,
            mockSequentialTask2,
            mockSequentialTask3,
            mockParallelTask1,
            mockParallelTask2,
            mockParallelTask3,
            mockSequentialErrorTask,
            mockParallelErrorTask,
            mockMultipleTasks1,
            mockMultipleTasks2,
            mockMultipleTasks3,
            mockCallbackTask1,
            mockCallbackTask2,
            mockCallbackTask3)
    }

    // region Test

    @Test
    fun runSequentialTasks_runsSequentialTasksWithoutError() {
        givenThatSequentialTaskWillReturn(mockSequentialTask1, TaskExecutionSuccess(10))
        givenThatSequentialTaskWillReturn(mockSequentialTask2, TaskExecutionSuccess(20))
        givenThatSequentialTaskWillReturn(mockSequentialTask3, TaskExecutionSuccess(30))
        whenRunSequentialTasks()
        thenTaskStateChangesAre(1, listOf(INITIAL, RUNNING, COMPLETED))
        thenTaskStateChangesAre(2, listOf(INITIAL, RUNNING, COMPLETED))
        thenTaskStateChangesAre(3, listOf(INITIAL, RUNNING, COMPLETED))
        thenNoMoreInteractionsWithView()
    }

    @Test
    fun runParallelTasks_runsParallelTasksWithoutError() {
        givenThatParallelTaskWillReturn(mockParallelTask1, TaskExecutionSuccess(10))
        givenThatParallelTaskWillReturn(mockParallelTask2, TaskExecutionSuccess(20))
        givenThatParallelTaskWillReturn(mockParallelTask3, TaskExecutionSuccess(30))
        whenRunParallelTasks()
        thenTaskStateChangesAre(1, listOf(INITIAL, RUNNING, COMPLETED))
        thenTaskStateChangesAre(2, listOf(INITIAL, RUNNING, COMPLETED))
        thenTaskStateChangesAre(3, listOf(INITIAL, RUNNING, COMPLETED))
        thenNoMoreInteractionsWithView()
    }

    @Test
    fun runSequentialTasksWithError_runsSequentialTasksWithError() {
        givenThatSequentialTaskWillReturn(mockSequentialTask1, TaskExecutionSuccess(10))
        givenThatSequentialErrorTaskWillReturn(mockSequentialErrorTask, TaskExecutionError(CustomTaskException()))
        givenThatSequentialTaskWillReturn(mockSequentialTask3, TaskExecutionSuccess(30))
        whenRunSequentialTasksWithError()
        thenTaskStateChangesAre(1, listOf(INITIAL, RUNNING, COMPLETED))
        thenTaskStateChangesAre(2, listOf(INITIAL, RUNNING, ERROR))
        thenTaskStateChangesAre(3, listOf(INITIAL, RUNNING, COMPLETED))
        thenNoMoreInteractionsWithView()
    }

    @Test
    fun runParallelTasksWithError_runsParallelTasksWithError() {
        givenThatParallelTaskWillReturn(mockParallelTask1, TaskExecutionSuccess(10))
        givenThatParallelErrorTaskWillReturn(mockParallelErrorTask, TaskExecutionError(CustomTaskException()))
        givenThatParallelTaskWillReturn(mockParallelTask3, TaskExecutionSuccess(30))
        whenRunParallelTasksWithError()
        thenTaskStateChangesAre(1, listOf(INITIAL, RUNNING, COMPLETED))
        thenTaskStateChangesAre(2, listOf(INITIAL, RUNNING, ERROR))
        thenTaskStateChangesAre(3, listOf(INITIAL, RUNNING, COMPLETED))
        thenNoMoreInteractionsWithView()
    }

    @Test
    fun runMultipleTasks_runsMultipleTasksWithoutError() {
        givenThatMultipleTasksWillReturn(mockMultipleTasks1, TaskExecutionSuccess(10))
        givenThatMultipleTasksWillReturn(mockMultipleTasks2, TaskExecutionSuccess(20))
        givenThatMultipleTasksWillReturn(mockMultipleTasks3, TaskExecutionSuccess(30))
        whenRunMultipleTasks()
        thenTaskStateChangesAre(1, listOf(INITIAL, RUNNING, COMPLETED))
        thenTaskStateChangesAre(2, listOf(INITIAL, RUNNING, COMPLETED))
        thenTaskStateChangesAre(3, listOf(INITIAL, RUNNING, COMPLETED))
        thenNoMoreInteractionsWithView()
    }

    @Test
    fun runCallbackTasksWithError_runsCallbackTasksWithError() {
        givenThatCallbackTaskWillReturn(mockCallbackTask1, TaskExecutionSuccess(10))
        givenThatCallbackTaskWillReturn(mockCallbackTask2, TaskExecutionError(CustomTaskException()))
        givenThatCallbackTaskWillReturn(mockCallbackTask3, TaskExecutionError(CustomTaskException()))
        whenRunCallbackTasksWithError()
        thenTaskStateChangesAre(1, listOf(INITIAL, RUNNING, COMPLETED))
        thenTaskStateChangesAre(2, listOf(INITIAL, RUNNING, ERROR))
        thenTaskStateChangesAre(3, listOf(INITIAL, RUNNING, ERROR))
        thenNoMoreInteractionsWithView()
    }

    // endregion Test

    // region Given

    private fun givenThatSequentialTaskWillReturn(sequentialTask: SequentialTaskUseCase,
                                                  taskExecutionResult: TaskExecutionResult) = runBlocking {
        given(sequentialTask.execute(anyLong(), anyLong(), anyLong())).willReturn(taskExecutionResult)
    }

    private fun givenThatParallelTaskWillReturn(parallelTask: ParallelTaskUseCase,
                                                taskExecutionResult: TaskExecutionResult) = runBlocking {
        given(parallelTask.executeAsync(anyLong(), anyLong(), anyLong())).willReturn(CompletableDeferred(taskExecutionResult))
    }

    private fun givenThatSequentialErrorTaskWillReturn(sequentialErrorTask: SequentialErrorTaskUseCase,
                                                       taskExecutionResult: TaskExecutionResult) = runBlocking {
        given(sequentialErrorTask.execute(anyLong(), anyLong(), anyLong())).willReturn(taskExecutionResult)
    }

    private fun givenThatParallelErrorTaskWillReturn(parallelErrorTask: ParallelErrorTaskUseCase,
                                                     taskExecutionResult: TaskExecutionResult) = runBlocking {
        given(parallelErrorTask.executeAsync(anyLong(), anyLong(), anyLong())).willReturn(CompletableDeferred(taskExecutionResult))
    }

    private fun givenThatMultipleTasksWillReturn(multipleTasks: MultipleTasksUseCase,
                                                 taskExecutionResult: TaskExecutionResult) = runBlocking {
        given(multipleTasks.execute(anyLong(), anyLong(), anyLong())).willReturn(taskExecutionResult)
    }

    private fun givenThatCallbackTaskWillReturn(callbackTask: CallbackTaskUseCase,
                                                taskExecutionResult: TaskExecutionResult) = runBlocking {
        given(callbackTask.execute(anyString())).willReturn(taskExecutionResult)
    }

    // endregion Given

    // region When

    private fun whenRunSequentialTasks() {
        subject.runSequentialTasks()
    }

    private fun whenRunParallelTasks() {
        subject.runParallelTasks()
    }

    private fun whenRunSequentialTasksWithError() {
        subject.runSequentialTasksWithError()
    }

    private fun whenRunParallelTasksWithError() {
        subject.runParallelTasksWithError()
    }

    private fun whenRunMultipleTasks() {
        subject.runMultipleTasks()
    }

    private fun whenRunCallbackTasksWithError() {
        subject.runCallbackTasksWithError()
    }

    // endregion When

    // region Then

    private fun thenTaskStateChangesAre(taskNumber: Int, states: List<TaskExecutionState>) {
        val inOrder = inOrder(mockView)

        states.forEach { state ->
            then(mockView).should(inOrder).updateTaskExecutionState(taskNumber, state)
        }
    }

    private fun thenNoMoreInteractionsWithView() {
        then(mockView).shouldHaveNoMoreInteractions()
    }

    // endregion Then
}