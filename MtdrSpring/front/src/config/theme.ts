import { createTheme } from "@mui/material";

const getTheme = (mode: "light" | "dark") =>
  createTheme({
    palette: {
      mode,
      primary: {
        main: "#c74634",
      },
      secondary: {
        main: "#1976d2",
      },
      background: {
        default: mode === "dark" ? "#312d2a" : "#fff",
        paper: mode === "dark" ? "#23201d" : "#f5f5f5",
      },
      text: {
        primary: mode === "dark" ? "#fff" : "#23201d",
        secondary: mode === "dark" ? "#bdbdbd" : "#555",
      },
      success: {
        main: "#61A60E",
      },
      warning: {
        main: "#F7901E",
      },
      error: {
        main: "#E60000",
      },
      info: {
        main: "#00A3E0",
      },
      action: {
        hover: mode === "dark" ? "#3a2e2e" : "#f5e6e6",
        selected: mode === "dark" ? "#4d3939" : "#ffeaea",
        disabled: mode === "dark" ? "#555" : "#ccc",
        disabledBackground: mode === "dark" ? "#2a2323" : "#eee",
        focus: mode === "dark" ? "#c74634" : "#c74634",
      },
    },
    typography: {
      fontFamily: "Montserrat, Arial, sans-serif",
    },
    components: {
      MuiTableCell: {
        styleOverrides: {
          root: {
            color: mode === "dark" ? "#fff" : "#23201d",
            // backgroundColor: mode === "dark" ? "#312d2a" : "#fff",
            backgroundColor: "transparent",
          },
          head: {
            backgroundColor: "#c74634",
            color: "#fff",
          },
        },
      },
    },
  });

export default getTheme;
