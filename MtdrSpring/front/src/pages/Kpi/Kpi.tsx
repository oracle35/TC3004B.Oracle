import { useEffect, useState, useMemo } from "react"; // Import useMemo
import MainTitle from "../../components/MainTitle";
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import { getTasks } from "../../api/task";
import { getUsers } from "../../api/user";
import { getSprints } from "../../api/sprint";
import { Sprint } from "../../models/Sprint";
import {
  Box,
  CircularProgress,
  Typography,
  Grid,
  Paper,
  Divider,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Alert, // Import Alert
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
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
import ReturnButton from "../../components/ReturnButton/ReturnButton";
import AssessmentIcon from "@mui/icons-material/Assessment";
import GroupIcon from "@mui/icons-material/Group";
import PersonIcon from "@mui/icons-material/Person";
import PlaylistAddCheckIcon from "@mui/icons-material/PlaylistAddCheck";
import RuleIcon from "@mui/icons-material/Rule";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome"; // Import AI icon
import { GoogleGenerativeAI } from "@google/generative-ai";

const GEMINI_API_KEY = import.meta.env.VITE_GEMINI_API_KEY;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let model: any = null;
if (GEMINI_API_KEY && GEMINI_API_KEY !== "SecondOptionKey") {
  try {
    const genAI = new GoogleGenerativeAI(GEMINI_API_KEY);
    model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
  } catch (error) {
    console.error("Failed to initialize GoogleGenerativeAI:", error);
  }
} else {
  console.warn("Gemini API Key not found. AI features will be disabled.");
}

const getUserName = (userId: number, users: User[]) => {
  const user = users.find((u) => u.id_User === userId);
  return user ? user.name : "Unknown User";
};

const getSprintName = (sprintId: number, sprints: Sprint[]) => {
  if (sprintId === -1) return "Backlog / Unassigned";
  const sprint = sprints.find((s) => s.id_Sprint === sprintId);
  return sprint ? sprint.name : `Sprint ${sprintId}`;
};

const KPIPage = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [sprints, setSprints] = useState<Sprint[]>([]);
  const [aiSummary, setAiSummary] = useState<string>("");
  const [aiLoading, setAiLoading] = useState<boolean>(false);
  const [aiError, setAiError] = useState<string | null>(null);

  // Effect for fetching initial data
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      if (model) {
        // Only set AI loading if model is initialized
        setAiLoading(true);
        setAiError(null);
        setAiSummary("");
      }
      try {
        const [tasksData, usersData, sprintData] = await Promise.all([
          getTasks(),
          getUsers(),
          getSprints(),
        ]);
        setTasks(tasksData);

        setUsers(usersData);
        // Sort sprints for consistent order
        setSprints(sprintData.sort((a, b) => a.name.localeCompare(b.name)));
      } catch (error) {
        console.error("Error fetching data:", error);
        setAiError("Failed to load project data."); // Set error if initial data fails
      } finally {
        setLoading(false);
        if (!model) {
          setAiLoading(false); // Stop AI loading if model wasn't initialized
          setAiError(
            "AI Summary feature is disabled (API key missing or invalid)."
          );
        }
      }
    };
    fetchData();
  }, []); // Empty dependency array means this runs once on mount

  // Memoized calculations for KPIs
  const completedTasksBySprint = useMemo(() => {
    const completed = tasks.filter((task) => task.state === "DONE");
    return completed.reduce((acc, task) => {
      const sprintName = getSprintName(task.id_Sprint, sprints);
      if (!acc[sprintName]) {
        acc[sprintName] = [];
      }
      acc[sprintName].push({
        id: task.id_Task,
        name: task.description,
        developer: getUserName(task.assignedTo, users),
        estimated: task.hoursEstimated ? task.hoursEstimated : 0,
        real: task.hoursReal,
      });
      return acc;
    }, {} as Record<string, Array<{ id: number; name: string; developer: string; estimated: number; real: number | null }>>);
  }, [tasks, users, sprints]);

  const teamPerformancePerSprint = useMemo(() => {
    const sprintStats = sprints.reduce((acc, sprint) => {
      acc[sprint.id_Sprint] = {
        sprintName: sprint.name,
        completedTasks: 0,
        totalRealHours: 0,
      };
      return acc;
    }, {} as Record<number, { sprintName: string; completedTasks: number; totalRealHours: number }>);

    sprintStats[-1] = {
      sprintName: "Backlog / Unassigned",
      completedTasks: 0,
      totalRealHours: 0,
    };

    tasks
      .filter((task) => task.state === "DONE")
      .forEach((task) => {
        const sprintId = task.id_Sprint ?? -1; // Handle potential null/undefined sprint IDs
        if (sprintStats[sprintId]) {
          sprintStats[sprintId].completedTasks += 1;
          sprintStats[sprintId].totalRealHours += task.hoursReal || 0;
        } else if (sprintStats[-1] && sprintId === -1) {
          // Explicit check for -1
          sprintStats[-1].completedTasks += 1;
          sprintStats[-1].totalRealHours += task.hoursReal || 0;
        }
        // Consider logging if a task's sprintId doesn't match any known sprint or -1
      });

    return Object.values(sprintStats).sort((a, b) =>
      a.sprintName.localeCompare(b.sprintName)
    );
  }, [tasks, sprints]);

  const individualPerformancePerSprint = useMemo(() => {
    const performance: Record<
      string,
      Record<string, { completedTasks: number; realHours: number }>
    > = {};

    // Initialize for known sprints
    sprints.forEach((sprint) => {
      const sprintName = sprint.name;
      performance[sprintName] = {};
      users.forEach((user) => {
        performance[sprintName][user.name] = {
          completedTasks: 0,
          realHours: 0,
        };
      });
    });

    // Initialize for Backlog/Unassigned
    const backlogSprintName = "Backlog / Unassigned";
    performance[backlogSprintName] = {};
    users.forEach((user) => {
      performance[backlogSprintName][user.name] = {
        completedTasks: 0,
        realHours: 0,
      };
    });

    // Populate data
    tasks
      .filter((task) => task.state === "DONE")
      .forEach((task) => {
        const sprintName = getSprintName(task.id_Sprint, sprints);
        const userName = getUserName(task.assignedTo, users);

        if (performance[sprintName] && performance[sprintName][userName]) {
          performance[sprintName][userName].completedTasks += 1;
          performance[sprintName][userName].realHours += task.hoursReal || 0;
        } else {
          // This case might happen if a user completed a task but is no longer in the users list,
          // or if a task belongs to a sprint not in the sprints list (edge case).
          // console.warn(`Could not map task ${task.id_Task} to sprint/user combination: ${sprintName}/${userName}`);
        }
      });

    return performance;
  }, [tasks, users, sprints]);

  const estimationAccuracyPerSprint = useMemo(() => {
    const accuracyStats = sprints.reduce((acc, sprint) => {
      acc[sprint.id_Sprint] = {
        sprintName: sprint.name,
        totalEstimated: 0,
        totalReal: 0,
      };
      return acc;
    }, {} as Record<number, { sprintName: string; totalEstimated: number; totalReal: number }>);

    accuracyStats[-1] = {
      sprintName: "Backlog / Unassigned",
      totalEstimated: 0,
      totalReal: 0,
    };

    tasks
      .filter((task) => task.state === "DONE")
      .forEach((task) => {
        const sprintId = task.id_Sprint ?? -1; // Handle potential null/undefined
        if (accuracyStats[sprintId]) {
          accuracyStats[sprintId].totalEstimated += task.hoursEstimated || 0;
          accuracyStats[sprintId].totalReal += task.hoursReal || 0;
        } else if (accuracyStats[-1] && sprintId === -1) {
          // Explicit check for -1
          accuracyStats[-1].totalEstimated += task.hoursEstimated || 0;
          accuracyStats[-1].totalReal += task.hoursReal || 0;
        }
      });

    return Object.values(accuracyStats)
      .filter((s) => s.totalEstimated > 0 || s.totalReal > 0) // Only show sprints with data
      .sort((a, b) => a.sprintName.localeCompare(b.sprintName));
  }, [tasks, sprints]);

  const totalHoursPerUser = useMemo(
    () =>
      users.map((user) => {
        const totalHours = tasks
          .filter((task) => task.assignedTo === user.id_User && task.hoursReal)
          .reduce((sum, task) => sum + (task.hoursReal || 0), 0);
        return { name: user.name, totalHours };
      }),
    [tasks, users]
  );

  const totalCompletedTasksPerUser = useMemo(
    () =>
      users.map((user) => {
        const count = tasks.filter(
          (task) => task.assignedTo === user.id_User && task.state === "DONE"
        ).length;
        return { name: user.name, doneTasks: count };
      }),
    [tasks, users]
  );

  // --- AI Summary Generation Effect ---
  useEffect(() => {
    // Only run if the model is initialized, not loading data, and data exists
    if (
      !model ||
      loading ||
      !tasks.length ||
      !users.length ||
      !sprints.length
    ) {
      // If data loading is finished but data is empty, stop AI loading
      if (!loading && model) setAiLoading(false);
      return;
    }

    const generateAiSummary = async () => {
      // Ensure AI loading is true before starting generation
      if (!aiLoading) setAiLoading(true);
      setAiError(null); // Clear previous errors

      // Prepare data for the prompt
      const teamPerfSummary = teamPerformancePerSprint
        .map(
          (s) =>
            `- ${s.sprintName}: ${
              s.completedTasks
            } tasks completed, ${s.totalRealHours.toFixed(1)} total real hours.`
        )
        .join("\n");

      const individualPerfSummary = Object.entries(
        individualPerformancePerSprint
      )
        .map(([sprintName, usersData]) => {
          const userLines = Object.entries(usersData)
            .filter(([, data]) => data.completedTasks > 0 || data.realHours > 0)
            .map(
              ([userName, data]) =>
                `  - ${userName}: ${
                  data.completedTasks
                } tasks, ${data.realHours.toFixed(1)}h`
            )
            .join("\n");
          return userLines ? `${sprintName}:\n${userLines}` : null;
        })
        .filter(Boolean)
        .join("\n\n");

      const estimationAccSummary = estimationAccuracyPerSprint
        .map(
          (s) =>
            `- ${s.sprintName}: Est. ${s.totalEstimated.toFixed(
              1
            )}h, Real ${s.totalReal.toFixed(1)}h`
        )
        .join("\n");

      const prompt = `
      Please provide a brief (2-3 sentences) summary of the following project Key Performance Indicators (KPIs). Focus on overall trends in team performance, individual contributions, and estimation accuracy across sprints.

      Team Performance per Sprint (Completed Tasks, Total Real Hours):
      ${teamPerfSummary || "No team performance data available."}

      Individual Performance per Sprint (Completed Tasks, Real Hours):
      ${individualPerfSummary || "No individual performance data available."}

      Estimation Accuracy per Sprint (Estimated vs Real Hours):
      ${estimationAccSummary || "No estimation accuracy data available."}

      Generate a concise summary:
      `;

      try {
        console.log("Sending prompt to Gemini:", prompt); // For debugging
        const result = await model.generateContent(prompt);
        const response = result.response;
        const text = response.text();
        console.log("Received summary from Gemini:", text); // For debugging
        setAiSummary(text);
      } catch (error) {
        console.error("Error generating AI summary:", error);
        setAiError(
          "Failed to generate AI summary. The API service might be unavailable or the request failed."
        );
        setAiSummary(""); // Clear any previous summary
      } finally {
        setAiLoading(false); // Stop loading indicator regardless of success/failure
      }
    };

    generateAiSummary();
  }, [
    loading, // Rerun when loading finishes
    // Include dependencies: data used in prompt generation and the model itself
    tasks,
    users,
    sprints,
    teamPerformancePerSprint,
    individualPerformancePerSprint,
    estimationAccuracyPerSprint,
    // Note: `model` is stable after initialization, but including it clarifies dependency
  ]);

  // --- Render Logic ---

  if (loading) {
    return (
      <Box sx={{ p: 3, textAlign: "center" }}>
        <MainTitle>KPI and Statistics</MainTitle>
        <CircularProgress sx={{ mt: 4 }} />
      </Box>
    );
  }

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1400, margin: "auto" }}>
      <Grid container spacing={1} alignItems="center" mb={2}>
        <Grid item>
          <ReturnButton />
        </Grid>
        <Grid item xs>
          <MainTitle>
            <AssessmentIcon sx={{ verticalAlign: "middle", mr: 1 }} /> KPI and
            Statistics
          </MainTitle>
        </Grid>
      </Grid>

      {/* AI Summary Section - Render only if model was initialized */}
      {model && (
        <Grid item xs={12} mb={3}>
          {" "}
          {/* Add margin bottom */}
          <Paper
            elevation={2}
            sx={{
              p: 2.5,
              borderColor: "primary.main",
              borderWidth: 1,
              borderStyle: "solid",
            }}
          >
            <Typography
              variant="h6"
              gutterBottom
              sx={{
                display: "flex",
                alignItems: "center",
                fontWeight: "medium",
              }}
            >
              <AutoAwesomeIcon sx={{ mr: 1, color: "primary.main" }} />{" "}
              AI-Generated Summary
            </Typography>
            <Divider sx={{ my: 1.5 }} />
            {aiLoading && (
              <CircularProgress
                size={24}
                sx={{ display: "block", margin: "auto" }}
              />
            )}
            {aiError && <Alert severity="error">{aiError}</Alert>}
            {!aiLoading && !aiError && aiSummary && (
              <Typography
                variant="body1"
                sx={{
                  fontStyle: "italic",
                  color: "text.secondary",
                  textAlign: "justify",
                }}
              >
                {aiSummary}
              </Typography>
            )}
            {!aiLoading && !aiError && !aiSummary && (
              <Typography
                variant="body2"
                sx={{ fontStyle: "italic", color: "text.secondary" }}
              >
                No summary available or data is insufficient for generation.
              </Typography>
            )}
          </Paper>
        </Grid>
      )}
      {/* Show message if AI is disabled */}
      {!model && (
        <Grid item xs={12} mb={3}>
          <Alert severity="info">
            AI Summary feature is disabled (API key missing or invalid).
          </Alert>
        </Grid>
      )}

      <Grid container spacing={3} mt={1}>
        {/* Section 1.1: Completed Task Details */}
        <Grid item xs={12}>
          <Paper elevation={2} sx={{ p: 2.5 }}>
            <Typography
              variant="h6"
              gutterBottom
              sx={{
                display: "flex",
                alignItems: "center",
                fontWeight: "medium",
              }}
            >
              <PlaylistAddCheckIcon sx={{ mr: 1, color: "success.main" }} />{" "}
              Completed Task Details per Sprint
            </Typography>
            <Divider sx={{ my: 1.5 }} />
            {Object.entries(completedTasksBySprint)
              .sort(([sprintA], [sprintB]) => sprintA.localeCompare(sprintB))
              .map(([sprintName, tasksInSprint]) => (
                <Accordion
                  key={sprintName}
                  sx={{
                    "&:before": { display: "none" },
                    boxShadow: "none",
                    borderBottom: "1px solid rgba(0, 0, 0, 0.12)",
                  }}
                >
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography sx={{ fontWeight: "medium" }}>
                      {sprintName} ({tasksInSprint.length} tasks)
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails sx={{ p: 0 }}>
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Task Name</TableCell>
                            <TableCell>Developer</TableCell>
                            <TableCell align="right">Est. Hours</TableCell>
                            <TableCell align="right">Real Hours</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {tasksInSprint.map((task) => (
                            <TableRow key={task.id}>
                              <TableCell component="th" scope="row">
                                {task.name}
                              </TableCell>
                              <TableCell>{task.developer}</TableCell>
                              <TableCell align="right">
                                {task.estimated}h
                              </TableCell>
                              <TableCell align="right">
                                {task.real !== null ? `${task.real}h` : "N/A"}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </AccordionDetails>
                </Accordion>
              ))}
            {Object.keys(completedTasksBySprint).length === 0 && (
              <Typography
                sx={{ p: 2, fontStyle: "italic", color: "text.secondary" }}
              >
                No completed tasks found.
              </Typography>
            )}
          </Paper>
        </Grid>

        {/* Section 1.2: Team Performance per Sprint */}
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
            <Typography
              variant="h6"
              gutterBottom
              sx={{
                display: "flex",
                alignItems: "center",
                fontWeight: "medium",
              }}
            >
              <GroupIcon sx={{ mr: 1, color: "info.main" }} /> Team Performance
              per Sprint
            </Typography>
            <Divider sx={{ my: 1.5 }} />
            <ResponsiveContainer width="100%" height={300}>
              <BarChart
                data={teamPerformancePerSprint}
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
                  label={{ value: "Tasks", angle: -90, position: "insideLeft" }}
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
          </Paper>
        </Grid>

        {/* Section 1.3: Estimation Accuracy per Sprint */}
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
            <Typography
              variant="h6"
              gutterBottom
              sx={{
                display: "flex",
                alignItems: "center",
                fontWeight: "medium",
              }}
            >
              <RuleIcon sx={{ mr: 1, color: "secondary.main" }} /> Estimation
              Accuracy per Sprint
            </Typography>
            <Divider sx={{ my: 1.5 }} />
            <ResponsiveContainer width="100%" height={300}>
              <BarChart
                data={estimationAccuracyPerSprint}
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
                  label={{ value: "Hours", angle: -90, position: "insideLeft" }}
                />
                <Tooltip />
                <Legend verticalAlign="top" />
                <Bar
                  dataKey="totalEstimated"
                  fill="#ffc658"
                  name="Total Estimated Hours"
                />
                <Bar
                  dataKey="totalReal"
                  fill="#ff7300"
                  name="Total Real Hours"
                />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Section 1.4: Individual Performance per Sprint */}
        <Grid item xs={12}>
          <Paper elevation={2} sx={{ p: 2.5 }}>
            <Typography
              variant="h6"
              gutterBottom
              sx={{
                display: "flex",
                alignItems: "center",
                fontWeight: "medium",
              }}
            >
              <PersonIcon sx={{ mr: 1, color: "primary.main" }} /> Individual
              Performance per Sprint
            </Typography>
            <Divider sx={{ my: 1.5 }} />
            {Object.entries(individualPerformancePerSprint)
              .sort(([sprintA], [sprintB]) => sprintA.localeCompare(sprintB))
              .map(([sprintName, usersData]) => (
                <Accordion
                  key={sprintName}
                  sx={{
                    "&:before": { display: "none" },
                    boxShadow: "none",
                    borderBottom: "1px solid rgba(0, 0, 0, 0.12)",
                  }}
                  // Default expanded state can be managed here if needed
                >
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography sx={{ fontWeight: "medium" }}>
                      {sprintName}
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails sx={{ p: 0 }}>
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Developer</TableCell>
                            <TableCell align="right">Completed Tasks</TableCell>
                            <TableCell align="right">Real Hours</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {Object.entries(usersData)
                            .filter(
                              ([, data]) =>
                                data.completedTasks > 0 || data.realHours > 0
                            ) // Show only users with activity in this sprint
                            .sort(([userA], [userB]) =>
                              userA.localeCompare(userB)
                            ) // Sort users alphabetically
                            .map(([userName, data]) => (
                              <TableRow key={userName}>
                                <TableCell component="th" scope="row">
                                  {userName}
                                </TableCell>
                                <TableCell align="right">
                                  {data.completedTasks}
                                </TableCell>
                                <TableCell align="right">
                                  {data.realHours.toFixed(1)}h
                                </TableCell>
                              </TableRow>
                            ))}
                          {/* Message if no users had activity in this specific sprint */}
                          {Object.values(usersData).every(
                            (d) => d.completedTasks === 0 && d.realHours === 0
                          ) && (
                            <TableRow>
                              <TableCell
                                colSpan={3}
                                align="center"
                                sx={{
                                  fontStyle: "italic",
                                  color: "text.secondary",
                                }}
                              >
                                No completed tasks recorded for this sprint.
                              </TableCell>
                            </TableRow>
                          )}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </AccordionDetails>
                </Accordion>
              ))}
            {/* Message if no sprint data exists at all */}
            {Object.keys(individualPerformancePerSprint).length === 0 &&
              !loading && (
                <Typography
                  sx={{ p: 2, fontStyle: "italic", color: "text.secondary" }}
                >
                  No sprint data available to display individual performance.
                </Typography>
              )}
          </Paper>
        </Grid>

        {/* Section 2.1: Overall Hours Per User */}
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
            <Typography variant="h6" gutterBottom>
              Overall Hours Real Per User
            </Typography>
            <Divider sx={{ my: 1.5 }} />
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={totalHoursPerUser}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis /> <Tooltip /> <Legend />
                <Bar dataKey="totalHours" fill="#8884d8" name="Total Hours" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Section 2.2: Overall Completed Tasks Per User */}
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 2.5, height: "100%" }}>
            <Typography variant="h6" gutterBottom>
              Overall Completed Tasks Per User
            </Typography>
            <Divider sx={{ my: 1.5 }} />
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={totalCompletedTasksPerUser}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis /> <Tooltip /> <Legend />
                <Bar
                  dataKey="doneTasks"
                  fill="#82ca9d"
                  name="Completed Tasks"
                />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default KPIPage;
