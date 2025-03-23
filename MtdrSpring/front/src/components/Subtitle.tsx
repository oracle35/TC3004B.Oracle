import { Typography } from "@mui/material";
import { ReactNode } from "react";

interface SubtitleProps {
  children: ReactNode;
}

export const Subtitle = ({ children }: SubtitleProps) => {
  return (
    <Typography variant="h2" className="text-2xl font-bold text-gray-800 mb-4">
      {children}
    </Typography>
  );
};
