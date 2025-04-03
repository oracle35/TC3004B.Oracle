/**
 * TaskTable component props interface.
 *
 * @interface TaskTableProps
 * @property {Task[]} tasks - Array of task objects.
 * @property {User[]} users - Array of user objects.
 * @property {function} handleDelete - Function to delete a task.
 * @property {function} handleEdit - Function to edit a task.
 * @property {function} handleStateChange - Function to change the state of a task.
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
  Chip,
} from "@mui/material";
import { Task } from "../models/Task";
import { User } from "../models/User";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import Paper from "@mui/material/Paper";

interface TaskTableProps {
  tasks: Task[];
  users: User[];
  handleDelete: (id: number) => void;
  handleEdit: (task: Task) => void;
  handleStateChange: (task: Task, newState: string) => void;
}

const getStateColor = (state: string) => {
  switch (state) {
    case "TODO":
      return "default";
    case "IN_PROGRESS":
      return "primary";
    case "DONE":
      return "success";
    default:
      return "default";
  }
};

const TaskTable = ({
  tasks,
  users,
  handleDelete,
  handleEdit,
  handleStateChange,
}: TaskTableProps) => {
  const getUserName = (userId: number) => {
    const user = users.find(u => u.id_User === userId);
    return user ? user.name : "Unassigned";
  };

  return (
    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Description</TableCell>
            <TableCell>Assigned To</TableCell>
            <TableCell>State</TableCell>
            <TableCell>Estimated Hours</TableCell>
            <TableCell>Real Hours</TableCell>
            <TableCell>Created At</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {tasks.length === 0 && (
            <TableRow>
              <TableCell colSpan={7} align="center">
                No tasks
              </TableCell>
            </TableRow>
          )}

          {tasks.map((task, index) => (
            <TableRow
              key={task.id_Task}
              className={index % 2 === 0 ? "bg-gray-100" : "bg-white"}
            >
              <TableCell>{task.description}</TableCell>
              <TableCell>{getUserName(task.assignedTo)}</TableCell>
              <TableCell>
                <Chip
                  label={task.state}
                  color={getStateColor(task.state)}
                  size="small"
                  onClick={() => {
                    const nextState = task.state === "TODO" ? "IN_PROGRESS" :
                                    task.state === "IN_PROGRESS" ? "DONE" : "TODO";
                    handleStateChange(task, nextState);
                  }}
                />
              </TableCell>
              <TableCell>{task.hoursEstimated}h</TableCell>
              <TableCell>{task.hoursReal ? `${task.hoursReal}h` : "-"}</TableCell>
              <TableCell>
                {task.createdAt
                  ? new Date(task.createdAt).toLocaleDateString()
                  : "No Creation Date"}
              </TableCell>
              <TableCell>
                <Tooltip title="Edit">
                  <IconButton onClick={() => handleEdit(task)}>
                    <EditIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Delete">
                  <IconButton onClick={() => handleDelete(task.id_Task)}>
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
