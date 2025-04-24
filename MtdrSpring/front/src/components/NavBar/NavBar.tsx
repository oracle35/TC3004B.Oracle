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
                backgroundColor: "#c74634", // Color de fondo para la barra de navegación
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
                        sx={{ color: "white", '&:hover': { backgroundColor: "#a43d2f" } }}
                        onClick={() => navigate("/kpi")}
                    >
                        KPI
                    </Button>
                    <Button
                        sx={{ color: "white", '&:hover': { backgroundColor: "#a43d2f" } }}
                        onClick={() => navigate("/stats")}
                    >
                        Estadísticas
                    </Button>
                    <Button
                        sx={{ color: "white", '&:hover': { backgroundColor: "#a43d2f" } }}
                        onClick={() => navigate("/test")}
                    >
                        Prueba
                    </Button>
                    <Button
                        sx={{ color: "white", '&:hover': { backgroundColor: "#a43d2f" } }}
                        onClick={handleLogout}
                    >
                        Logout
                    </Button>
                </Box>
            </Toolbar>
        </AppBar>
    );
};

export default NavBar;
