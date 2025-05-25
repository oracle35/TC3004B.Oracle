import { Box, Grid } from "@mui/material";
import MainTitle from "../components/MainTitle.tsx";
import NavBar from "../components/NavBar/NavBar.tsx";
import React from "react";

/*
 * General Layout component for the application.
 * Contains the main title and navigation bar.
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
  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1920, margin: "auto" }}>
      <Grid container spacing={1} alignItems="center" mb={2}>
        <Grid item xs>
          <MainTitle>
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
