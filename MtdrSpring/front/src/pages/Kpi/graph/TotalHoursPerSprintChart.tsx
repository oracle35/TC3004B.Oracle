import React from "react";
import { Paper, Typography, Divider } from "@mui/material";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
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

interface TotalHoursPerSprintData {
  sprintName: string;
  totalHours: number;
}

interface TotalHoursPerSprintChartProps {
  data: TotalHoursPerSprintData[];
}

const TotalHoursPerSprintChart: React.FC<TotalHoursPerSprintChartProps> = ({
  data,
}) => {
  return (
    <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
      <Typography
        variant="h6"
        gutterBottom
        sx={{ display: "flex", alignItems: "center", fontWeight: "medium" }}
      >
        <AccessTimeIcon sx={{ mr: 1, color: "primary.main" }} /> Total Hours per
        Sprint
      </Typography>
      <Divider sx={{ my: 1.5 }} />
      {data.length > 0 ? (
        <ResponsiveContainer width="100%" height={300}>
          <BarChart
            data={data}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
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
              label={{
                value: "Total Hours",
                angle: -90,
                position: "insideLeft",
              }}
            />
            <Tooltip />
            <Legend verticalAlign="top" />
            <Bar
              dataKey="totalHours"
              fill="#8884d8"
              name="Total Hours"
              radius={[4, 4, 0, 0]}
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
            height: 300,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          No sprint hours data available.
        </Typography>
      )}
      <Typography
        variant="caption"
        sx={{
          color: "text.secondary",
          fontStyle: "italic",
          mb: 1,
          display: "block",
          textAlign: "right",
        }}
      >
        * Each sprint lasts 2 weeks.
      </Typography>
    </Paper>
  );
};

export default TotalHoursPerSprintChart;
