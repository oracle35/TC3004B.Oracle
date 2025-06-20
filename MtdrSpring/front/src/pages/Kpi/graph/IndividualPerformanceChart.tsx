/* eslint-disable react/prop-types */
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  TooltipProps,
  XAxis,
  YAxis,
} from "recharts";
import { Box, Grid, Paper, Typography, useTheme } from "@mui/material";
import {
  NameType,
  ValueType,
} from "recharts/types/component/DefaultTooltipContent";

interface IndividualPerformanceData {
  sprintName: string;
  [key: string]: string | number;
}

interface IndividualPerformanceChartProps {
  data: Record<
    string,
    Record<string, { completedTasks: number; realHours: number }>
  >;
}

const CustomTooltip = ({
  active,
  payload,
}: TooltipProps<ValueType, NameType>) => {
  const theme = useTheme();
  const isDark = theme.palette.mode === "dark";

  if (active && payload && payload.length) {
    const entry = payload[0];
    const sprintName = entry.payload.sprintName;
    const isTasksChart = entry.name?.toString().includes("Tasks");

    // Calculate team total for this sprint
    const teamTotal = payload.reduce((sum, p) => sum + (p.value as number), 0);

    return (
      <Paper
        elevation={3}
        sx={{
          p: 2,
          // Adjusted color to fix with the rest of the tooltips from other graphs that do not have a custom tooltip.
          backgroundColor: isDark ? "#23201D" : theme.palette.background.paper,
          boxShadow: isDark
            ? "0px 2px 4px rgba(0, 0, 0, 0.3)"
            : "0px 2px 4px rgba(0, 0, 0, 0.1)",
          color: theme.palette.text.primary,
          border: `1px solid ${isDark ? "#555" : "#ccc"}`,
        }}
      >
        <Typography>{sprintName}</Typography>
        <Typography
          variant="body2"
          sx={{
            color: theme.palette.text.primary,
          }}
        >
          {isTasksChart ? "Total Completed Tasks: " : "Total Hours: "}
          {teamTotal}
        </Typography>
      </Paper>
    );
  }
  return null;
};

const formatLabel = (label: string) => {
  const words = label.split(" ");
  if (words.length > 3) {
    return words.reduce((acc, word, index) => {
      if (index > 0 && index % 3 === 0) {
        return acc + "\n" + word;
      }
      return acc + (index === 0 ? word : " " + word);
    }, "");
  }
  return label;
};

const IndividualPerformanceChart: React.FC<IndividualPerformanceChartProps> = ({
  data,
}) => {
  const theme = useTheme();
  const isDark = theme.palette.mode === "dark";
  const users = Object.keys(Object.values(data)[0] || {});
  const colors = [
    "#1976D2", // Material Blue 700
    "#F44336", // Material Red 500
    "#43A047", // Material Green 600
    "#FFB300", // Material Amber 600
    "#8E24AA", // Material Purple 600
    "#00ACC1", // Material Cyan 600
  ];

  // Transform data into a single array, excluding "Backlog / Unassigned"
  const chartData = Object.entries(data)
    .filter(([sprintName]) => sprintName !== "Backlog / Unassigned")
    .map(([sprintName, userData]) => {
      const sprintData: IndividualPerformanceData = { sprintName };
      users.forEach((user) => {
        sprintData[`${user} - Tasks`] = userData[user].completedTasks;
        sprintData[`${user} - Hours`] = userData[user].realHours;
      });
      return sprintData;
    });

  return (
    <Grid container spacing={2}>
      {/* Completed Tasks Chart */}
      <Grid item xs={12} md={6}>
        <Paper elevation={3} sx={{ p: 2, height: "100%" }}>
          <Typography variant="subtitle1" gutterBottom>
            Completed Tasks by Sprint
          </Typography>
          <Box sx={{ width: "100%", height: 400 }}>
            <ResponsiveContainer>
              <BarChart
                data={chartData}
                margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                barGap={4}
                barCategoryGap="20%"
              >
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke={isDark ? "#444" : "#ccc"}
                />
                <XAxis
                  dataKey="sprintName"
                  textAnchor="end"
                  height={100}
                  interval={0}
                  tick={{ fontSize: 12, fill: isDark ? "#fff" : "#222" }}
                  tickFormatter={formatLabel}
                />
                <YAxis
                  orientation="left"
                  stroke="#8884d8"
                  label={{
                    value: "Completed Tasks",
                    angle: -90,
                    position: "insideLeft",
                    style: { fontSize: 12, fill: isDark ? "#fff" : "#222" },
                  }}
                  tick={{ fontSize: 12, fill: isDark ? "#fff" : "#222" }}
                />
                <Tooltip content={<CustomTooltip />} />

                <Legend
                  wrapperStyle={{
                    fontSize: 12,
                    color: isDark ? "#fff" : "#222",
                  }}
                  formatter={(value) => formatLabel(value as string)}
                />
                {users.map((user, index) => (
                  <Bar
                    key={`${user}-tasks`}
                    dataKey={`${user} - Tasks`}
                    name={`${user} - Tasks`}
                    fill={colors[index % colors.length]}
                    radius={[4, 4, 0, 0]}
                  />
                ))}
              </BarChart>
            </ResponsiveContainer>
          </Box>
        </Paper>
      </Grid>

      {/* Real Hours Chart */}
      <Grid item xs={12} md={6}>
        <Paper elevation={3} sx={{ p: 2, height: "100%" }}>
          <Typography variant="subtitle1" gutterBottom>
            Real Hours by Sprint
          </Typography>
          <Box sx={{ width: "100%", height: 400 }}>
            <ResponsiveContainer>
              <BarChart
                data={chartData}
                margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                barGap={4}
                barCategoryGap="20%"
              >
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke={isDark ? "#444" : "#ccc"}
                />
                <XAxis
                  dataKey="sprintName"
                  textAnchor="end"
                  height={100}
                  interval={0}
                  tick={{ fontSize: 12, fill: isDark ? "#fff" : "#222" }}
                  tickFormatter={formatLabel}
                />
                <YAxis
                  orientation="left"
                  stroke="#82ca9d"
                  label={{
                    value: "Hours",
                    angle: -90,
                    position: "insideLeft",
                    style: { fontSize: 12 },
                  }}
                  tick={{ fontSize: 12, fill: isDark ? "#fff" : "#222" }}
                />
                <Tooltip content={<CustomTooltip />} />
                <Legend
                  wrapperStyle={{ fontSize: 12 }}
                  formatter={(value) => formatLabel(value as string)}
                />
                {users.map((user, index) => (
                  <Bar
                    key={`${user}-hours`}
                    dataKey={`${user} - Hours`}
                    name={`${user} - Hours`}
                    fill={colors[index % colors.length]}
                    radius={[4, 4, 0, 0]}
                  />
                ))}
              </BarChart>
            </ResponsiveContainer>
          </Box>
        </Paper>
      </Grid>
    </Grid>
  );
};

export default IndividualPerformanceChart;
