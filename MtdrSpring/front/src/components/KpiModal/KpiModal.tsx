import { useEffect, useState } from "react";
import { Box, Modal, Typography } from "@mui/material";
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import { Sprint } from "../../models/Sprint";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { getSprints } from "../../api/sprint";

interface KpiModalProps {
  open: boolean;
  onClose: () => void;
  tasks: Task[];
  users: User[];
}

/**
 * Current KPI Modal. 
 * TODO: Remove this in favor of the /kpi route. 
 */

const KpiModal = ({ open, onClose, tasks, users }: KpiModalProps) => {
  const [sprints, setSprints] = useState<Sprint[]>([]);

  useEffect(() => {
    const fetchSprints = async () => {
      const data = await getSprints();
      setSprints(data);
    };
    fetchSprints();
  }, []);

  // Create a mapping from sprint ID to sprint name
  const sprintNameMap = sprints.reduce((acc, sprint) => {
    acc[sprint.id_Sprint] = sprint.name;
    return acc;
  }, {} as Record<number, string>);

  // Aggregate total hoursReal per user
  const hoursData = users.map(user => {
    const totalHours = tasks
      .filter(task => task.assignedTo === user.id_User && task.hoursReal)
      .reduce((sum, task) => sum + (task.hoursReal || 0), 0);
    return { name: user.name, totalHours };
  });

  // Aggregate count of DONE tasks per user
  const doneTasksData = users.map(user => {
    const count = tasks.filter(task => task.assignedTo === user.id_User && task.state === "DONE").length;
    return { name: user.name, doneTasks: count };
  });

  // Aggregate finished tasks per sprint (using state "DONE") and use sprint name
  const finishedTasksData = Object.values(
    tasks.filter(task => task.state === "DONE").reduce((acc, task) => {
      const sprintName = sprintNameMap[task.id_Sprint] || `Sprint ${task.id_Sprint}`;
      if (acc[task.id_Sprint]) {
        acc[task.id_Sprint].finishedTasks += 1;
      } else {
        acc[task.id_Sprint] = { sprint: sprintName, finishedTasks: 1 };
      }
      return acc;
    }, {} as Record<number, { sprint: string; finishedTasks: number }>)
  ); 

  return (
    <Modal open={open} onClose={onClose}>
      <Box sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: 1200,
          maxHeight: '90vh',
          overflowY: 'auto',
          bgcolor: 'background.paper',
          border: '2px solid #000',
          boxShadow: 24,
          p: 4,
          color: '#000',
      }}>
        <Typography variant="h6" component="h2">
          KPI Modal
        </Typography>

        <Typography sx={{ mt: 2 }}>
          Hours Real Per User
        </Typography>
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

        <Typography sx={{ mt: 4 }}>
          Completed Tasks Per User
        </Typography>
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
        
        <Typography sx={{ mt: 4 }}>
          Finished Tasks Per Sprint
        </Typography>
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
    </Modal>
  );
};

export default KpiModal;