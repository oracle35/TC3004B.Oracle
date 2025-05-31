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
  Button,
  Chip,
  CircularProgress,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  styled,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
} from "@mui/material";
import CheckIcon from "@mui/icons-material/Check";
import EditIcon from "@mui/icons-material/Edit";
import { Task } from "../models/Task";
import { User } from "../models/User";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import CompletedTaskDialog from "./CompletedTaskDialog/CompletedTaskDialog";

interface TaskTableProps {
  tasks: Task[];
  users: User[];
  handleDelete: (id: number) => void;
  handleEdit: (task: Task) => void;
  handleStateChange: (task: Task, newState: string, hrsReales: number) => void;
}

const StyledTableRow = styled(TableRow)`
  &:nth-of-type(odd) {
    background-color: ${({ theme }) =>
      theme.palette.action.hover}; // accessing the theme
  }
  &:nth-of-type(even) {
    background-color: "grey";
  }
`;

const getDisplayState = (state: string): string => {
  switch (state) {
    case "TODO": // Assuming backend uses TODO
      return "To Do";
    case "IN_PROGRESS":
      return "In Progress";
    case "QA":
      return "QA";
    case "ON_HOLD":
      return "On Hold";
    case "BLOCKED":
      return "Blocked";
    case "DONE":
      return "Done";
    default:
      return state; // Return original if unknown
  }
};

const TaskTable = ({
  tasks,
  users,
  handleEdit,
  handleStateChange,
}: TaskTableProps) => {
  const [updatingTaskId] = useState<number | null>(null);

  // Sorting and filtering tasks
  const [sortAsc, setSortAsc] = useState<boolean>(true);
  const [filterState, setFilterState] = useState<string>("ALL");
  const [dialogState, setDialogState] = useState({
    open: false,
    taskName: "",
    selectedTask: null as Task | null,
    hrsReales: 0,
  });

  const filteredTasks = tasks
    .filter((task) => filterState === "ALL" || task.state === filterState)
    .sort((a, b) => {
      if (sortAsc) {
        return a.state.localeCompare(b.state);
      } else {
        return b.state.localeCompare(a.state);
      }
    });

  const getUserName = (userId: number) => {
    const user = users.find((u) => u.id_User === userId);
    return user ? user.name : "Unassigned";
  };

  const getStateColor = (state: string) => {
    switch (state) {
      case "TODO":
        return "default";
      case "IN_PROGRESS":
        return "primary";
      case "QA":
        return "secondary";
      case "ON_HOLD":
        return "warning";
      case "BLOCKED":
        return "error";
      case "DONE":
        return "success";
      default:
        return "default";
    }
  };

  const toggleState = (task: Task) => {
    if (task.state === "TODO") {
      handleStateChange(task, "IN_PROGRESS", 0);
    } else if (task.state === "IN_PROGRESS") {
      handleStateChange(task, "QA", 0);
    } else if (task.state === "QA") {
      handleStateChange(task, "ON_HOLD", 0);
    } else if (task.state === "ON_HOLD") {
      handleStateChange(task, "BLOCKED", 0);
    } else if (task.state === "BLOCKED") {
      handleStateChange(task, "TODO", 0);
    }
  };

  const markAsDone = (task: Task) => {
    setDialogState({
      open: true,
      taskName: task.description,
      selectedTask: task,
      hrsReales: task.hoursReal || 0,
    });
  };

  const handleDialogClose = () => {
    setDialogState((prev) => ({
      ...prev,
      open: false,
      selectedTask: null,
      hrsReales: 0,
      taskName: "",
    }));
  };

  const handleConfirmDone = () => {
    if (dialogState.selectedTask) {
      handleStateChange(
        dialogState.selectedTask,
        dialogState.selectedTask.state === "DONE" ? "IN_PROGRESS" : "DONE",
        dialogState.hrsReales,
      );
      setDialogState({
        open: false,
        taskName: "",
        selectedTask: null,
        hrsReales: 0,
      });
    }
  };

  return (
    <>
      {/**Sorting and filtering controls */}
      <div
        style={{
          display: "flex",
          gap: "1rem",
          alignItems: "center",
          marginBottom: 24,
          marginTop: 8,
        }}
      >
        <FormControl size="small" variant="outlined" sx={{ minWidth: 180 }}>
          <InputLabel
            id="filter-state-label"
            sx={{
              color: "#c74634",
              "&.Mui-focused": {
                color: "#c74634",
              },
            }}
          >
            Filter by State
          </InputLabel>
          <Select
            labelId="filter-state-label"
            value={filterState}
            label="Filter by State"
            onChange={(e) => setFilterState(e.target.value)}
            sx={{
              height: 40,
              textTransform: "none",
              fontWeight: 600,
              borderRadius: 2,
              color: "#c74634",

              "&:hover": {
                background: "#312d2a",
                borderColor: "#c74634",
              },
              ".MuiOutlinedInput-notchedOutline": {
                borderColor: "#c74634",
              },
              "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
                borderColor: "#c74634",
              },
              "&:hover .MuiOutlinedInput-notchedOutline": {
                borderColor: "#c74634",
              },
              ".MuiSvgIcon-root ": {
                fill: "white !important",
              },
            }}
          >
            <MenuItem value="ALL">All</MenuItem>
            <MenuItem value="TODO">To Do</MenuItem>
            <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
            <MenuItem value="QA">QA</MenuItem>
            <MenuItem value="DONE">Done</MenuItem>
            <MenuItem value="ON_HOLD">On Hold</MenuItem>
            <MenuItem value="BLOCKED">Blocked</MenuItem>
          </Select>
        </FormControl>
        <Button
          variant="outlined"
          onClick={() => setSortAsc((prev) => !prev)}
          size="small"
          sx={{
            height: 40,
            textTransform: "none",
            fontWeight: 600,
            borderRadius: 2,
            color: "#c74634",
            borderColor: "#c74634",
            "&:hover": {
              background: "#312d2a",
              borderColor: "#c74634",
            },
          }}
        >
          Sort by State {sortAsc ? "▲" : "▼"}
        </Button>
      </div>

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
              <TableCell>Finishes At</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredTasks.map((task) => (
              <StyledTableRow key={task.id_Task}>
                <TableCell>{task.description}</TableCell>
                <TableCell>{getUserName(task.assignedTo)}</TableCell>
                <TableCell width="150px">
                  {updatingTaskId === task.id_Task ? (
                    <CircularProgress size={24} />
                  ) : (
                    <Chip
                      label={getDisplayState(task.state)}
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
                {/*Originally defined for testing purposes. Might be final. */}
                <TableCell>
                  {task.finishesAt
                    ? new Date(task.finishesAt).toLocaleDateString()
                    : "No Finish Date"}
                </TableCell>
                <TableCell>
                  <Tooltip title="Edit Task" placement="top">
                    <IconButton onClick={() => handleEdit(task)}>
                      <EditIcon />
                    </IconButton>
                  </Tooltip>
                  {task.state !== "DONE" ? (
                    <Tooltip title="Mark task as Done" placement="top">
                      <IconButton onClick={() => markAsDone(task)}>
                        <CheckIcon />
                      </IconButton>
                    </Tooltip>
                  ) : (
                    <Tooltip title="Reopen Task" placement="top">
                      <IconButton onClick={() => markAsDone(task)}>
                        <ArrowBackIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
              </StyledTableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <CompletedTaskDialog
        dialogState={dialogState}
        setDialogState={setDialogState}
        handleConfirmDone={handleConfirmDone}
        handleDialogClose={handleDialogClose}
      />
    </>
  );
};

export default TaskTable;
