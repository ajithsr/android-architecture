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
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import rx.Observable;
import rx.Scheduler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Deletes a {@link Task} from the {@link TasksRepository}.
 */
public class DeleteTask extends UseCaseRx<DeleteTask.RequestValues> {

    private TasksRepository tasksRepository;

    public DeleteTask(Scheduler threadExecutor, Scheduler postExecutionThread, @NonNull TasksRepository tasksRepository) {
        super(threadExecutor, postExecutionThread);
        this.tasksRepository = tasksRepository;
    }

    @Override
    protected Observable buildUseCaseObservable(RequestValues requestValues) {
        return tasksRepository.deleteTask(requestValues.getTaskId()).toObservable();
    }

    public static final class RequestValues extends UseCaseRx.RequestValues {
        private final String mTaskId;

        public RequestValues(@NonNull String taskId) {
            mTaskId = checkNotNull(taskId, "taskId cannot be null!");
        }

        public String getTaskId() {
            return mTaskId;
        }
    }
}
