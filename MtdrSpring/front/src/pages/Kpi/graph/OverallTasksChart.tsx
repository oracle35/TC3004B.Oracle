import React from "react";
import { Divider, Paper, Typography, useTheme } from "@mui/material";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

interface OverallTasksData {
  name: string;
  doneTasks: number;
}

interface OverallTasksChartProps {
  data: OverallTasksData[];
  isDark?: boolean;
}

const OverallTasksChart: React.FC<OverallTasksChartProps> = ({ data }) => {
  const theme = useTheme();
  const isDark = theme.palette.mode === "dark";
  return (
    <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
      <Typography variant="h6" gutterBottom>
        Overall Completed Tasks Per User
      </Typography>
      <Divider sx={{ my: 1.5 }} />
      {data.length > 0 ? (
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={data}>
            <CartesianGrid
              strokeDasharray="3 3"
              stroke={isDark ? "#444" : "#ccc"}
            />
            <XAxis dataKey="name" />
            <YAxis />{" "}
            <Tooltip
              contentStyle={{
                background: isDark ? theme.palette.background.paper : "#fff",
                color: isDark ? "#fff" : "#222",
                border: `1px solid ${isDark ? "#555" : "#ccc"}`,
              }}
              labelStyle={{
                color: isDark ? "#fff" : "#222",
              }}
              itemStyle={{
                color: isDark ? "#fff" : "#222",
              }}
            />{" "}
            <Legend
              verticalAlign="top"
              wrapperStyle={{
                color: isDark ? "#fff" : "#222",
              }}
            />
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
