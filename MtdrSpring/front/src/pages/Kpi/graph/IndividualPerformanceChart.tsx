/* eslint-disable react/prop-types */
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, TooltipProps } from 'recharts';
import { Paper, Typography, Box } from '@mui/material';
import { NameType, ValueType } from 'recharts/types/component/DefaultTooltipContent';

interface IndividualPerformanceData {
  sprintName: string;
  [key: string]: string | number;
}

interface IndividualPerformanceChartProps {
  data: Record<string, Record<string, { completedTasks: number; realHours: number }>>;
}

const CustomTooltip = ({ active, payload, label }: TooltipProps<ValueType, NameType>) => {
  if (active && payload && payload.length) {
    const entry = payload[0];
    const name = entry.name as string;
    const [developer, metric] = name.split(' - ');
    
    return (
      <Paper elevation={3} sx={{ p: 2, backgroundColor: 'rgba(255, 255, 255, 0.95)' }}>
        <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
          {developer} - {label}
        </Typography>
        <Typography variant="body2" sx={{ color: entry.color }}>
          {`${metric}: ${entry.value}`}
        </Typography>
      </Paper>
    );
  }
  return null;
};

const IndividualPerformanceChart: React.FC<IndividualPerformanceChartProps> = ({ data }) => {
  const users = Object.keys(Object.values(data)[0] || {});
  const colors = ['#8884d8', '#82ca9d', '#ffc658', '#ff8042', '#0088fe', '#00C49F'];

  // Transform data into a single array
  const chartData = Object.entries(data).map(([sprintName, userData]) => {
    const sprintData: IndividualPerformanceData = { sprintName };
    users.forEach(user => {
      sprintData[`${user} - Tasks`] = userData[user].completedTasks;
      sprintData[`${user} - Hours`] = userData[user].realHours;
    });
    return sprintData;
  });

  return (
    <Paper elevation={3} sx={{ p: 2, height: '100%' }}>
      <Typography variant="h6" gutterBottom>
        Individual Performance by Sprint
      </Typography>
      <Box sx={{ width: '100%', height: 500 }}>
        <ResponsiveContainer>
          <BarChart
            data={chartData}
            margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
            barGap={0}
            barCategoryGap={0.1}
          >
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis 
              dataKey="sprintName" 
              textAnchor="end" 
              height={100}
              interval={0}
            />
            <YAxis 
              yAxisId="left" 
              orientation="left" 
              stroke="#8884d8"
              label={{ value: 'Completed Tasks', angle: -90, position: 'insideLeft' }}
            />
            <YAxis 
              yAxisId="right" 
              orientation="right" 
              stroke="#82ca9d"
              label={{ value: 'Hours', angle: 90, position: 'insideRight' }}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend />
            {users.map((user, index) => (
              <Bar
                key={`${user}-tasks`}
                yAxisId="left"
                dataKey={`${user} - Tasks`}
                name={`${user} - Tasks`}
                fill={colors[index % colors.length]}
                radius={[4, 4, 0, 0]}
                opacity={0.8}
              />
            ))}
            {users.map((user, index) => (
              <Bar
                key={`${user}-hours`}
                yAxisId="right"
                dataKey={`${user} - Hours`}
                name={`${user} - Hours`}
                fill={colors[index % colors.length]}
                radius={[4, 4, 0, 0]}
                opacity={0.4}
              />
            ))}
          </BarChart>
        </ResponsiveContainer>
      </Box>
    </Paper>
  );
};

export default IndividualPerformanceChart; 