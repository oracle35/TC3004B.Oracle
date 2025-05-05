import React from "react";
import { Paper, Typography, Divider } from "@mui/material";
import GroupIcon from "@mui/icons-material/Group";
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

interface TeamPerformanceData {
  sprintName: string;
  completedTasks: number;
  totalRealHours: number;
}

interface TeamPerformanceChartProps {
  data: TeamPerformanceData[];
}

const TeamPerformanceChart: React.FC<TeamPerformanceChartProps> = ({
  data,
}) => {
  return (
    <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
      <Typography
        variant="h6"
        gutterBottom
        sx={{ display: "flex", alignItems: "center", fontWeight: "medium" }}
      >
        <GroupIcon sx={{ mr: 1, color: "info.main" }} /> Team Performance per
        Sprint
      </Typography>
      <Divider sx={{ my: 1.5 }} />
      {data.length > 0 ? (
        <ResponsiveContainer width="100%" height={300}>
          <BarChart
            data={data}
            margin={{ top: 5, right: 0, left: -20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis
              dataKey="sprintName"
              angle={-30}
              textAnchor="end"
              height={70}
              interval={0}
              fontSize={12}
            />
            <YAxis
              yAxisId="left"
              orientation="left"
              stroke="#8884d8"
              label={{
                value: "Tasks",
                angle: -90,
                position: "insideLeft",
              }}
            />
            <YAxis
              yAxisId="right"
              orientation="right"
              stroke="#82ca9d"
              label={{
                value: "Hours",
                angle: -90,
                position: "insideRight",
              }}
            />
            <Tooltip />
            <Legend verticalAlign="top" />
            <Bar
              yAxisId="left"
              dataKey="completedTasks"
              fill="#8884d8"
              name="Completed Tasks"
            />
            <Bar
              yAxisId="right"
              dataKey="totalRealHours"
              fill="#82ca9d"
              name="Total Real Hours"
            />
          </BarChart>
        </ResponsiveContainer>
      ) : (
        <Typography
          sx={{
            p: 2,
            fontStyle: "italic",
            color: "text.secondary",
            textAlign: "center",
            height: 300, // Match chart height for consistency
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          No team performance data available.
        </Typography>
      )}
    </Paper>
  );
};

export default TeamPerformanceChart;
