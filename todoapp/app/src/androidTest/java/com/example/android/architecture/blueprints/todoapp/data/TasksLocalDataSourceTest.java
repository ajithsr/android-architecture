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

package com.example.android.architecture.blueprints.todoapp.data;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksDbHelper;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Integration test for the {@link TasksDataSource}, which uses the {@link TasksDbHelper}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TasksLocalDataSourceTest {

   private static final String TITLE = "title";

   private static final String TITLE2 = "title2";

   private static final String TITLE3 = "title3";

   private TasksLocalDataSource mLocalDataSource;

   @Before
   public void setup() {
      mLocalDataSource = TasksLocalDataSource.getInstance(
            InstrumentationRegistry.getTargetContext());
   }

   @After
   public void cleanUp() {
      mLocalDataSource.deleteAllTasks();
   }

   @Test
   public void testPreConditions() {
      assertNotNull(mLocalDataSource);
   }

   @Test
   public void saveTask_retrievesTask() {
      // Given a new task
      final Task newTask = new Task(TITLE, "");

      // When saved into the persistent repository
      TestSubscriber<Object> testSubscriber1 = new TestSubscriber<>();
      mLocalDataSource.saveTask(newTask).subscribe(testSubscriber1);

      TestSubscriber<Task> testSubscriber2 = new TestSubscriber<>();
      mLocalDataSource.getTask(newTask.getId()).subscribe(testSubscriber2);
      testSubscriber2.assertNoErrors();
      testSubscriber2.assertValue(newTask);

   }

   @Test
   public void completeTask_retrievedTaskIsComplete() {
      // Given a new task in the persistent repository
      final Task newTask = new Task(TITLE, "");
      TestSubscriber<Object> testSubscriber1 = new TestSubscriber<>();
      mLocalDataSource.saveTask(newTask).subscribe(testSubscriber1);

      // When completed in the persistent repository
      TestSubscriber<Object> testSubscriber2 = new TestSubscriber<>();
      mLocalDataSource.completeTask(newTask).subscribe(testSubscriber2);

      // When we retrieve the task form the repository
      TestSubscriber<Task> testSubscriber3 = new TestSubscriber<>();
      mLocalDataSource.getTask(newTask.getId()).subscribe(testSubscriber3);
      testSubscriber3.assertNoErrors();
      List<Task> onNextEvents = testSubscriber3.getOnNextEvents();
      Task completedTask = onNextEvents.get(0);

      //The task is completed
      assertEquals(completedTask, newTask);
      assertTrue(completedTask.isCompleted());
   }

   @Test
   public void activateTask_retrievedTaskIsActive() {
      // Given a new completed task in the persistent repository
      final Task newTask = new Task(TITLE, "");
      mLocalDataSource.saveTask(newTask).subscribe(new TestSubscriber<>());
      mLocalDataSource.completeTask(newTask).subscribe(new TestSubscriber<>());

      // When activated in the persistent repository
      mLocalDataSource.activateTask(newTask).subscribe(new TestSubscriber<>());

      // Then the task can be retrieved from the persistent repository and is active
      TestSubscriber<Task> testSubscriber = new TestSubscriber<>();
      mLocalDataSource.getTask(newTask.getId()).subscribe(testSubscriber);
      testSubscriber.assertValueCount(1);
      Task result = testSubscriber.getOnNextEvents().get(0);

      assertThat(result.isActive(), is(true));
      assertThat(result.isCompleted(), is(false));
   }

   @Test
   public void clearCompletedTask_taskNotRetrievable() {
      // Given 2 new completed tasks and 1 active task in the persistent repository
      final Task newTask1 = new Task(TITLE, "");
      mLocalDataSource.saveTask(newTask1).subscribe(new TestSubscriber<>());
      mLocalDataSource.completeTask(newTask1).subscribe(new TestSubscriber<>());
      final Task newTask2 = new Task(TITLE2, "");
      mLocalDataSource.saveTask(newTask2).subscribe(new TestSubscriber<>());
      mLocalDataSource.completeTask(newTask2).subscribe(new TestSubscriber<>());
      final Task newTask3 = new Task(TITLE3, "");
      mLocalDataSource.saveTask(newTask3).subscribe(new TestSubscriber<>());

      // When completed tasks are cleared in the repository
      mLocalDataSource.clearCompletedTasks().subscribe(new TestSubscriber<>());

      // Then the completed tasks cannot be retrieved and the active one can
      TestSubscriber<Task> testSubscriber1 = new TestSubscriber<>();
      mLocalDataSource.getTask(newTask1.getId()).subscribe(testSubscriber1);
      testSubscriber1.assertNoValues();

      TestSubscriber<Task> testSubscriber2 = new TestSubscriber<>();
      mLocalDataSource.getTask(newTask2.getId()).subscribe(testSubscriber2);
      testSubscriber2.assertNoValues();

      TestSubscriber<Task> testSubscriber3 = new TestSubscriber<>();
      mLocalDataSource.getTask(newTask3.getId()).subscribe(testSubscriber3);
      testSubscriber3.assertValueCount(1);
   }

   @Test
   public void deleteAllTasks_emptyListOfRetrievedTask() {
      // Given a new task in the persistent repository and a mocked callback
      Task newTask = new Task(TITLE, "");
      mLocalDataSource.saveTask(newTask).subscribe(new TestSubscriber<>());

      // When all tasks are deleted
      mLocalDataSource.deleteAllTasks();

      // Then the retrieved tasks is an empty list
      TestSubscriber<ArrayList<Task>> testSubscriber = new TestSubscriber<>();
      mLocalDataSource.getTasks().subscribe(testSubscriber);
      testSubscriber.assertNoValues();
   }

    @Test
    public void getTasks_retrieveSavedTasks() {
        // Given 2 new tasks in the persistent repository
        final Task newTask1 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask1).subscribe(new TestSubscriber<>());

        final Task newTask2 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask2).subscribe(new TestSubscriber<>());


       TestSubscriber<ArrayList<Task>> testSubscriber = new TestSubscriber<>();
       mLocalDataSource.getTasks().subscribe(testSubscriber);



       testSubscriber.assertNoErrors();
       ArrayList<Task> tasks = testSubscriber.getOnNextEvents().get(0);
       assertThat(tasks.size(),is(2));
       assertThat(tasks.get(0).getId(),is(newTask1.getId()));
       assertThat(tasks.get(1).getId(),is(newTask2.getId()));
    }
}
