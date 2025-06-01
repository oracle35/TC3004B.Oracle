import { Box, Grid, useTheme } from "@mui/material";
import MainTitle from "../components/MainTitle.tsx";
import NavBar from "../components/NavBar/NavBar.tsx";
import React from "react";

/*
 * General Layout component for the application.
 * Contains the main title and navigation bar.
 * It is used in all pages BUT the login page.
 * */

export default function Layout({
  children,
  title,
  icon,
}: {
  children: React.ReactNode;
  title?: string;
  icon?: React.ReactNode;
}) {
  const theme = useTheme();

  

  return (
    <Box
      sx={{
        p: { xs: 2, sm: 3 },
        maxWidth: 1920,
        margin: "auto",
        bgcolor: theme.palette.background.default,
        color: theme.palette.text.primary,
        minHeight: "100vh",
        transition: "background-color 0.3s, color 0.3s",
      }}
    >
      <Grid container spacing={1} alignItems="center" mb={2}>
        <Grid item xs>
          <MainTitle sx={{ color: theme.palette.text.primary }}>
            <div className="mr-5">{icon}</div>
            {title}
          </MainTitle>
          <NavBar />
        </Grid>
      </Grid>
      {children}
    </Box>
  );
}
