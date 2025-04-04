import { Typography } from "@mui/material";

const MainTitle = ({ title }: { title: string }) => {
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
      {title}
    </Typography>
  );
};

export default MainTitle;
