import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Box, TextField, Button, Alert } from "@mui/material";
import MainTitle from "../../components/MainTitle";

const LoginPage: React.FC = () => {
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [error, setError] = useState<string>("");
  const navigate = useNavigate();

  const handleLogin = (event: React.FormEvent) => {
    event.preventDefault();
    setError(""); // Clear previous errors

    // Simple hardcoded check
    if (username === "admin" && password === "admin") {
      localStorage.setItem("isAuthenticated", "true"); // Store auth status
      navigate("/");
    } else {
      setError("Invalid username or password");
    }
  };

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        minHeight: "100vh",
        p: 3,
        color: "white",
      }}
    >
      <MainTitle sx={{ color: "white" }}>Login</MainTitle>

      <Box
        component="form"
        onSubmit={handleLogin}
        sx={{
          mt: 3,
          width: "100%",
          maxWidth: "400px",
          display: "flex",
          flexDirection: "column",
          gap: 2,
        }}
      >
        <TextField
          label="Username"
          variant="outlined"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
          fullWidth
          InputProps={{
            style: {
              backgroundColor: "white",
              color: "black",
            },
          }}
          InputLabelProps={{
            style: {
              color: "black",
            },
          }}
        />
        <TextField
          label="Password"
          type="password"
          variant="outlined"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          fullWidth
          InputProps={{
            style: {
              backgroundColor: "white",
              color: "black",
            },
          }}
          InputLabelProps={{
            style: {
              color: "black",
            },
          }}
        />
        {error && <Alert severity="error">{error}</Alert>}
        <Button
          type="submit"
          variant="contained"
          fullWidth
          sx={{
            backgroundColor: "#c74634",
            color: "white",
            "&:hover": {
              backgroundColor: "#a63b2c",
            },
            fontWeight: "bold",
          }}
        >
          Login
        </Button>
      </Box>
    </Box>
  );
};

export default LoginPage;
