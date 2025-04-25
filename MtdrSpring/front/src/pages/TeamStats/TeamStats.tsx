import { useEffect, useState } from "react";
import {
  Box,
  CircularProgress,
  Typography,
  Alert,
  Paper, // Import Paper for card-like sections
  Divider, // Import Divider for separation
  List, // Import List components for user availability
  ListItem,
  ListItemText,
  Grid, // Import Grid for layout
} from "@mui/material";
import { User } from "../../models/User";
import { Task } from "../../models/Task";
import { Sprint } from "../../models/Sprint";
import { UserAvailability } from "../../models/UserAvailability";
import { getUsers } from "../../api/user";
import { getTasks } from "../../api/task";
import { getCurrentSprint } from "../../utils/sprint";
import { getUserAvailability } from "../../api/userAvailability";
import ErrorMessage from "../../components/ErrorMessage/ErrorMessage";
import { Subtitle } from "../../components/Subtitle";
import AssessmentIcon from '@mui/icons-material/Assessment';
import GroupIcon from '@mui/icons-material/Group'; 
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import NavBar from "../../components/NavBar/NavBar.tsx"; 
import MainTitle from "../../components/MainTitle.tsx";

const TeamStats = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [, setTasks] = useState<Task[]>([]);
  const [currentSprint, setCurrentSprint] = useState<Sprint | null>(null);
  const [userAvailability, setUserAvailability] = useState<UserAvailability[]>(
    []
  );
  const [weeklyAvailableSum, setWeeklyAvailableSum] = useState<number>(0);
  const [totalEstimatedHours, setTotalEstimatedHours] = useState<number>(0);
  const [totalAvailableHours, setTotalAvailableHours] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError("");
      try {
        const [usersData, tasksData, currentSprintData, availabilityData] =
          await Promise.all([
            getUsers(),
            getTasks(),
            getCurrentSprint(),
            getUserAvailability(),
          ]);

        console.log("Fetched Users:", usersData);
        console.log("Fetched Availability:", availabilityData);

        setUsers(usersData);
        setTasks(tasksData);
        setCurrentSprint(currentSprintData || null);
        setUserAvailability(availabilityData);

        const weeklySum = availabilityData.reduce(
          (sum, availability) =>
            sum + (availability.available_HOURS ?? availability.available_HOURS ?? 0), // Use nullish coalescing
          0
        );
        setWeeklyAvailableSum(weeklySum);

        const sprintAvailableHours = weeklySum * 2;
        setTotalAvailableHours(sprintAvailableHours);

        if (currentSprintData) {
          const sprintTasks = tasksData.filter(
            (task: Task) => task.id_Sprint === currentSprintData.id_Sprint
          );
          const estimatedSum = sprintTasks.reduce(
            (sum: number, task: Task) => sum + (task.hoursEstimated || 0),
            0
          );
          setTotalEstimatedHours(estimatedSum);
        } else {
          setTotalEstimatedHours(0);
          setError("No active sprint found to calculate statistics for.");
        }
      } catch (err) {
        console.error("Error fetching team stats data:", err);
        setError("Failed to load team statistics data.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, margin: 'auto' }}> 
        <MainTitle>Sprint Overview</MainTitle>

        <CircularProgress sx={{ mt: 4 }} />
      </Box>
    );
  }

  const exceedsCapacity = totalEstimatedHours > totalAvailableHours;

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, margin: 'auto' }}> 
      <Grid container spacing={1} alignItems="center" mb={2}>
        <Grid item xs>
           <MainTitle><AssessmentIcon sx={{ verticalAlign: 'middle', mr: 1 }}/> Sprint Overview</MainTitle>
          <NavBar />
        </Grid>
      </Grid>

      {currentSprint ? (
        <Subtitle><AccessTimeIcon sx={{ verticalAlign: 'middle', mr: 0.5, fontSize: '1.1rem', color: "white" }}/> Current Sprint: {currentSprint.name}</Subtitle>
      ) : (
        <Subtitle>No Active Sprint</Subtitle>
      )}

      {error && !currentSprint && <ErrorMessage error={error} />}

      {currentSprint && (
        <Grid container spacing={3} mt={1}> {/* Use Grid for layout */}
          {/* Sprint Capacity Section */}
          <Grid item xs={12} md={6}> {/* Takes full width on small, half on medium+ */}
            <Paper elevation={2} sx={{ p: 2.5, height: '100%' }}> {/* Use Paper for card look */}
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', fontWeight: 'medium' }}>
                 <AccessTimeIcon sx={{ mr: 1, color: 'primary.main' }} /> Sprint Capacity Overview (2 Weeks)
              </Typography>
              <Divider sx={{ my: 1.5 }} />
              <Box sx={{ mb: 2 }}>
                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                  Total Estimated Hours:{" "}
                  <Typography component="span" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                    {totalEstimatedHours.toFixed(1)}h
                  </Typography>
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                  Total Available Hours:{" "}
                   <Typography component="span" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                    {totalAvailableHours.toFixed(1)}h
                  </Typography>
                </Typography>
              </Box>
              <Alert
                severity={exceedsCapacity ? "warning" : "success"}
                variant="outlined" // Use outlined or filled for different look
                sx={{ textAlign: "left" }}
              >
                {exceedsCapacity
                  ? `The estimated workload (${totalEstimatedHours.toFixed(1)}h) exceeds the team's available capacity (${totalAvailableHours.toFixed(1)}h).`
                  : `The team has sufficient capacity (${totalAvailableHours.toFixed(1)}h) for the estimated workload (${totalEstimatedHours.toFixed(1)}h).`}
              </Alert>
            </Paper>
          </Grid>

          {/* User Availability Section */}
          <Grid item xs={12} md={6}> {/* Takes full width on small, half on medium+ */}
            <Paper elevation={2} sx={{ p: 2.5, height: '100%' }}> {/* Use Paper for card look */}
               <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', fontWeight: 'medium' }}>
                 <GroupIcon sx={{ mr: 1, color: 'primary.main' }} /> User Availability (Weekly)
              </Typography>
              <Divider sx={{ my: 1.5 }} />
              <Typography variant="body1" sx={{ mb: 1, fontWeight: 'medium' }}>
                Total Weekly Available:{" "}
                <Typography component="span" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                    {weeklyAvailableSum.toFixed(1)}h
                </Typography>
              </Typography>
              {users.length > 0 ? (
                <List dense disablePadding> {/* Use List for better structure */}
                  {users.map((user) => {
                    const availability = userAvailability.find(
                      (ua) => ua.id_USER == user.id_User // Loose equality
                    );
                    const hours = availability
                      ? availability.available_HOURS ?? availability.available_HOURS
                      : null;

                    return (
                      <ListItem key={user.id_User} disableGutters sx={{ py: 0.5 }}>
                        <ListItemText
                          primary={user.name}
                          secondary={
                            hours !== null && hours !== undefined
                              ? `${hours}h Available`
                              : "Availability N/A"
                          }
                          primaryTypographyProps={{ fontWeight: 'medium' }}
                        />
                      </ListItem>
                    );
                  })}
                </List>
              ) : (
                <Typography variant="body2" sx={{ fontStyle: 'italic', color: 'text.secondary' }}>
                  No user availability data found.
                </Typography>
              )}
            </Paper>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default TeamStats;