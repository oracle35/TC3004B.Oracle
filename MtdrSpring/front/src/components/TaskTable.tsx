/**
 * TaskTable component props interface.
 *
 * @interface TaskTableProps
 * @property {ToDoElement[]} tasks - Array of task objects to be displayed in the table.
 * @property {boolean} [done] - Optional filter to display tasks based on their done status.
 * @property {function} toggleDone - Function to toggle the done status of a task.
 * @param {React.MouseEvent<HTMLButtonElement>} event - The click event.
 * @param {number} id - The ID of the task.
 * @param {string} description - The description of the task.
 * @param {boolean} done - The current done status of the task.
 */

/**
 * TaskTable component to display a list of tasks in a table format.
 *
 * @param {TaskTableProps} props - The props for the TaskTable component.
 * @param {ToDoElement[]} props.tasks - Array of task objects to be displayed in the table.
 * @param {boolean} [props.done] - Optional filter to display tasks based on their done status.
 * @param {function} props.toggleDone - Function to toggle the done status of a task.
 * @returns {JSX.Element} The rendered TaskTable component.
 */

import { Button } from "@mui/material";
import { ToDoElement } from "../models/ToDoElement";

interface TaskTableProps {
  tasks: ToDoElement[];
  done?: boolean;
  toggleDone: (
    event: React.MouseEvent<HTMLButtonElement>,
    id: number,
    description: string,
    done: boolean
  ) => void;
}

const TaskTable = ({ tasks, done, toggleDone }: TaskTableProps) => {
  return (
    <table className="w-full">
      <thead>
        <tr>
          <th>Description</th>
          <th>Creation date</th>
          <th>Delivery date</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {tasks.length === 0 && (
          <tr>
            <td colSpan={4}>No tasks</td>
          </tr>
        )}
        {tasks
          .filter((task) => task.done === done) // Filter tasks by done status
          .map((task, index) => (
            <tr
              key={task.id}
              className={`text-black ${index % 2 == 0 ? "bg-gray-300" : "bg-white"}`}
            >
              <td className="p-2">{task.description}</td>
              <td className="p-2">
                {task.creation_ts ? task.creation_ts.toString() : "Undefined"}
              </td>
              <td className="p-2">
                {task.delivery_ts ? task.delivery_ts.toString() : "Undefined"}
              </td>
              <td className="p-2">
                <Button
                  className="bg-blue-500 text-white"
                  onClick={(event) =>
                    toggleDone(event, task.id, task.description, !task.done)
                  }
                >
                  {task.done ? "Undone" : "Done"}
                </Button>
              </td>
            </tr>
          ))}
      </tbody>
    </table>
  );
};

export default TaskTable;
