import { Typography } from "@mui/material";
import { ReactNode } from "react";

interface SubtitleProps {
  children: ReactNode;
}

export const Subtitle = ({ children }: SubtitleProps) => {
  return (
    <Typography
      variant="h2"
      sx={{
        textAlign: "left",
        margin: "20px",
        color: "#fff",
        fontFamily: "Arial, sans-serif",
        fontSize: "1.2rem",
        opacity: 0.65,
      }}
    >
      {children}
    </Typography>
  );
};
