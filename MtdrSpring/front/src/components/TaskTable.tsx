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

import { useState } from "react";
import {
  IconButton,
  Chip,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Paper,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Button,
  TextField,
  styled,
} from "@mui/material";
import CheckIcon from "@mui/icons-material/Check";
import EditIcon from "@mui/icons-material/Edit";
import { Task } from "../models/Task";
import { User } from "../models/User";

interface TaskTableProps {
  tasks: Task[];
  users: User[];
  handleDelete: (id: number) => void;
  handleEdit: (task: Task) => void;
  handleStateChange: (task: Task, newState: string, hrsReales: number) => void;
}

const StyledTableRow = styled(TableRow)`
  &:nth-of-type(odd) {
    background-color: ${({ theme }) => theme.palette.action.hover}; // accessing the theme
  }
  &:nth-of-type(even) {
    background-color: "grey";
  }
`;


const TaskTable = ({
  tasks,
  users,
  handleEdit,
  handleStateChange,
}: TaskTableProps) => {
  const [updatingTaskId] = useState<number | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [taskName, setTaskName] = useState("");
  const [hrsReales, setHrsReales] = useState<number>(0);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);

  const getUserName = (userId: number) => {
    const user = users.find((u) => u.id_User === userId);
    return user ? user.name : "Unassigned";
  };

  const getStateColor = (state: string) => {
    switch (state) {
      case "TO_DO":
        return "default";
      case "IN_PROGRESS":
        return "primary";
      case "DONE":
        return "success";
      default:
        return "default";
    }
  };

  const toggleState = (task: Task) => {
    if (task.state === "TO_DO") {
      handleStateChange(task, "IN_PROGRESS", 0);
    } else if (task.state === "IN_PROGRESS") {
      handleStateChange(task, "TO_DO", 0);
    } else if (task.state === "DONE") {
      setSelectedTask(task);
      setHrsReales(task.hoursReal || 0);
      setTaskName(task.description);
      setOpenDialog(true); // Abre el diálogo para modificar horas y cambiar estado
    }
  };

  const markAsDone = (task: Task) => {
    if (task.state !== "DONE") {
      setSelectedTask(task);
      setTaskName(task.description);
      setHrsReales(task.hoursReal || 0);
      setOpenDialog(true);
    }
  };

  const handleDialogClose = () => {
    setOpenDialog(false);
    setHrsReales(0);
    setSelectedTask(null);
  };

  const handleConfirmDone = () => {
    if (selectedTask) {
      console.log("Confirming task:", selectedTask);
      console.log("Real Hours:", hrsReales);

      // Llama a handleStateChange pasando el estado y las horas reales
      handleStateChange(
        selectedTask,
        selectedTask.state === "DONE" ? "IN_PROGRESS" : "DONE",
        hrsReales
      );
      setHrsReales(0); // Resetea las horas después de confirmarlo
      setSelectedTask(null); // Resetea la tarea seleccionada
      setOpenDialog(false); // Cierra el diálogo
    }
  };

  return (
    <>
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Description</TableCell>
              <TableCell>Assigned To</TableCell>
              <TableCell width="150px">State</TableCell>
              <TableCell>Estimated Hours</TableCell>
              <TableCell>Real Hours</TableCell>
              <TableCell>Created At</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {tasks.map((task) => (
              <StyledTableRow key={task.id_Task}>
                <TableCell>{task.description}</TableCell>
                <TableCell>{getUserName(task.assignedTo)}</TableCell>
                <TableCell width="150px">
                  {updatingTaskId === task.id_Task ? (
                    <CircularProgress size={24} />
                  ) : (
                    <Chip
                      label={task.state}
                      color={getStateColor(task.state)}
                      size="small"
                      onClick={() => toggleState(task)}
                    />
                  )}
                </TableCell>
                <TableCell>{task.hoursEstimated}h</TableCell>
                <TableCell>
                  {task.hoursReal ? `${task.hoursReal}h` : "-"}
                </TableCell>{" "}
                {/* Aquí mostramos las horas reales */}
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
                  <Tooltip title="Mark as Done">
                    <IconButton onClick={() => markAsDone(task)}>
                      <CheckIcon />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </StyledTableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Dialog to request real hours worked */}
      <Dialog open={openDialog} onClose={handleDialogClose}>
        <DialogTitle>
          {selectedTask?.state === "DONE"
            ? "Modify Real Hours"
            : "Task Marked as Done"}
        </DialogTitle>
        <DialogContent>
          <p>
            The task &quot;{taskName}&quot; has been{" "}
            {selectedTask?.state === "DONE" ? "reopened" : "marked as DONE"}.
          </p>
          <TextField
            label="Real Hours Worked"
            type="number"
            fullWidth
            value={hrsReales}
            onChange={(e) => setHrsReales(Number(e.target.value))}
            inputProps={{ min: 0 }}
            margin="dense"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDialogClose} color="secondary">
            Cancel
          </Button>
          <Button
            onClick={handleConfirmDone}
            color="primary"
            variant="contained"
          >
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default TaskTable;
