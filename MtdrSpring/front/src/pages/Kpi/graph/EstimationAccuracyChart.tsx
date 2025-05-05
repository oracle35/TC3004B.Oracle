import React from "react";
import { Paper, Typography, Divider } from "@mui/material";
import RuleIcon from "@mui/icons-material/Rule";
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

interface EstimationAccuracyData {
  sprintName: string;
  totalEstimated: number;
  totalReal: number;
}

interface EstimationAccuracyChartProps {
  data: EstimationAccuracyData[];
}

const EstimationAccuracyChart: React.FC<EstimationAccuracyChartProps> = ({
  data,
}) => {
  return (
    <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
      <Typography
        variant="h6"
        gutterBottom
        sx={{ display: "flex", alignItems: "center", fontWeight: "medium" }}
      >
        <RuleIcon sx={{ mr: 1, color: "secondary.main" }} /> Estimation Accuracy
        per Sprint
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
              label={{
                value: "Hours",
                angle: -90,
                position: "insideLeft",
              }}
            />
            <Tooltip />
            <Legend verticalAlign="top" />
            <Bar
              dataKey="totalEstimated"
              fill="#ffc658"
              name="Total Estimated Hours"
            />
            <Bar dataKey="totalReal" fill="#ff7300" name="Total Real Hours" />
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
          No estimation accuracy data available.
        </Typography>
      )}
    </Paper>
  );
};

export default EstimationAccuracyChart;
