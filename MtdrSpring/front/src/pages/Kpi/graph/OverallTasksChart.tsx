import React from "react";
import { Paper, Typography, Divider } from "@mui/material";
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

interface OverallTasksData {
  name: string;
  doneTasks: number;
}

interface OverallTasksChartProps {
  data: OverallTasksData[];
}

const OverallTasksChart: React.FC<OverallTasksChartProps> = ({ data }) => {
  return (
    <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
      <Typography variant="h6" gutterBottom>
        Overall Completed Tasks Per User
      </Typography>
      <Divider sx={{ my: 1.5 }} />
      {data.length > 0 ? (
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis /> <Tooltip /> <Legend verticalAlign="top" />
            <Bar dataKey="doneTasks" fill="#82ca9d" name="Completed Tasks" />
          </BarChart>
        </ResponsiveContainer>
      ) : (
        <Typography
          sx={{
            p: 2,
            fontStyle: "italic",
            color: "text.secondary",
            textAlign: "center",
            height: 300, // Match chart height
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          No user task completion data available.
        </Typography>
      )}
    </Paper>
  );
};

export default OverallTasksChart;
