/**
 * TaskDependency model
 * This model represents a dependency relationship between tasks.
 */

export interface TaskDependency {
  id_ParentTask: number;
  id_ChildTask: number;
} 