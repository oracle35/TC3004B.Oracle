import { AppBar, Toolbar, Button, Box } from "@mui/material";
import { useNavigate } from "react-router-dom";

const NavBar = () => {
  const navigate = useNavigate();

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
        backgroundColor: "#c74634",
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
            sx={{ color: "white", "&:hover": { backgroundColor: "#a43d2f" } }}
            onClick={() => navigate("/")}
          >
            HOME
          </Button>
          <Button
            sx={{ color: "white", "&:hover": { backgroundColor: "#a43d2f" } }}
            onClick={() => navigate("/kpi")}
          >
            KPI AND STATISTICS
          </Button>
          <Button
            sx={{ color: "white", "&:hover": { backgroundColor: "#a43d2f" } }}
            onClick={() => navigate("/stats")}
          >
            SPRINT OVERVIEW
          </Button>
          <Button
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
