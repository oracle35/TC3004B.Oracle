import { useEffect, useState } from "react";
import {
  Typography,
  Box,
  Paper,
  List,
  ListItem,
  ListItemText,
} from "@mui/material";
import GroupIcon from "@mui/icons-material/Group";
import NavBar from "../../components/NavBar/NavBar";
import { getTasks } from "../../api/task";
import { getUsers } from "../../api/user";
import { Task } from "../../models/Task";
import { User } from "../../models/User";

interface EnrichedTask extends Task {
  deadline?: Date;
  remaining?: number;
}

const Pending = () => {
  const [pendingTasks, setPendingTasks] = useState<EnrichedTask[]>([]);
  const [users, setUsers] = useState<User[]>([]);

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
            created.getTime() + (task.hoursEstimated ?? 0) * 3600 * 1000,
          );
          const remaining = deadline.getTime() - Date.now();
          return { ...task, deadline, remaining };
        })
        .sort(
          (a: EnrichedTask, b: EnrichedTask) =>
            a.deadline!.getTime() - b.deadline!.getTime(),
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
    };
    if (remainingMs == null) return baseStyle;
    const hrs = remainingMs / (1000 * 3600);
    if (hrs < 48) {
      return {
        ...baseStyle,
        borderLeft: "6px solid #d32f2f",
        backgroundColor: "#ffebee",
      };
    }
    if (hrs < 96) {
      return {
        ...baseStyle,
        borderLeft: "6px solid #fbc02d",
        backgroundColor: "#fffde7",
      };
    }
    return {
      ...baseStyle,
      borderLeft: "6px solid #388e3c",
      backgroundColor: "#e8f5e9",
    };
  };

  return (
    <>
      <NavBar />
      <Box display="flex" alignItems="center" gap={1} p={2}>
        <GroupIcon sx={{ color: "#ccc", fontSize: 30 }} />
        <Typography variant="h5" color="#ccc" fontWeight="bold">
          Pending Tsks
        </Typography>
      </Box>

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
    </>
  );
};

export default Pending;
