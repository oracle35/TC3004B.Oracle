import { createTheme } from "@mui/material/styles";

const theme = createTheme({
  palette: {
    mode: "dark",
    primary: {
      main: "#c74634",
    },
    secondary: {
      main: "#1976d2",
    },
    background: {
      default: "#312d2a",
      paper: "#23201d",
    },
    text: {
      primary: "#fff",
      secondary: "#bdbdbd",
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
  },
  typography: {
    fontFamily: "Montserrat, Arial, sans-serif",
  },
  components: {
    MuiTableCell: {
      styleOverrides: {
        root: {
          color: "#fff",
          backgroundColor: "#312d2a",
        },
        head: {
          backgroundColor: "#c74634",
          color: "#fff",
        },
      },
    },
  },
});

export default theme;
