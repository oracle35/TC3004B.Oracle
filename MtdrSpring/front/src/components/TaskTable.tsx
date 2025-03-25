/**
 * TaskTable component props interface.
 *
 * @interface TaskTableProps
 * @property {ToDoElement[]} tasks - Array of task objects.
 * @property {boolean} [done] - Filter tasks by done status.
 * @property {function} toggleDone - Function to toggle task done status.
 * @property {function} handleDelete - Function to delete a task.
 */

/**
 * TaskTable component to display a list of tasks in a table format.
 *
 * @param {TaskTableProps} props - Component props.
 * @returns {JSX.Element} The rendered component.
 */

import {
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
} from "@mui/material";
import { ToDoElement } from "../models/ToDoElement";
import DeleteIcon from "@mui/icons-material/Delete";
import DoneIcon from "@mui/icons-material/Done";
import UndoIcon from "@mui/icons-material/Undo";
import Paper from "@mui/material/Paper";

interface TaskTableProps {
  tasks: ToDoElement[];
  done?: boolean;
  toggleDone: (
    event: React.MouseEvent<HTMLButtonElement>,
    id: number,
    description: string,
    done: boolean
  ) => void;
  handleDelete: (id: number) => void;
}

const TaskTable = ({
  tasks,
  done,
  toggleDone,
  handleDelete,
}: TaskTableProps) => {
  return (
    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Description</TableCell>
            <TableCell>Creation date</TableCell>
            <TableCell>Delivery date</TableCell>
            <TableCell></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {tasks.length === 0 && (
            <TableRow>
              <TableCell colSpan={4}>No tasks</TableCell>
            </TableRow>
          )}
          {tasks
            .filter((task) => task.done === done) // Filter tasks by done status
            .map((task, index) => (
              <TableRow
                key={task.id}
                className={index % 2 === 0 ? "bg-gray-300" : "bg-white"}
              >
                <TableCell>{task.description}</TableCell>
                <TableCell>
                  {task.creation_ts ? task.creation_ts.toString() : "Undefined"}
                </TableCell>
                <TableCell>
                  {task.delivery_ts ? task.delivery_ts.toString() : "Undefined"}
                </TableCell>
                <TableCell>
                  <Tooltip title={`Mark as ${task.done ? "undone" : "done"}`}>
                    <IconButton
                      onClick={(event) =>
                        toggleDone(event, task.id, task.description, !task.done)
                      }
                    >
                      {task.done ? <UndoIcon /> : <DoneIcon />}
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete">
                    <IconButton onClick={() => handleDelete(task.id)}>
                      <DeleteIcon />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default TaskTable;
