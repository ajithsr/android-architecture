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

package com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.UseCaseRx;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.filter.FilterFactory;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.filter.TaskFilter;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;

import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Fetches the list of tasks.
 */
public class GetTasks extends UseCaseRx<GetTasks.RequestValues> {

   private final TasksRepository mTasksRepository;
   private final FilterFactory filterFactory;


   public GetTasks(Scheduler threadExecutor,
                   Scheduler postExecutionThread, @NonNull TasksRepository tasksRepository, @NonNull FilterFactory filterFactory, boolean mForceUpdate, TasksFilterType currentFiltering) {
      super(threadExecutor, postExecutionThread);
      mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
      this.filterFactory = checkNotNull(filterFactory, "filterFactory cannot be null!");
   }


   @Override
   protected Observable buildUseCaseObservable(final GetTasks.RequestValues requestValues) {
      if (requestValues.isForceUpdate()) {
         mTasksRepository.refreshTasks();
      }
      return mTasksRepository.getTasks().map(new Func1<List<Task>, List<Task>>() {
         @Override
         public List<Task> call(List<Task> tasks) {
            TaskFilter taskFilter = filterFactory.create(requestValues.currentFiltering);
            List<Task> tasksFiltered = taskFilter.filter(tasks);
            return tasksFiltered;
         }
      });
   }

   public static final class RequestValues extends UseCaseRx.RequestValues {
      private boolean forceUpdate;
      private TasksFilterType currentFiltering;

      public RequestValues(boolean mForceUpdate, TasksFilterType currentFiltering) {
         this.forceUpdate = mForceUpdate;
         this.currentFiltering = currentFiltering;
      }

      public boolean isForceUpdate() {
         return forceUpdate;
      }
   }


}
