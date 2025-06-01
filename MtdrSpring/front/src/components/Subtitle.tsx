import { SxProps, Theme, Typography } from "@mui/material";
import { ReactNode } from "react";

interface SubtitleProps {
  children: ReactNode;
  sx?: SxProps<Theme>;
}

export const Subtitle = ({ children, sx }: SubtitleProps) => {
  return (
    <Typography
      variant="h2"
      sx={{
        textAlign: "left",
        margin: "20px",
        color: "secondary",
        fontFamily: "Arial, sans-serif",
        fontSize: "1.2rem",
        opacity: 0.65,
        ...sx,
      }}
    >
      {children}
    </Typography>
  );
};
