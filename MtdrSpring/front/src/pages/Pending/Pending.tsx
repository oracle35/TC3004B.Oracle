import { Typography } from "@mui/material";
import Layout from "../Layout.tsx";
import { ChecklistRtl } from "@mui/icons-material";

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
