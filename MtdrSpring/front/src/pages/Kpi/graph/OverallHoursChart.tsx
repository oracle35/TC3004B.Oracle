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

interface OverallHoursData {
  name: string;
  totalHours: number;
}

interface OverallHoursChartProps {
  data: OverallHoursData[];
}

const OverallHoursChart: React.FC<OverallHoursChartProps> = ({ data }) => {
  return (
    <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
      <Typography variant="h6" gutterBottom>
        Overall Real Hours Per User
      </Typography>
      <Divider sx={{ my: 1.5 }} />
      {data.length > 0 ? (
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis /> <Tooltip /> <Legend verticalAlign="top"/>
            <Bar dataKey="totalHours" fill="#8884d8" name="Total Hours" />
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
          No user hour data available.
        </Typography>
      )}
    </Paper>
  );
};

export default OverallHoursChart;