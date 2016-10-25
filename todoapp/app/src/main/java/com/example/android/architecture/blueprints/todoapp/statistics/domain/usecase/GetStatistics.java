package com.example.android.architecture.blueprints.todoapp.statistics.domain.usecase;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.UseCaseRx;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.model.Statistics;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;

import java.util.ArrayList;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

/**
 * Calculate statistics of active and completed Tasks {@link Task} in the {@link TasksRepository}.
 */
public class GetStatistics extends UseCaseRx<GetStatistics.RequestValues> {

    private TasksRepository tasksRepository;

    public GetStatistics(Scheduler threadExecutor, Scheduler postExecutionThread, @NonNull TasksRepository tasksRepository) {
        super(threadExecutor, postExecutionThread);
        this.tasksRepository = tasksRepository;
    }

    @Override
    protected Observable<Statistics> buildUseCaseObservable(RequestValues requestValues) {
        return tasksRepository.getTasks().map(new Func1<ArrayList<Task>, Statistics>() {

            @Override
            public Statistics call(ArrayList<Task> tasks) {

                int activeTasks = 0;
                int completedTasks = 0;

                // We calculate number of active and completed tasks
                for (Task task : tasks) {
                    if (task.isCompleted()) {
                        completedTasks += 1;
                    } else {
                        activeTasks += 1;
                    }
                }
                return new Statistics(completedTasks,activeTasks);
            }
        });
    }

    public static class RequestValues extends UseCaseRx.RequestValues {
    }

}
