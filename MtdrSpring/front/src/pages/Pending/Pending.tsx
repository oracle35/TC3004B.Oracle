import { Typography } from "@mui/material";
import Layout from "../Layout.tsx";
import { ChecklistRtl } from "@mui/icons-material";

// Pending Tasks page
// This page will show the tasks that are pending to be completed, ordered by the closest due date.

const PendingTasks = () => {
  return (
    <Layout
      title="Pending Tasks"
      icon={<ChecklistRtl fontSize="large" htmlColor="white" />}
    >
      <Typography variant="body1">
        Aquí aparecerán las tareas pendientes.
      </Typography>
    </Layout>
  );
};

export default PendingTasks;
