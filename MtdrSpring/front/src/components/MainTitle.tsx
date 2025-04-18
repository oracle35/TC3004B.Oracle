import { Typography } from "@mui/material";
import { ReactNode } from "react";

interface MainTitleProps {
  children: ReactNode;
}

const MainTitle = ({ children }: MainTitleProps) => {
  return (
    <Typography
      gutterBottom
      sx={{
        textAlign: "left",
        margin: "20px",
        color: "#000",
        fontFamily: "Arial, sans-serif",
        fontSize: "1.5rem",
        opacity: 0.75,
      }}
    >
      {children}
    </Typography>
  );
};

export default MainTitle;
