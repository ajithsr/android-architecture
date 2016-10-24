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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.UseCaseHandler;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.GetTask;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.SaveTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;

import rx.Subscriber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link AddEditTaskFragment}), retrieves the data and
 * updates
 * the UI as required.
 */
public class AddEditTaskPresenter implements AddEditTaskContract.Presenter {

    private final AddEditTaskContract.View mAddTaskView;

    private final GetTask mGetTask;

    private final SaveTask mSaveTask;

    private final UseCaseHandler mUseCaseHandler;

    @Nullable
    private String taskId;

    /**
     * Creates a presenter for the add/edit view.
     *
     * @param taskId      ID of the task to edit or null for a new task
     * @param addTaskView the add/edit view
     */
    public AddEditTaskPresenter(@NonNull UseCaseHandler useCaseHandler, @Nullable String taskId,
            @NonNull AddEditTaskContract.View addTaskView, @NonNull GetTask getTask,
            @NonNull SaveTask saveTask) {
        mUseCaseHandler = checkNotNull(useCaseHandler, "useCaseHandler cannot be null!");
        this.taskId = taskId;
        mAddTaskView = checkNotNull(addTaskView, "addTaskView cannot be null!");
        mGetTask = checkNotNull(getTask, "getTask cannot be null!");
        mSaveTask = checkNotNull(saveTask, "saveTask cannot be null!");

        mAddTaskView.setPresenter(this);
    }

    @Override
    public void start() {
        if (taskId != null) {
            populateTask();
        }
    }

   @Override
   public void onDestroyView() {
      mGetTask.unsubscribe();
   }

   @Override
    public void saveTask(String title, String description) {
        if (isNewTask()) {
            createTask(title, description);
        } else {
            updateTask(title, description);
        }
    }

    @Override
    public void populateTask() {
        if (taskId == null) {
            throw new RuntimeException("populateTask() was called but task is new.");
        }
       mGetTask.execute(new GetTask.RequestValues(taskId),new Subscriber<Task>() {
          @Override
          public void onCompleted() {

          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(Task task) {
             showTask(task);
          }

       });

    }

    private void showTask(Task task) {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView.isActive()) {
            mAddTaskView.setTitle(task.getTitle());
            mAddTaskView.setDescription(task.getDescription());
        }
    }

    private void showSaveError() {
        // Show error, log, etc.
    }

    private void showEmptyTaskError() {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView.isActive()) {
            mAddTaskView.showEmptyTaskError();
        }
    }

    private boolean isNewTask() {
        return taskId == null;
    }

    private void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if (newTask.isEmpty()) {
            mAddTaskView.showEmptyTaskError();
        } else {
            mUseCaseHandler.execute(mSaveTask, new SaveTask.RequestValues(newTask),
                    new UseCase.UseCaseCallback<SaveTask.ResponseValue>() {
                        @Override
                        public void onSuccess(SaveTask.ResponseValue response) {
                            mAddTaskView.showTasksList();
                        }

                        @Override
                        public void onError() {
                            showSaveError();
                        }
                    });
        }
    }

    private void updateTask(String title, String description) {
        if (taskId == null) {
            throw new RuntimeException("updateTask() was called but task is new.");
        }
        Task newTask = new Task(title, description, taskId);
        mUseCaseHandler.execute(mSaveTask, new SaveTask.RequestValues(newTask),
                new UseCase.UseCaseCallback<SaveTask.ResponseValue>() {
                    @Override
                    public void onSuccess(SaveTask.ResponseValue response) {
                        // After an edit, go back to the list.
                        mAddTaskView.showTasksList();
                    }

                    @Override
                    public void onError() {
                        showSaveError();
                    }
                });
    }
}
