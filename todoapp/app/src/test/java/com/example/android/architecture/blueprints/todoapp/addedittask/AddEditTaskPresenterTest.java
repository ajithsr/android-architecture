/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.addedittask;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.GetTask;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.SaveTask;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Scheduler;
import rx.schedulers.Schedulers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link AddEditTaskPresenter}.
 */
public class AddEditTaskPresenterTest {

    @Mock
    private AddEditTaskContract.View mAddEditTaskView;

    private AddEditTaskPresenter mAddEditTaskPresenter;
    private TasksRepository fakeTasksRepository;

    @Before
    public void setupMocksAndView() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // The presenter wont't update the view unless it's active.
        when(mAddEditTaskView.isActive()).thenReturn(true);
    }

    @Test
    public void saveNewTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = givenEditTaskPresenter(null);

        // When the presenter is asked to save a task
        mAddEditTaskPresenter.saveTask("New Task Title", "Some Task Description");

        // Then a task is saved in the repository and the view updated
        verify(mAddEditTaskView).showTasksList(); // shown in the UI
    }


    @Test
    public void saveTask_emptyTaskShowsErrorUi() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = givenEditTaskPresenter(null);

        // When the presenter is asked to save an empty task
        mAddEditTaskPresenter.saveTask("", "");

        // Then an empty not error is shown in the UI
        verify(mAddEditTaskView).showEmptyTaskError();
    }

    @Test
    public void saveExistingTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = givenEditTaskPresenter("1");

        // When the presenter is asked to save an existing task
        mAddEditTaskPresenter.saveTask("New Task Title", "Some Task Description");

        // Then a task is saved in the repository and the view updated
        verify(mAddEditTaskView).showTasksList(); // shown in the UI
    }

    @Test
    public void populateTask_callsRepoAndUpdatesView() {
        Task testTask = new Task("TITLE", "DESCRIPTION");
        // Get a reference to the class under test
        mAddEditTaskPresenter = givenEditTaskPresenter(testTask.getId());
        givenFakeRepository(testTask);

        // When the presenter is asked to populate an existing task
        mAddEditTaskPresenter.populateTask();

        verify(mAddEditTaskView).setTitle(testTask.getTitle());
        verify(mAddEditTaskView).setDescription(testTask.getDescription());
    }

    private void givenFakeRepository(Task testTask) {
        fakeTasksRepository.saveTask(testTask);
    }

    private AddEditTaskPresenter givenEditTaskPresenter(String taskId) {
        Scheduler fakeScheduler = Schedulers.immediate();

        fakeTasksRepository = Injection.provideFakeTasksRepository();
        GetTask getTask = new GetTask(fakeScheduler, fakeScheduler, fakeTasksRepository);
        SaveTask saveTask = new SaveTask(fakeScheduler, fakeScheduler, fakeTasksRepository);

        return new AddEditTaskPresenter(taskId, mAddEditTaskView, getTask,
              saveTask);
    }
}
