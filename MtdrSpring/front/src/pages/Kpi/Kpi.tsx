/**
 * KPI Page
 */

import { useEffect, useState } from "react";
import MainTitle from "../../components/MainTitle";
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import { getTasks } from "../../api/task";
import { getUsers } from "../../api/user";
import { getSprints } from "../../api/sprint";
import { Sprint } from "../../models/Sprint";
import { Box, CircularProgress, Typography } from "@mui/material";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  XAxis,
  YAxis,
  Tooltip,
} from "recharts";
import ReturnButton from "../../components/ReturnButton/ReturnButton";
// import styles from "./Kpi.module.css";

const KPIPage = () => {
  const [loading, setLoading] = useState<boolean>(false);

  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [sprints, setSprints] = useState<Sprint[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [tasksData, usersData, sprintData] = await Promise.all([
          getTasks(),
          getUsers(),
          getSprints(),
        ]);
        setTasks(tasksData);
        setUsers(usersData);
        setSprints(sprintData);
      } catch (error) {
        console.error("Error fetching data:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // Create a mapping from sprint ID to sprint name
  const sprintNameMap = sprints.reduce((acc, sprint) => {
    acc[sprint.id_Sprint] = sprint.name;
    return acc;
  }, {} as Record<number, string>);

  const hoursData = users.map((user) => {
    const totalHours = tasks
      .filter((task) => task.assignedTo === user.id_User && task.hoursReal)
      .reduce((sum, task) => sum + (task.hoursReal || 0), 0);
    return { name: user.name, totalHours };
  });

  const doneTasksData = users.map((user) => {
    const count = tasks.filter(
      (task) => task.assignedTo === user.id_User && task.state === "DONE"
    ).length;
    return { name: user.name, doneTasks: count };
  });

  const finishedTasksData = Object.values(
    tasks
      .filter((task) => task.state === "DONE")
      .reduce((acc, task) => {
        const sprintName =
          sprintNameMap[task.id_Sprint] || `Sprint ${task.id_Sprint}`;
        if (acc[task.id_Sprint]) {
          acc[task.id_Sprint].finishedTasks += 1;
        } else {
          acc[task.id_Sprint] = { sprint: sprintName, finishedTasks: 1 };
        }
        return acc;
      }, {} as Record<number, { sprint: string; finishedTasks: number }>)
  );

  if (loading) {
    return (
      <div>
        <MainTitle>KPI and Statistics</MainTitle>
        <CircularProgress />
      </div>
    );
  }

  return (
    <div>
      <ReturnButton />
      <MainTitle>KPI and Statistics</MainTitle>
      <Box>
        <Typography sx={{ mt: 2 }}>Hours Real Per User</Typography>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={hoursData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="totalHours" fill="#8884d8" name="Total Hours" />
          </BarChart>
        </ResponsiveContainer>

        <Typography sx={{ mt: 4 }}>Completed Tasks Per User</Typography>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={doneTasksData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="doneTasks" fill="#82ca9d" name="Completed Tasks" />
          </BarChart>
        </ResponsiveContainer>

        <Typography sx={{ mt: 4 }}>Finished Tasks Per Sprint</Typography>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={finishedTasksData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="sprint" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="finishedTasks" fill="#44bb88" name="Finished Tasks" />
          </BarChart>
        </ResponsiveContainer>
      </Box>
    </div>
  );
};

export default KPIPage;
