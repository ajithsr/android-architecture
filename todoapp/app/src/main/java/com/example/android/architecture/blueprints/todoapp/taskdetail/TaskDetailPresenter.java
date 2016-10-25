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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.DeleteTask;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.GetTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.ActivateTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.CompleteTask;
import com.google.common.base.Strings;

import rx.Subscriber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class TaskDetailPresenter implements TaskDetailContract.Presenter {

   private final TaskDetailContract.View mTaskDetailView;
   private final GetTask getTask;
   private final CompleteTask completeTask;
   private final ActivateTask activateTask;
   private final DeleteTask deleteTask;

   @Nullable
   private String mTaskId;

   public TaskDetailPresenter(
                              @Nullable String taskId,
                              @NonNull TaskDetailContract.View taskDetailView,
                              @NonNull GetTask getTask,
                              @NonNull CompleteTask completeTask,
                              @NonNull ActivateTask activateTask,
                              @NonNull DeleteTask deleteTask) {
      mTaskId = taskId;
      mTaskDetailView = checkNotNull(taskDetailView, "taskDetailView cannot be null!");
      this.getTask = checkNotNull(getTask, "getTask cannot be null!");
      this.completeTask = checkNotNull(completeTask, "completeTask cannot be null!");
      this.activateTask = checkNotNull(activateTask, "activateTask cannot be null!");
      this.deleteTask = checkNotNull(deleteTask, "deleteTask cannot be null!");
      mTaskDetailView.setPresenter(this);
   }

   @Override
   public void start() {
      openTask();
   }

   @Override
   public void onDestroyView() {
      getTask.unsubscribe();
      completeTask.unsubscribe();
      activateTask.unsubscribe();
   }

   private void openTask() {
      if (Strings.isNullOrEmpty(mTaskId)) {
         mTaskDetailView.showMissingTask();
         return;
      }

      mTaskDetailView.setLoadingIndicator(true);
      GetTask.RequestValues requestValues = new GetTask.RequestValues(mTaskId);

      getTask.execute(requestValues,new Subscriber<Task>() {
         @Override
         public void onCompleted() {

         }

         @Override
         public void onError(Throwable e) {
            // The view may not be able to handle UI updates anymore
            if (!mTaskDetailView.isActive()) {
               return;
            }
            mTaskDetailView.showMissingTask();
            Log.e("TEST ERROR",e.getMessage());
         }

         @Override
         public void onNext(Task task) {
            // The view may not be able to handle UI updates anymore
            if (!mTaskDetailView.isActive()) {
               return;
            }
            mTaskDetailView.setLoadingIndicator(false);
            showTask(task);
         }
      });

   }

   @Override
   public void editTask() {
      if (Strings.isNullOrEmpty(mTaskId)) {
         mTaskDetailView.showMissingTask();
         return;
      }
      mTaskDetailView.showEditTask(mTaskId);
   }

   @Override
   public void deleteTask() {
       deleteTask.execute(new DeleteTask.RequestValues(mTaskId), new Subscriber() {
         @Override
         public void onCompleted() {
            mTaskDetailView.showTaskDeleted();
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
   public void completeTask() {
      if (Strings.isNullOrEmpty(mTaskId)) {
         mTaskDetailView.showMissingTask();
         return;
      }
      CompleteTask.RequestValues requestValues = new CompleteTask.RequestValues(mTaskId);
      completeTask.execute(requestValues, new Subscriber() {
         @Override
         public void onCompleted() {
            mTaskDetailView.showTaskMarkedComplete();
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
   public void activateTask() {
      if (Strings.isNullOrEmpty(mTaskId)) {
         mTaskDetailView.showMissingTask();
         return;
      }
      activateTask.execute(new ActivateTask.RequestValues(mTaskId), new Subscriber() {
         @Override
         public void onCompleted() {
            mTaskDetailView.showTaskMarkedActive();
         }

         @Override
         public void onError(Throwable e) {

         }

         @Override
         public void onNext(Object o) {

         }
      });
   }

   private void showTask(@NonNull Task task) {
      String title = task.getTitle();
      String description = task.getDescription();

      if (Strings.isNullOrEmpty(title)) {
         mTaskDetailView.hideTitle();
      } else {
         mTaskDetailView.showTitle(title);
      }

      if (Strings.isNullOrEmpty(description)) {
         mTaskDetailView.hideDescription();
      } else {
         mTaskDetailView.showDescription(description);
      }
      mTaskDetailView.showCompletionStatus(task.isCompleted());
   }
}
