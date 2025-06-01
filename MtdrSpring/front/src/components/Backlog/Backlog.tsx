import {
  Box,
  Divider,
  Drawer,
  FormControl,
  IconButton,
  List,
  ListItem,
  ListItemText,
  MenuItem,
  Select,
  Typography,
} from "@mui/material";
import { Task } from "../../models/Task";
import { Sprint } from "../../models/Sprint";
import { useEffect, useState } from "react";
import styles from "./Backlog.module.css";
import { Subtitle } from "../Subtitle";
import { updateTask } from "../../api/task";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
interface BacklogDrawerProps {
  open: boolean;
  onClose: (arg0: boolean) => void;
  tasks: Task[];
  sprints: Sprint[];
  handleEdit: (updatedTaskFromModal: Task) => Promise<void>;
}

/*
 * Backlog Sidebar.
 * Used to indicate the backlog, aka. of tasks that are on -1.
 * TODO: Refactor this entire module.
 * */

const BacklogDrawer = ({
  open,
  onClose,
  tasks,
  sprints,
  handleEdit,
}: BacklogDrawerProps) => {
  const [backlogTasks, setBacklogTasks] = useState<Task[]>([]);
  const [selectedSprintForTask, setSelectedSprintForTask] = useState<{
    [taskId: number]: number;
  }>({});

  const handleMoveToSprint = (task: Task, sprintId: number) => {
    if (!sprintId) return;

    // Update the task to move it to the selected sprint
    // TODO: Change updateTask to just give the attributes that are needed and not the entire task.
    updateTask(task.id_Task, {
      ...task,
      id_Sprint: sprintId,
    })
      .then(() => {
        setBacklogTasks((prevTasks) =>
          prevTasks.filter((t) => t.id_Task !== task.id_Task),
        );
        setSelectedSprintForTask((prev) => {
          const newState = { ...prev };
          delete newState[task.id_Task]; // Remove the task from selectedSprintForTask
          return newState;
        });
        handleEdit({
          ...task,
          id_Sprint: sprintId,
        }); // Update the task in the parent component
      })
      .catch((error) => {
        console.error("Failed to update task:", error);
      });
  };

  useEffect(() => {
    // Filter tasks where id_Sprint is exactly -1
    // Also ensure they are in a pending state ('TODO' or 'IN_PROGRESS')
    const filteredTasks = tasks.filter((task) => {
      const isPending = task.state === "TODO" || task.state === "IN_PROGRESS";
      const isInGeneralBacklog = task.id_Sprint === -1;
      return isPending && isInGeneralBacklog;
    });

    // Sorts tasks by description
    filteredTasks.sort((a, b) => a.description.localeCompare(b.description));

    setBacklogTasks(filteredTasks);
  }, [tasks, open]); // Dependency array includes tasks and open state

  return (
    <Drawer open={open} onClose={() => onClose(false)} anchor="right">
      <Box role="presentation" className={styles.drawerContent}>
        <Subtitle>
          {" "}
          <Typography sx={{ color: "black" }}>
            Pending Backlog Tasks
          </Typography>{" "}
        </Subtitle>
        <Divider />
        {backlogTasks.length > 0 ? (
          <List>
            {backlogTasks.map((task) => (
              <ListItem
                key={task.id_Task}
                disablePadding
                sx={{ alignItems: "flex-start" }}
              >
                <ListItemText
                  primary={task.description}
                  secondary={`Created on: ${
                    sprints.find((s) => s.id_Sprint === task.id_Sprint)
                      ?.startsAt || "(No date available)"
                  }`}
                />
                <FormControl size="small" sx={{ minWidth: 120, ml: 2 }}>
                  <Select
                    displayEmpty
                    value={selectedSprintForTask[task.id_Task] || ""}
                    onChange={(e) =>
                      setSelectedSprintForTask((prev) => ({
                        ...prev,
                        [task.id_Task]: Number(e.target.value),
                      }))
                    }
                    renderValue={(selected) =>
                      selected
                        ? sprints.find((s) => s.id_Sprint === selected)?.name
                        : "Add to Sprint"
                    }
                  >
                    {sprints
                      .filter((s) => s.id_Sprint !== -1)
                      .map((sprint) => (
                        <MenuItem
                          key={sprint.id_Sprint}
                          value={sprint.id_Sprint}
                        >
                          {sprint.name}
                        </MenuItem>
                      ))}
                  </Select>
                </FormControl>
                <IconButton
                  color="primary"
                  sx={{ ml: 1 }}
                  disabled={!selectedSprintForTask[task.id_Task]}
                  onClick={() =>
                    handleMoveToSprint(
                      task,
                      selectedSprintForTask[task.id_Task],
                    )
                  }
                >
                  <ArrowForwardIcon />
                </IconButton>
              </ListItem>
            ))}
          </List>
        ) : (
          <Typography sx={{ mt: 2 }}>
            No pending tasks found in active or future sprints.
          </Typography>
        )}
      </Box>
    </Drawer>
  );
};

export default BacklogDrawer;
