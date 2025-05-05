import React from "react";
import {
  Paper,
  Typography,
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
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import PlaylistAddCheckIcon from "@mui/icons-material/PlaylistAddCheck";

interface CompletedTask {
  id: number;
  name: string;
  developer: string;
  estimated: number;
  real: number | null;
}

interface CompletedTasksTableProps {
  data: Record<string, CompletedTask[]>;
}

const CompletedTasksTable: React.FC<CompletedTasksTableProps> = ({ data }) => {
  const sortedSprintNames = Object.keys(data).sort((a, b) => a.localeCompare(b));

  return (
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
      {sortedSprintNames.length > 0 ? (
        sortedSprintNames.map((sprintName) => (
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
                {sprintName} ({data[sprintName].length} tasks)
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
                    {data[sprintName].map((task) => (
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
        ))
      ) : (
        <Typography
          sx={{ p: 2, fontStyle: "italic", color: "text.secondary" }}
        >
          No completed tasks found.
        </Typography>
      )}
    </Paper>
  );
};

export default CompletedTasksTable;