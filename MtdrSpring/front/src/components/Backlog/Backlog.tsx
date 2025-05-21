import {
  Drawer,
  List,
  ListItem,
  ListItemText,
  Typography,
  Box,
  Divider,
} from "@mui/material";
import { Task } from "../../models/Task";
import { Sprint } from "../../models/Sprint";
import { useEffect, useState } from "react";
import styles from "./Backlog.module.css";
import { Subtitle } from "../Subtitle";

interface BacklogDrawerProps {
  open: boolean;
  onClose: (arg0: boolean) => void;
  tasks: Task[];
  sprints: Sprint[];
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
}: BacklogDrawerProps) => {
  const [backlogTasks, setBacklogTasks] = useState<Task[]>([]);

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
              <ListItem key={task.id_Task} disablePadding>
                <ListItemText
                  primary={task.description}
                  secondary={`Sprint: ${
                    sprints.find((s) => s.id_Sprint === task.id_Sprint)?.name ||
                    "Unassigned"
                  }`}
                />
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
