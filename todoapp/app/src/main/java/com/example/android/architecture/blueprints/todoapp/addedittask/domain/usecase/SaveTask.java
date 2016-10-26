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

package com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.UseCaseRx;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Updates or creates a new {@link Task} in the {@link TasksRepository}.
 */
public class SaveTask extends UseCaseRx<SaveTask.RequestValues> {


    private TasksRepository tasksRepository;

    public SaveTask(Scheduler threadExecutor, Scheduler postExecutionThread, @NonNull TasksRepository tasksRepository) {
        super(threadExecutor, postExecutionThread);

        this.tasksRepository = tasksRepository;
    }

    @Override
    protected Observable buildUseCaseObservable(final RequestValues requestValues) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                Task task = requestValues.getTask();
                tasksRepository.saveTask(task);
                subscriber.onCompleted();
            }
        });
    }

    public static final class RequestValues extends UseCaseRx.RequestValues {

        private final Task mTask;

        public RequestValues(@NonNull Task task) {
            mTask = checkNotNull(task, "task cannot be null!");
        }

        public Task getTask() {
            return mTask;
        }
    }
}
