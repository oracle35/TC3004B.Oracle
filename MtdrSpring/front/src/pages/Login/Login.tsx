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
    // TODO: Change to proper authentication logic later.
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
      }}
    >
      <MainTitle>Login</MainTitle>

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
        />
        {error && <Alert severity="error">{error}</Alert>}
        <Button
          type="submit"
          variant="contained"
          fullWidth

        >
          Login
        </Button>
      </Box>
    </Box>
  );
};

export default LoginPage;
