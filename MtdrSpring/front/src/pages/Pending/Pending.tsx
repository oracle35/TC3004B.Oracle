import { useEffect, useState } from "react";
import {
  Typography,
  Box,
  Paper,
  List,
  ListItem,
  ListItemText,
  useTheme,
} from "@mui/material";
import ChecklistRtlIcon from "@mui/icons-material/ChecklistRtl";
import { getTasks } from "../../api/task";
import { getUsers } from "../../api/user";
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import Layout from "../Layout";

interface EnrichedTask extends Task {
  deadline?: Date;
  remaining?: number;
}

const Pending = () => {
  const [pendingTasks, setPendingTasks] = useState<EnrichedTask[]>([]);
  const [users, setUsers] = useState<User[]>([]);

  const theme = useTheme();

  useEffect(() => {
    async function fetchData() {
      const [tasks, fetchedUsers] = await Promise.all([getTasks(), getUsers()]);
      if (!tasks || !fetchedUsers) return;

      setUsers(fetchedUsers);

      const filtered = tasks
        .filter((task: Task) => task.state === "TODO")
        .map((task: Task) => {
          const created = new Date(task.createdAt as Date);
          const deadline = new Date(
            created.getTime() + (task.hoursEstimated ?? 0) * 3600 * 1000
          );
          const remaining = deadline.getTime() - Date.now();
          return { ...task, deadline, remaining };
        })
        .sort(
          (a: EnrichedTask, b: EnrichedTask) =>
            a.deadline!.getTime() - b.deadline!.getTime()
        );

      setPendingTasks(filtered);
    }

    fetchData();
  }, []);

  const getUserName = (userId: number) => {
    const user = users.find((u) => u.id_User === userId);
    return user ? user.name : "Unassigned";
  };
  const getPaperStyle = (remainingMs: number | undefined) => {
    const baseStyle = {
      marginBottom: "16px",
      padding: "12px",
      maxWidth: "600px",
      margin: "20px auto",
      backgroundColor: theme.palette.background.paper,
      color: theme.palette.text.primary,
      borderLeft: `6px solid ${theme.palette.divider}`,
      boxShadow: theme.shadows[2],
    };
    if (remainingMs == null) return baseStyle;
    const hrs = remainingMs / (1000 * 3600);
    if (hrs < 48) {
      return {
        ...baseStyle,
        borderLeft: `6px solid ${theme.palette.error.main}`,
        backgroundColor: theme.palette.mode === "dark" ? "#2d2323" : "#ffebee",
      };
    }
    if (hrs < 96) {
      return {
        ...baseStyle,
        borderLeft: `6px solid ${theme.palette.warning.main}`,
        backgroundColor: theme.palette.mode === "dark" ? "#2d271a" : "#fffde7",
      };
    }
    return {
      ...baseStyle,
      borderLeft: `6px solid ${theme.palette.success.main}`,
      backgroundColor: theme.palette.mode === "dark" ? "#1e2a22" : "#e8f5e9",
    };
  };
  return (
    <Layout title="Pending Tasks" icon={<ChecklistRtlIcon />}>
      <Box mt={2}>
        <List>
          {pendingTasks.map((task, index) => (
            <Paper key={index} sx={getPaperStyle(task.remaining)}>
              <ListItem dense>
                <ListItemText
                  primary={
                    <Typography fontWeight="bold">
                      {task.description}
                    </Typography>
                  }
                  secondary={
                    <>
                      <Typography variant="body2">
                        Assigned to: {getUserName(task.assignedTo)}
                      </Typography>
                      <Typography variant="body2">
                        Created:{" "}
                        {task.createdAt
                          ? new Date(task.createdAt).toLocaleString()
                          : "N/A"}
                      </Typography>
                      <Typography variant="body2">
                        Deadline: {task.deadline?.toLocaleString()}
                      </Typography>
                      <Typography variant="body2">
                        Remaining time:{" "}
                        {task.remaining != null
                          ? Math.round(task.remaining / (1000 * 3600)) + " hrs"
                          : "N/A"}
                      </Typography>
                    </>
                  }
                />
              </ListItem>
            </Paper>
          ))}
        </List>
      </Box>
    </Layout>
  );
};

export default Pending;
