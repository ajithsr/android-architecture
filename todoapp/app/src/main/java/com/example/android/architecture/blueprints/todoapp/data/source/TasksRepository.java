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

package com.example.android.architecture.blueprints.todoapp.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class TasksRepository implements TasksDataSource {

   private static TasksRepository INSTANCE = null;

   private final TasksDataSource mTasksRemoteDataSource;

   private final TasksDataSource mTasksLocalDataSource;

   /**
    * This variable has package local visibility so it can be accessed from tests.
    */
   Map<String, Task> mCachedTasks;

   /**
    * Marks the cache as invalid, to force an update the next time data is requested. This variable
    * has package local visibility so it can be accessed from tests.
    */
   boolean mCacheIsDirty = false;

   // Prevent direct instantiation.
   private TasksRepository(@NonNull TasksDataSource tasksRemoteDataSource,
                           @NonNull TasksDataSource tasksLocalDataSource) {
      mTasksRemoteDataSource = checkNotNull(tasksRemoteDataSource);
      mTasksLocalDataSource = checkNotNull(tasksLocalDataSource);
   }

   /**
    * Returns the single instance of this class, creating it if necessary.
    *
    * @param tasksRemoteDataSource the backend data source
    * @param tasksLocalDataSource  the device storage data source
    * @return the {@link TasksRepository} instance
    */
   public static TasksRepository getInstance(TasksDataSource tasksRemoteDataSource,
                                             TasksDataSource tasksLocalDataSource) {
      if (INSTANCE == null) {
         INSTANCE = new TasksRepository(tasksRemoteDataSource, tasksLocalDataSource);
      }
      return INSTANCE;
   }

   /**
    * Used to force {@link #getInstance(TasksDataSource, TasksDataSource)} to create a new instance
    * next time it's called.
    */
   public static void destroyInstance() {
      INSTANCE = null;
   }

   /**
    * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
    * available first.
    * <p>
    */
   @Override
   public Observable<ArrayList<Task>> getTasks() {

      // Respond immediately with cache if available and not dirty
      if (mCachedTasks != null && !mCacheIsDirty) {
         return Observable.just(new ArrayList<>(mCachedTasks.values()));
      }

      if (mCacheIsDirty) {
         // If the cache is dirty we need to fetch new data from the network.
         return getTasksFromRemoteDataSource();
      } else {
         return mTasksLocalDataSource.getTasks()
               .switchIfEmpty(getTasksFromRemoteDataSource());
      }
   }


   @Override
   public void saveTask(@NonNull final Task task) {
      checkNotNull(task);
      mTasksRemoteDataSource.saveTask(task);
      mTasksLocalDataSource.saveTask(task);

      // Do in memory cache update to keep the app UI up to date
      if (mCachedTasks == null) {
         mCachedTasks = new LinkedHashMap<>();
      }
      mCachedTasks.put(task.getId(), task);
   }

   @Override
   public void completeTask(@NonNull final Task task) {
      checkNotNull(task);
      mTasksRemoteDataSource.completeTask(task);
      mTasksLocalDataSource.completeTask(task);

      Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);

      // Do in memory cache update to keep the app UI up to date
      if (mCachedTasks == null) {
         mCachedTasks = new LinkedHashMap<>();
      }
      mCachedTasks.put(task.getId(), completedTask);

   }

   @Override
   public void completeTask(@NonNull String taskId) {
      checkNotNull(taskId);
      completeTask(getTaskWithId(taskId));
   }

   @Override
   public void activateTask(@NonNull final Task task) {
      checkNotNull(task);
      mTasksRemoteDataSource.activateTask(task);
      mTasksLocalDataSource.activateTask(task);

      Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());

      // Do in memory cache update to keep the app UI up to date
      if (mCachedTasks == null) {
         mCachedTasks = new LinkedHashMap<>();
      }
      mCachedTasks.put(task.getId(), activeTask);
   }

   @Override
   public void activateTask(@NonNull String taskId) {
      checkNotNull(taskId);
      activateTask(getTaskWithId(taskId));
   }

   @Override
   public void clearCompletedTasks() {
      mTasksRemoteDataSource.clearCompletedTasks();
      mTasksLocalDataSource.clearCompletedTasks();

      // Do in memory cache update to keep the app UI up to date
      if (mCachedTasks == null) {
         mCachedTasks = new LinkedHashMap<>();
      }
      Iterator<Map.Entry<String, Task>> it = mCachedTasks.entrySet().iterator();
      while (it.hasNext()) {
         Map.Entry<String, Task> entry = it.next();
         if (entry.getValue().isCompleted()) {
            it.remove();
         }
      }

   }

   /**
    * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
    * uses the network data source. This is done to simplify the sample.
    * <p>
    */
   @Override
   public Observable<Task> getTask(@NonNull final String taskId) {
      checkNotNull(taskId);

      Task cachedTask = getTaskWithId(taskId);
      // Respond immediately with cache if available
      if (cachedTask != null) {
         return Observable.just(cachedTask);
      }
      return mTasksLocalDataSource.getTask(taskId)
            .switchIfEmpty(mTasksRemoteDataSource.getTask(taskId));
   }


   @Override
   public void refreshTasks() {
      mCacheIsDirty = true;
   }

   @Override
   public void deleteAllTasks() {
      mTasksRemoteDataSource.deleteAllTasks();
      mTasksLocalDataSource.deleteAllTasks();

      if (mCachedTasks == null) {
         mCachedTasks = new LinkedHashMap<>();
      }
      mCachedTasks.clear();
   }

   @Override
   public void deleteTask(@NonNull final String taskId) {
      mTasksRemoteDataSource.deleteTask(checkNotNull(taskId));
      mTasksLocalDataSource.deleteTask(checkNotNull(taskId));

      mCachedTasks.remove(taskId);

   }

   private Observable<ArrayList<Task>> getTasksFromRemoteDataSource() {

      return mTasksRemoteDataSource.getTasks().map(new Func1<ArrayList<Task>, ArrayList<Task>>() {
         @Override
         public ArrayList<Task> call(ArrayList<Task> tasks) {
            refreshCache(tasks);
            refreshLocalDataSource(tasks);
            return tasks;
         }
      });

   }

   @VisibleForTesting
   private void refreshCache(List<Task> tasks) {
      if (mCachedTasks == null) {
         mCachedTasks = new LinkedHashMap<>();
      }
      mCachedTasks.clear();
      for (Task task : tasks) {
         mCachedTasks.put(task.getId(), task);
      }
      mCacheIsDirty = false;
   }

   private void refreshLocalDataSource(List<Task> tasks) {
      mTasksLocalDataSource.deleteAllTasks();
      for (Task task : tasks) {
         mTasksLocalDataSource.saveTask(task);
      }
   }

   @Nullable
   private Task getTaskWithId(@NonNull String id) {
      checkNotNull(id);
      if (mCachedTasks == null || mCachedTasks.isEmpty()) {
         return null;
      } else {
         return mCachedTasks.get(id);
      }
   }
}
