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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.ActivateTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.ClearCompleteTasks;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.CompleteTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.GetTasks;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TasksFragment}), retrieves the data and updates the
 * UI as required.
 */
public class TasksPresenter implements TasksContract.Presenter {


   private final TasksContract.View mTasksView;
   private final GetTasks getTasks;
   private final CompleteTask completeTask;
   private final ActivateTask activateTask;
   private final ClearCompleteTasks clearCompleteTasks;

   private TasksFilterType currentFiltering = TasksFilterType.ALL_TASKS;

   private boolean mFirstLoad = true;


   public TasksPresenter(
                         @NonNull TasksContract.View tasksView, @NonNull GetTasks getTasks,
                         @NonNull CompleteTask completeTask, @NonNull ActivateTask activateTask,
                         @NonNull ClearCompleteTasks clearCompleteTasks) {
      mTasksView = checkNotNull(tasksView, "tasksView cannot be null!");
      this.getTasks = checkNotNull(getTasks, "getTask cannot be null!");
      this.completeTask = checkNotNull(completeTask, "completeTask cannot be null!");
      this.activateTask = checkNotNull(activateTask, "activateTask cannot be null!");
      this.clearCompleteTasks = checkNotNull(clearCompleteTasks,
            "clearCompleteTasks cannot be null!");


      mTasksView.setPresenter(this);
   }

   @Override
   public void start() {
      loadTasks(false);
   }

   @Override
   public void onDestroyView() {
      getTasks.unsubscribe();
      activateTask.unsubscribe();
      completeTask.unsubscribe();
      clearCompleteTasks.unsubscribe();
   }

   @Override
   public void result(int requestCode, int resultCode) {
      // If a task was successfully added, show snackbar
      if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode
            && Activity.RESULT_OK == resultCode) {
         mTasksView.showSuccessfullySavedMessage();
      }
   }

   @Override
   public void loadTasks(boolean forceUpdate) {
      // Simplification for sample: a network reload will be forced on first load.
      loadTasks(forceUpdate || mFirstLoad, true);
      mFirstLoad = false;
   }

   /**
    * @param forceUpdate   Pass in true to refresh the data in the {@link TasksDataSource}
    * @param showLoadingUI Pass in true to display a loading icon in the UI
    */
   private void loadTasks(boolean forceUpdate, final boolean showLoadingUI) {
      if (showLoadingUI) {
         mTasksView.setLoadingIndicator(true);
      }

      getTasks.execute(new GetTasks.RequestValues(forceUpdate, currentFiltering),
            new Subscriber<ArrayList<Task>>() {
               @Override
               public void onCompleted() {

               }

               @Override
               public void onError(Throwable e) {
                  // The view may not be able to handle UI updates anymore
                  if (!mTasksView.isActive()) {
                     return;
                  }
                  mTasksView.showLoadingTasksError();
               }

               @Override
               public void onNext(ArrayList<Task> tasks) {
                  if (!mTasksView.isActive()) {
                     return;
                  }
                  if (showLoadingUI) {
                     mTasksView.setLoadingIndicator(false);
                  }
                  processTasks(tasks);
               }
            });
   }

   private void processTasks(List<Task> tasks) {
      if (tasks.isEmpty()) {
         // Show a message indicating there are no tasks for that filter type.
         processEmptyTasks();
      } else {
         // Show the list of tasks
         mTasksView.showTasks(tasks);
         // Set the filter label's text.
         showFilterLabel();
      }
   }

   private void showFilterLabel() {
      switch (currentFiltering) {
         case ACTIVE_TASKS:
            mTasksView.showActiveFilterLabel();
            break;
         case COMPLETED_TASKS:
            mTasksView.showCompletedFilterLabel();
            break;
         default:
            mTasksView.showAllFilterLabel();
            break;
      }
   }

   private void processEmptyTasks() {
      switch (currentFiltering) {
         case ACTIVE_TASKS:
            mTasksView.showNoActiveTasks();
            break;
         case COMPLETED_TASKS:
            mTasksView.showNoCompletedTasks();
            break;
         default:
            mTasksView.showNoTasks();
            break;
      }
   }

   @Override
   public void addNewTask() {
      mTasksView.showAddTask();
   }

   @Override
   public void openTaskDetails(@NonNull Task requestedTask) {
      checkNotNull(requestedTask, "requestedTask cannot be null!");
      mTasksView.showTaskDetailsUi(requestedTask.getId());
   }

   @Override
   public void completeTask(@NonNull Task completedTask) {
      checkNotNull(completedTask, "completedTask cannot be null!");

      completeTask.execute(new CompleteTask.RequestValues(completedTask.getId()), new Subscriber() {
         @Override
         public void onCompleted() {
            mTasksView.showTaskMarkedComplete();
            loadTasks(false, false);
         }

         @Override
         public void onError(Throwable e) {
            mTasksView.showLoadingTasksError();
         }

         @Override
         public void onNext(Object o) {

         }
      });
   }


   @Override
   public void activateTask(@NonNull Task activeTask) {
      checkNotNull(activeTask, "activeTask cannot be null!");
      activateTask.execute(new ActivateTask.RequestValues(activeTask.getId()), new Subscriber() {
         @Override
         public void onCompleted() {
            mTasksView.showTaskMarkedActive();
            loadTasks(false, false);
         }

         @Override
         public void onError(Throwable e) {

         }

         @Override
         public void onNext(Object o) {

         }
      });
   }

   @Override
   public void clearCompletedTasks() {
      clearCompleteTasks.execute(new ClearCompleteTasks.RequestValues(), new Subscriber() {
         @Override
         public void onCompleted() {
            mTasksView.showCompletedTasksCleared();
            loadTasks(false, false);
         }

         @Override
         public void onError(Throwable e) {
            mTasksView.showLoadingTasksError();
         }

         @Override
         public void onNext(Object o) {

         }
      });
   }

   /**
    * Sets the current task filtering type.
    *
    * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
    *                    {@link TasksFilterType#COMPLETED_TASKS}, or
    *                    {@link TasksFilterType#ACTIVE_TASKS}
    */
   @Override
   public void setFiltering(TasksFilterType requestType) {
      currentFiltering = requestType;
   }

   @Override
   public TasksFilterType getFiltering() {
      return currentFiltering;
   }

}
