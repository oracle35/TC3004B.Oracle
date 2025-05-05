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
import PersonIcon from "@mui/icons-material/Person";

interface PerformanceData {
  completedTasks: number;
  realHours: number;
}

interface IndividualPerformanceTableProps {
  data: Record<string, Record<string, PerformanceData>>;
}

const IndividualPerformanceTable: React.FC<IndividualPerformanceTableProps> = ({
  data,
}) => {
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
        <PersonIcon sx={{ mr: 1, color: "primary.main" }} /> Individual
        Performance per Sprint
      </Typography>
      <Divider sx={{ my: 1.5 }} />
      {sortedSprintNames.length > 0 ? (
        sortedSprintNames.map((sprintName) => {
          const usersData = data[sprintName];
          const sortedUsers = Object.entries(usersData)
              .filter(([, perfData]) => perfData.completedTasks > 0 || perfData.realHours > 0)
              .sort(([userA], [userB]) => userA.localeCompare(userB));
          const hasData = sortedUsers.length > 0;

          return (
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
                          {hasData ? (
                              sortedUsers.map(([userName, perfData]) => (
                                  <TableRow key={userName}>
                                  <TableCell component="th" scope="row">
                                      {userName}
                                  </TableCell>
                                  <TableCell align="right">
                                      {perfData.completedTasks}
                                  </TableCell>
                                  <TableCell align="right">
                                      {perfData.realHours.toFixed(1)}h
                                  </TableCell>
                                  </TableRow>
                              ))
                          ) : (
                              <TableRow>
                                  <TableCell
                                      colSpan={3}
                                      align="center"
                                      sx={{ fontStyle: "italic", color: "text.secondary" }}
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
          )
        })
      ) : (
        <Typography
          sx={{ p: 2, fontStyle: "italic", color: "text.secondary" }}
        >
          No sprint data available to display individual performance.
        </Typography>
      )}
    </Paper>
  );
};

export default IndividualPerformanceTable;