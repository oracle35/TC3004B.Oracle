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
  Box,
  Button,
  Chip,
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

const StyledTableRow = styled(TableRow)(({ theme }) => ({
  "&:nth-of-type(odd)": {
    backgroundColor: theme.palette.action.hover,
  },
  "&:nth-of-type(even)": {
    backgroundColor: theme.palette.background.paper,
  },
}));

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
  // Sorting and filtering tasks
  const [sortAsc, setSortAsc] = useState<boolean>(true);
  const [filterState, setFilterState] = useState<string>("ALL");
  const [dialogState, setDialogState] = useState({
    open: false,
    taskName: "",
    selectedTask: null as Task | null,
    hrsReales: 0,
  });
  const [stateSelectorTaskId, setStateSelectorTaskId] = useState<number | null>(
    null,
  );

  const filteredTasks = tasks.filter(
    (task) => filterState === "ALL" || task.state === filterState,
  );

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

  const TASK_STATES = [
    { value: "TODO", label: "To Do" },
    { value: "IN_PROGRESS", label: "In Progress" },
    { value: "QA", label: "QA" },
    { value: "ON_HOLD", label: "On Hold" },
    { value: "BLOCKED", label: "Blocked" },
    { value: "DONE", label: "Done" },
  ];

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
              <TableCell sx={{ width: 220, maxWidth: 220, minWidth: 220 }}>
                Description
              </TableCell>
              <TableCell sx={{ width: 140, maxWidth: 140, minWidth: 140 }}>
                Assigned To
              </TableCell>
              <TableCell sx={{ width: 150, maxWidth: 150, minWidth: 150 }}>
                State
              </TableCell>
              <TableCell sx={{ width: 120, maxWidth: 120, minWidth: 120 }}>
                Estimated Hours
              </TableCell>
              <TableCell sx={{ width: 110, maxWidth: 110, minWidth: 110 }}>
                Real Hours
              </TableCell>
              <TableCell sx={{ width: 130, maxWidth: 130, minWidth: 130 }}>
                Created At
              </TableCell>
              <TableCell sx={{ width: 130, maxWidth: 130, minWidth: 130 }}>
                Finishes At
              </TableCell>
              <TableCell sx={{ width: 120, maxWidth: 120, minWidth: 120 }}>
                Actions
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredTasks.map((task) => (
              <StyledTableRow key={task.id_Task}>
                <TableCell
                  sx={{
                    width: 220,
                    maxWidth: 220,
                    minWidth: 220,
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                  }}
                >
                  {task.description}
                </TableCell>
                <TableCell
                  sx={{
                    width: 140,
                    maxWidth: 140,
                    minWidth: 140,
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                  }}
                >
                  {getUserName(task.assignedTo)}
                </TableCell>
                <TableCell
                  sx={{
                    width: 150,
                    maxWidth: 150,
                    minWidth: 150,
                    position: "relative",
                  }}
                >
                  <Box>
                    <Chip
                      label={getDisplayState(task.state)}
                      color={getStateColor(task.state)}
                      size="small"
                      onClick={() => setStateSelectorTaskId(task.id_Task)}
                      sx={{ cursor: "pointer" }}
                    />
                    {stateSelectorTaskId === task.id_Task && (
                      <Box
                        sx={{
                          zIndex: 10,
                          bgcolor: "background.paper",
                          boxShadow: 3,
                          borderRadius: 1,
                          minWidth: 120,
                          mt: 1,
                          p: 1,
                        }}
                      >
                        <FormControl size="small" fullWidth>
                          <Select
                            value=""
                            onChange={(e) => {
                              handleStateChange(task, e.target.value, 0);
                              setStateSelectorTaskId(null);
                            }}
                            onBlur={() => setStateSelectorTaskId(null)}
                            autoFocus
                            displayEmpty
                            renderValue={() => "Change state..."}
                          >
                            {TASK_STATES.filter(
                              (option) => option.value !== task.state,
                            ).map((option) => (
                              <MenuItem key={option.value} value={option.value}>
                                {option.label}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      </Box>
                    )}
                  </Box>
                </TableCell>
                <TableCell sx={{ width: 120, maxWidth: 120, minWidth: 120 }}>
                  {task.hoursEstimated}h
                </TableCell>
                <TableCell sx={{ width: 110, maxWidth: 110, minWidth: 110 }}>
                  {task.hoursReal ? `${task.hoursReal}h` : "-"}
                </TableCell>{" "}
                {/* Aquí mostramos las horas reales */}
                <TableCell sx={{ width: 130, maxWidth: 130, minWidth: 130 }}>
                  {task.createdAt
                    ? new Date(task.createdAt).toLocaleDateString()
                    : "No Creation Date"}
                </TableCell>
                {/*Originally defined for testing purposes. Might be final. */}
                <TableCell sx={{ width: 130, maxWidth: 130, minWidth: 130 }}>
                  {task.finishesAt
                    ? new Date(task.finishesAt).toLocaleDateString()
                    : "No Finish Date"}
                </TableCell>
                <TableCell sx={{ width: 120, maxWidth: 120, minWidth: 120 }}>
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
