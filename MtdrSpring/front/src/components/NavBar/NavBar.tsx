import { AppBar, Box, Button, IconButton, Toolbar, useTheme } from "@mui/material";
import React from "react";
import { useNavigate } from "react-router-dom";
import HomeIcon from "@mui/icons-material/Home";
import BarChartIcon from "@mui/icons-material/BarChart";
import TimelineIcon from "@mui/icons-material/Timeline";
import PendingActionsIcon from "@mui/icons-material/PendingActions";
import LogoutIcon from "@mui/icons-material/Logout";
import Brightness4Icon from "@mui/icons-material/Brightness4";
import Brightness7Icon from "@mui/icons-material/Brightness7";
import { ColorModeContext } from "../../App";

const NavBar = () => {
  const navigate = useNavigate();
  const theme = useTheme(); 
  const colorMode = React.useContext(ColorModeContext); 


  const handleLogout = () => {
    localStorage.removeItem("isAuthenticated");
    navigate("/login");
  };

  return (
    <AppBar
      position="static"
      sx={{
        height: 60,
        borderRadius: 3,
        mx: 2,
        mt: 2,
        bgcolor: "primary.main",
      }}
    >
      <Toolbar sx={{ minHeight: "100% !important", padding: "0 !important" }}>
        <Box
          sx={{
            display: "flex",
            justifyContent: "center",
            width: "100%",
            gap: 2,
          }}
        >
          <Button
            startIcon={<HomeIcon />}
            sx={{ color: "white", "&:hover": { backgroundColor: "#a43d2f" } }}
            onClick={() => navigate("/")}
          >
            HOME
          </Button>
          <Button
            startIcon={<BarChartIcon />}
            sx={{ color: "white", "&:hover": { backgroundColor: "#a43d2f" } }}
            onClick={() => navigate("/kpi")}
          >
            KPI AND STATISTICS
          </Button>
          <Button
            startIcon={<TimelineIcon />}
            sx={{ color: "white", "&:hover": { backgroundColor: "#a43d2f" } }}
            onClick={() => navigate("/stats")}
          >
            SPRINT OVERVIEW
          </Button>
          <Button
            startIcon={<PendingActionsIcon />}
            sx={{ color: "white", "&:hover": { backgroundColor: "#a43d2f" } }}
            onClick={() => navigate("/pending")}
          >
            PENDING TASKS
          </Button>
          <IconButton
            sx={{ ml: 1, color: "white" }}
            onClick={colorMode.toggleColorMode}
            title="Toggle light/dark mode"
          >
            {theme.palette.mode === "dark" ? <Brightness7Icon /> : <Brightness4Icon />}
          </IconButton>
          <Button
            startIcon={<LogoutIcon />}
            sx={{ color: "white", "&:hover": { backgroundColor: "#a43d2f" } }}
            onClick={handleLogout}
          >
            LOGOUT
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default NavBar;
