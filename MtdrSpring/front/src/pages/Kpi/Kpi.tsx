import { useEffect, useMemo, useState } from "react";
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import { getTasks } from "../../api/task";
import { getUsers } from "../../api/user";
import { getSprints } from "../../api/sprint";
import { Sprint } from "../../models/Sprint";
import { getAiSummary } from "../../api/kpi";
import { CircularProgress, Grid } from "@mui/material";

// Charts and Components
import AiSummarySection from "./components/AiSummarySection";
import CompletedTasksTable from "./components/CompletedTasksTable";
import IndividualPerformanceTable from "./components/IndividualPerformanceTable";
import TeamPerformanceChart from "./graph/TeamPerformanceChart";
import EstimationAccuracyChart from "./graph/EstimationAccuracyChart";
import OverallHoursChart from "./graph/OverallHoursChart";
import OverallTasksChart from "./graph/OverallTasksChart";
import IndividualPerformanceChart from "./graph/IndividualPerformanceChart";
import TotalHoursPerSprintChart from "./graph/TotalHoursPerSprintChart";
import Layout from "../Layout.tsx";
import AssessmentIcon from "@mui/icons-material/Assessment";

// --- Helper Functions (Keep them here as they are used by useMemo) ---
const getUserName = (userId: number, users: User[]) => {
  const user = users.find((u) => u.id_User === userId);
  return user ? user.name : "Unknown User";
};

const getSprintName = (
  sprintId: number | null | undefined,
  sprints: Sprint[],
) => {
  // Handle null/undefined sprintId, defaulting to -1
  const id = sprintId ?? -1;
  if (id === -1) return "Backlog / Unassigned";
  const sprint = sprints.find((s) => s.id_Sprint === id);
  return sprint ? sprint.name : `Sprint ${id}`;
};

const KPIPage = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [sprints, setSprints] = useState<Sprint[]>([]);
  const [aiSummary, setAiSummary] = useState<string>("");
  const [aiLoading, setAiLoading] = useState<boolean>(false);
  const [aiError, setAiError] = useState<string | null>(null);

  // --- Effect for fetching initial data ---
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setAiLoading(true);
      setAiError(null);
      setAiSummary("");
      try {
        const [tasksData, usersData, sprintData] = await Promise.all([
          getTasks(),
          getUsers(),
          getSprints(),
        ]);
        setTasks(tasksData || []);
        setUsers(usersData || []);
        setSprints(
          (sprintData || []).sort((a, b) => a.name.localeCompare(b.name)),
        );
      } catch (error) {
        console.error("Error fetching data:", error);
        setAiError("Failed to load project data. Cannot generate AI summary.");
        setAiLoading(false);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // --- Memoized calculations (Keep them here near the source data) ---
  const completedTasksBySprint = useMemo(() => {
    // ... (keep the existing calculation logic)
    if (!tasks || !users || !sprints) return {};
    const completed = tasks.filter((task) => task.state === "DONE");
    return completed.reduce(
      (acc, task) => {
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
      },
      {} as Record<
        string,
        Array<{
          id: number;
          name: string;
          developer: string;
          estimated: number;
          real: number | null;
        }>
      >,
    );
  }, [tasks, users, sprints]);

  const teamPerformancePerSprint = useMemo(() => {
    // ... (keep the existing calculation logic)
    if (!tasks || !sprints) return [];
    const sprintStats = sprints.reduce(
      (acc, sprint) => {
        acc[sprint.id_Sprint] = {
          sprintName: sprint.name,
          completedTasks: 0,
          totalRealHours: 0,
        };
        return acc;
      },
      {} as Record<
        number,
        { sprintName: string; completedTasks: number; totalRealHours: number }
      >,
    );

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
      });

    return Object.values(sprintStats).sort((a, b) =>
      a.sprintName.localeCompare(b.sprintName),
    );
  }, [tasks, sprints]);

  const individualPerformancePerSprint = useMemo(() => {
    // ... (keep the existing calculation logic)
    if (!tasks || !users || !sprints) return {};
    const performance: Record<
      string,
      Record<string, { completedTasks: number; realHours: number }>
    > = {};

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

    const backlogSprintName = "Backlog / Unassigned";
    performance[backlogSprintName] = {};
    users.forEach((user) => {
      performance[backlogSprintName][user.name] = {
        completedTasks: 0,
        realHours: 0,
      };
    });

    tasks
      .filter((task) => task.state === "DONE")
      .forEach((task) => {
        const sprintName = getSprintName(task.id_Sprint, sprints);
        const userName = getUserName(task.assignedTo, users);

        if (performance[sprintName] && performance[sprintName][userName]) {
          performance[sprintName][userName].completedTasks += 1;
          performance[sprintName][userName].realHours += task.hoursReal || 0;
        }
      });

    return performance;
  }, [tasks, users, sprints]);

  const totalHoursPerSprint = useMemo(() => {
    const sprintHours: Record<string, number> = {};

    // Calculate total hours for each sprint from individual performance data
    Object.entries(individualPerformancePerSprint).forEach(
      ([sprintName, userData]) => {
        sprintHours[sprintName] = Object.values(userData).reduce(
          (total, user) => total + user.realHours,
          0,
        );
      },
    );

    // Convert to array format for the chart
    return Object.entries(sprintHours)
      .filter(([sprintName]) => sprintName !== "Backlog / Unassigned")
      .map(([sprintName, totalHours]) => ({
        sprintName,
        totalHours,
      }))
      .sort((a, b) => a.sprintName.localeCompare(b.sprintName));
  }, [individualPerformancePerSprint]);

  const estimationAccuracyPerSprint = useMemo(() => {
    // ... (keep the existing calculation logic)
    if (!tasks || !sprints) return [];
    const accuracyStats = sprints.reduce(
      (acc, sprint) => {
        acc[sprint.id_Sprint] = {
          sprintName: sprint.name,
          totalEstimated: 0,
          totalReal: 0,
        };
        return acc;
      },
      {} as Record<
        number,
        { sprintName: string; totalEstimated: number; totalReal: number }
      >,
    );

    accuracyStats[-1] = {
      sprintName: "Backlog / Unassigned",
      totalEstimated: 0,
      totalReal: 0,
    };

    tasks
      .filter((task) => task.state === "DONE")
      .forEach((task) => {
        const sprintId = task.id_Sprint ?? -1;
        if (accuracyStats[sprintId]) {
          accuracyStats[sprintId].totalEstimated += task.hoursEstimated || 0;
          accuracyStats[sprintId].totalReal += task.hoursReal || 0;
        } else if (accuracyStats[-1] && sprintId === -1) {
          accuracyStats[-1].totalEstimated += task.hoursEstimated || 0;
          accuracyStats[-1].totalReal += task.hoursReal || 0;
        }
      });

    return Object.values(accuracyStats)
      .filter((s) => s.totalEstimated > 0 || s.totalReal > 0)
      .sort((a, b) => a.sprintName.localeCompare(b.sprintName));
  }, [tasks, sprints]);

  const totalHoursPerUser = useMemo(() => {
    // ... (keep the existing calculation logic)
    if (!tasks || !users) return [];
    return users.map((user) => {
      const totalHours = tasks
        .filter((task) => task.assignedTo === user.id_User && task.hoursReal)
        .reduce((sum, task) => sum + (task.hoursReal || 0), 0);
      return { name: user.name, totalHours };
    });
  }, [tasks, users]);

  const totalCompletedTasksPerUser = useMemo(() => {
    // ... (keep the existing calculation logic)
    if (!tasks || !users) return [];
    return users.map((user) => {
      const count = tasks.filter(
        (task) => task.assignedTo === user.id_User && task.state === "DONE",
      ).length;
      return { name: user.name, doneTasks: count };
    });
  }, [tasks, users]);

  // --- AI Summary Generation Effect ---
  useEffect(() => {
    // ... (keep the existing effect logic)
    if (
      loading ||
      !tasks ||
      tasks.length === 0 ||
      !users ||
      users.length === 0 ||
      !sprints ||
      sprints.length === 0
    ) {
      if (!loading) {
        setAiLoading(false);
        if (!aiError) {
          setAiError("Insufficient data loaded to generate AI summary.");
        }
      }
      return;
    }

    const fetchAiSummary = async () => {
      if (!aiLoading) setAiLoading(true);
      setAiError(null);

      try {
        console.log("Fetching AI summary from backend...");
        const summary = await getAiSummary();
        console.log("Received summary from backend:", summary);
        setAiSummary(summary);
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (error: any) {
        console.error("Error fetching AI summary from backend:", error);
        setAiError(error.message || "Failed to fetch AI summary from backend.");
        setAiSummary("");
      } finally {
        setAiLoading(false);
      }
    };

    fetchAiSummary();
  }, [loading, tasks, users, sprints, aiError]); // Added aiError dependency to potentially retry if needed

  // --- Render Logic ---
  if (loading && tasks.length === 0) {
    // Check tasks length to avoid flicker if data loads fast
    return (
      <Layout
        title="KPI and Statistics"
        icon={<AssessmentIcon fontSize="large" htmlColor="white" />}
      >
        <CircularProgress sx={{ mt: 4 }} />
      </Layout>
    );
  }

  return (
    <Layout
      title="KPI and Statistics"
      icon={<AssessmentIcon fontSize="large" htmlColor="white" />}
    >
      {/* AI Summary Section */}
      <Grid item xs={12} mb={3}>
        <AiSummarySection
          aiSummary={aiSummary}
          aiLoading={aiLoading}
          aiError={aiError}
        />
      </Grid>

      <Grid container spacing={3} mt={1}>
        {/* Section 1.1: Completed Task Details */}
        <Grid item xs={12}>
          <CompletedTasksTable data={completedTasksBySprint} />
        </Grid>

        {/* Section 1.2: Team Performance per Sprint */}
        <Grid item xs={12} md={6}>
          <TeamPerformanceChart data={teamPerformancePerSprint} />
        </Grid>

        {/* Section 1.3: Estimation Accuracy per Sprint */}
        <Grid item xs={12} md={6}>
          <EstimationAccuracyChart data={estimationAccuracyPerSprint} />
        </Grid>

        {/* Section 1.4: Individual Performance Chart */}
        <Grid item xs={12}>
          <IndividualPerformanceChart data={individualPerformancePerSprint} />
        </Grid>

        {/* Section 1.5: Individual Performance per Sprint */}
        <Grid item xs={12}>
          <IndividualPerformanceTable data={individualPerformancePerSprint} />
        </Grid>

        <Grid item xs={12} md={6}>
          <TotalHoursPerSprintChart data={totalHoursPerSprint} />
        </Grid>

        {/* Section 2.1: Overall Hours Per User */}
        <Grid item xs={12} md={6}>
          <OverallHoursChart data={totalHoursPerUser} />
        </Grid>

        {/* Section 2.2: Overall Completed Tasks Per User */}
        <Grid item xs={12} md={6}>
          <OverallTasksChart data={totalCompletedTasksPerUser} />
        </Grid>
      </Grid>
    </Layout>
  );
};

export default KPIPage;
