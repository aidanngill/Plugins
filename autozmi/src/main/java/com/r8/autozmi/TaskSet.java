package com.r8.autozmi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** List of tasks to run in a set order every tick. */
@Slf4j
public class TaskSet {
  @Getter
  private Integer currentTaskIndex = 0;

  @Getter
  private Integer currentWrapIndex = 0;

  @Getter
  private Boolean isWrapped = false;

  public List<Task> taskList = new ArrayList<>();

  public TaskSet(Task... tasks) {
    taskList.addAll(Arrays.asList(tasks));
  }

  public void addAll(Task... tasks) {
    taskList.addAll(Arrays.asList(tasks));
  }

  public void resetTaskIndex() {
    currentTaskIndex = 0;
    taskList.forEach(Task::reset);
  }

  public void resetWrap() {
    currentWrapIndex = currentTaskIndex;
    isWrapped = false;
  }

  /** Get the current task, or {@code null} if we are at the end of the set. */
  public Task getCurrentTask() {
    if (taskList.size() <= currentTaskIndex) {
      return null;
    }

    return taskList.get(currentTaskIndex);
  }

  /** Validate the tasks in the set until one that hasn't been validated
   * is found, and return it.*/
  public Task getValidTask() {
    if (taskList.size() <= currentTaskIndex) {
      resetTaskIndex();
    }

    if (currentTaskIndex < currentWrapIndex) {
      isWrapped = true;
    }

    Task currentTask = getCurrentTask();

    if (currentTask == null) {
      return null;
    }

    if (currentTask.validate()) {
      log.debug("Task {} was validated", currentTaskIndex);

      currentTaskIndex++;
      return getValidTask();
    }

    log.debug("Task {}: {}", currentTaskIndex, currentTask.validate());

    return currentTask;
  }

  public Integer getSize() {
    return taskList.size();
  }
}
