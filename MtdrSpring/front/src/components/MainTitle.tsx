import { Typography, SxProps, Theme } from "@mui/material";
import { ReactNode } from "react";

interface MainTitleProps {
  children: ReactNode;
  sx?: SxProps<Theme>;
}

const MainTitle = ({ children, sx = {} }: MainTitleProps) => {
  return (
    <Typography
      gutterBottom
      sx={{
        textAlign: "left",
        margin: "20px",
        color: "#fff",
        fontFamily: "Arial, sans-serif",
        fontSize: "1.5rem",
        opacity: 0.75,
        ...sx,
      }}
    >
      {children}
    </Typography>
  );
};

export default MainTitle;
