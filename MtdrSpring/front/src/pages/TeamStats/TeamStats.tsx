import { useEffect, useState } from "react";
import { Box, CircularProgress, Typography, Alert } from "@mui/material";
import MainTitle from "../../components/MainTitle";
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
import ReturnButton from "../../components/ReturnButton/ReturnButton";

const TeamStats = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [, setTasks] = useState<Task[]>([]);
  const [currentSprint, setCurrentSprint] = useState<Sprint | null>(null);
  const [userAvailability, setUserAvailability] = useState<UserAvailability[]>(
    []
  );
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
            getCurrentSprint(), // Fetch the single current sprint object
            getUserAvailability(), // Fetch all user availability records
          ]);

        console.log("Fetched Users:", usersData);
        console.log("Fetched Availability:", availabilityData);

        setUsers(usersData);
        setTasks(tasksData);
        setCurrentSprint(currentSprintData || null);
        setUserAvailability(availabilityData);

        if (currentSprintData) {
          const sprintTasks = tasksData.filter(
            (task: Task) => task.id_Sprint === currentSprintData.id_Sprint
          );

          const estimatedSum = sprintTasks.reduce(
            (sum: number, task: Task) => sum + (task.hoursEstimated || 0),
            0
          );
          setTotalEstimatedHours(estimatedSum);

          const availableSum = availabilityData.reduce(
            (sum, availability) => sum + (availability.available_HOURS || 0),
            0
          );
          setTotalAvailableHours(availableSum);
        } else {
          setTotalEstimatedHours(0);
          const availableSum = availabilityData.reduce(
            (sum, availability) => sum + (availability.available_HOURS || 0),
            0
          );
          setTotalAvailableHours(availableSum);
          setError("No active sprint found to calculate statistics for."); // Inform user
        }
      } catch (err) {
        console.error("Error fetching team stats data:", err);
        setError("Failed to load team statistics data.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []); // Run only on component mount

  if (loading) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "80vh",
          flexDirection: "column",
        }}
      >
        <MainTitle>Team Statistics</MainTitle>
        <CircularProgress />
      </Box>
    );
  }

  const exceedsCapacity = totalEstimatedHours > totalAvailableHours;

  return (
    <Box sx={{ p: 3 }}>
      {" "}
      <ReturnButton />
      <MainTitle>Team Statistics</MainTitle>
      {currentSprint ? (
        <Subtitle>Current Sprint: {currentSprint.name}</Subtitle>
      ) : (
        <Subtitle>No Active Sprint</Subtitle>
      )}
      {error && !currentSprint && <ErrorMessage error={error} />}{" "}
      {currentSprint && ( // Only show stats if there's a current sprint
        <>
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
            Sprint Capacity Overview
          </Typography>
          <Box sx={{ mb: 2, pl: 2 }}>
            <Typography variant="body1">
              <strong>Total Estimated Hours in Sprint:</strong>{" "}
              {totalEstimatedHours.toFixed(1)}h
            </Typography>
            <Typography variant="body1">
              <strong>Total Team Available Hours:</strong>{" "}
              {totalAvailableHours.toFixed(1)}h
            </Typography>
          </Box>
          <Box sx={{ my: 2 }}>
            {exceedsCapacity ? (
              <Alert severity="warning" sx={{ fontSize: "1rem" }}>
                The total estimated hours ({totalEstimatedHours.toFixed(1)}h)
                for the current sprint exceed the team&apos;s total available
                hours ({totalAvailableHours.toFixed(1)}h).
              </Alert>
            ) : (
              <Alert severity="success" sx={{ fontSize: "1rem" }}>
                The team has sufficient capacity (
                {totalAvailableHours.toFixed(1)}h available) for the estimated
                workload ({totalEstimatedHours.toFixed(1)}h).
              </Alert>
            )}
          </Box>

          <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
            User Availability
          </Typography>
          <Box sx={{ display: "flex", flexDirection: "column", gap: 1, pl: 2 }}>
            {users.length > 0 ? ( // Check if users array is populated
              users.map((user) => {
                // Find the availability record for the current user
                // Use loose equality (==) for potential type mismatch (number vs string)
                const availability = userAvailability.find(
                  (ua) => ua.id_USER == user.id_User
                );

                // Determine the correct property name for available hours
                // Use nullish coalescing (??) to handle 0 correctly
                const hours = availability
                  ? availability.available_HOURS ?? availability.available_HOURS
                  : null; // Use null if availability not found

                return (
                  <Typography key={user.id_User} variant="body2">
                    <strong>{user.name}:</strong>{" "}
                    {hours !== null && hours !== undefined
                      ? `${hours}h`
                      : "N/A"}
                  </Typography>
                );
              })
            ) : (
              <Typography variant="body2">No user data available.</Typography>
            )}
          </Box>
        </>
      )}
    </Box>
  );
};

export default TeamStats;
